package com.example.szakdoga;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.rajat.pdfviewer.PdfViewerActivity;
import com.rajat.pdfviewer.util.CacheStrategy;
import com.rajat.pdfviewer.util.ToolbarTitleBehavior;
import com.rajat.pdfviewer.util.saveTo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public class SearchPDF extends AppCompatActivity {

    EditText searchEditText;
    LinearLayout resultContainer;
    private String currentPath = "";
    ProgressBar loader;


    private String SERVER_IP;
    private String SHARE_NAME;
    private String FOLDER_PATH;
    private String USERNAME;
    private String PASSWORD;

    static class PDF {
        String name;
        FileIdBothDirectoryInformation fileID;

        PDF(String name, FileIdBothDirectoryInformation fileID) {
            this.fileID = fileID;
            this.name = name;
        }
    }

    List<PDF> allPdf = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_pdf);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loader = findViewById(R.id.loader);

        ///*Loading config.json*\\\
        loadJson();

        searchEditText = findViewById(R.id.searchEditText);
        resultContainer = findViewById(R.id.resultContainer);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String rawQuery = s.toString();
                String query = rawQuery.trim();
                if (query.isEmpty() && rawQuery.isEmpty()) {
                    fetchItemsFromNas(currentPath);
                } else if (query.length() >= 3) {
                    searchRecursivelyOnNas(FOLDER_PATH, query);
                }
            }
        });
        //currentpath betoltese hogy ne FOLDER_PATH-ra hagyatkozzunk
        if (savedInstanceState != null) {
            currentPath = savedInstanceState.getString("currentPath", FOLDER_PATH);
        } else {
            currentPath = FOLDER_PATH;
        }

        fetchItemsFromNas(currentPath);
    }

    //**************************************************Getting NAS config from JSON**************************************************\\
    private void loadJson() {
        try (InputStream inputStream = getAssets().open("config.json")) {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            USERNAME = jsonObject.getString("username");
            SERVER_IP = jsonObject.getString("nas_ip");
            PASSWORD = jsonObject.getString("password");
            SHARE_NAME = jsonObject.getString("share_name");
            FOLDER_PATH = jsonObject.getString("folder_path");

        } catch (Exception e) {
            throw new RuntimeException("Hiba a config.json beolvasásakor", e);
        }
    }

    //************************************************** **************************************************\\
    private void searchRecursivelyOnNas(String startPath, String query) {
        new Thread(() -> {
            SMBClient client = new SMBClient();
            try (Connection connection = client.connect(SERVER_IP)) {
                Session session = connection.authenticate(new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), ""));
                try (DiskShare share = (DiskShare) session.connectShare(SHARE_NAME)) {
                    List<PDF> matchedPdfs = new ArrayList<>();
                    recursiveSearch(share, startPath, query, matchedPdfs);

                    runOnUiThread(() -> {
                        allPdf.clear();
                        allPdf.addAll(matchedPdfs);
                        updateResults(query);
                    });
                }
            } catch (Exception e) {
                Log.e("SMB_SEARCH", "Rekurzív keresés hiba", e);
                runOnUiThread(() -> Toast.makeText(this, "Hiba a rekurzív keresés során", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    //************************************************** **************************************************\\
    private void recursiveSearch(DiskShare share, String path, String query, List<PDF> resultList) {
        for (FileIdBothDirectoryInformation item : share.list(path)) {
            String name = item.getFileName();
            if (name.equals(".") || name.equals("..")) continue;

            String fullPath = path.endsWith("/") ? path + name : path + "/" + name;

            if ((item.getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) != 0) {
                recursiveSearch(share, fullPath, query, resultList);
            } else if (name.toLowerCase().endsWith(".pdf") && name.toLowerCase().contains(query.toLowerCase())) {
                resultList.add(new PDF(fullPath, item));
            }
        }
    }

    //**************************************************Getting PDFs from Server**************************************************\\
    private void fetchItemsFromNas(String folderPath) {
        new Thread(() -> {
            SMBClient client = new SMBClient();
            try (Connection connection = client.connect(SERVER_IP)) {
                Session session = connection.authenticate(
                        new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), "")
                );

                try (DiskShare share = (DiskShare) session.connectShare(SHARE_NAME)) {
                    List<PDF> tempList = new ArrayList<>();
                    List<String> folderList = new ArrayList<>();

                    for (FileIdBothDirectoryInformation item : share.list(folderPath)) {
                        String name = item.getFileName();
                        if (name.equals(".") || name.equals("..")) continue;

                        if ((item.getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) != 0) {
                            folderList.add(name);
                        } else if (name.toLowerCase().endsWith(".pdf")) {
                            tempList.add(new PDF(name, item));
                        }
                    }

                    runOnUiThread(() -> {
                        allPdf.clear();
                        allPdf.addAll(tempList);
                        currentPath = folderPath;
                        updateResultsWithFolders(folderList, tempList);
                    });
                }

            } catch (Exception e) {
                Log.e("SMB", "Hiba a NAS elérésekor", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Hiba a NAS elérésekor", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    //**************************************************Opening PDFs from Server**************************************************\\
    private void openPdfFromNas(String fullFilePath) {
        new Thread(() -> {
            try {
                File cacheDir = getCacheDir();
                String fileName = fullFilePath.substring(fullFilePath.lastIndexOf('/') + 1);
                File tempFile = new File(cacheDir, fileName);

                SMBClient client = new SMBClient();
                try (Connection connection = client.connect(SERVER_IP)) {
                    Session session = connection.authenticate(
                            new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), "")
                    );
                    try (DiskShare share = (DiskShare) session.connectShare(SHARE_NAME)) {
                        try (com.hierynomus.smbj.share.File smbFile = share.openFile(
                                fullFilePath,
                                EnumSet.of(AccessMask.GENERIC_READ),
                                null,
                                SMB2ShareAccess.ALL,
                                SMB2CreateDisposition.FILE_OPEN,
                                null)) {
                            try (InputStream is = smbFile.getInputStream();
                                 OutputStream os = new FileOutputStream(tempFile)) {
                                byte[] buffer = new byte[16384];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    os.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                    }
                }

                runOnUiThread(() -> {
                    loader.setVisibility(View.GONE);
                    launchPdf(tempFile.getAbsolutePath(), fileName);
                });

            } catch (Exception e) {
                Log.e("PDF_OPEN", "Hiba a PDF megnyitásakor", e);
                runOnUiThread(() -> {
                    loader.setVisibility(View.GONE);  // Elrejtjük hiba esetén is
                    Toast.makeText(this, "Hiba a PDF megnyitásakor", Toast.LENGTH_SHORT).show();
                });

            }
        }).start();
    }

    //**************************************************Responsible for opening a PDF in a new "window"**************************************************\\
    private void launchPdf(String path, String fileName) {
        Toast.makeText(this, "PDF megnyitása: " + fileName, Toast.LENGTH_SHORT).show();

        startActivity(PdfViewerActivity.Companion.launchPdfFromPath(
                this,
                path,
                fileName,
                saveTo.ASK_EVERYTIME,
                false,
                true,
                ToolbarTitleBehavior.SINGLE_LINE_SCROLLABLE,
                CacheStrategy.MAXIMIZE_PERFORMANCE
        ));
    }


    private String normalizePath(String path) {
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }


    //**************************************************Dynamic search through Folders**************************************************\\
    @SuppressLint("SetTextI18n")
    private void updateResultsWithFolders(List<String> folders, List<PDF> pdfs) {

        resultContainer.removeAllViews();

        String lastPath = currentPath;
        currentPath = lastPath;

        if (!normalizePath(lastPath).equals(normalizePath(FOLDER_PATH))) {
            Button backBtn = new Button(this);
            backBtn.setText("<-- Vissza");
            backBtn.setOnClickListener(v -> {
                String parentPath;

                if (currentPath.startsWith(FOLDER_PATH) && currentPath.length() > FOLDER_PATH.length()) {
                    parentPath = currentPath.substring(0, currentPath.lastIndexOf('/'));
                    if (parentPath.endsWith("/")) {
                        parentPath = parentPath.substring(0, parentPath.length() - 1);
                    }
                } else {
                    parentPath = FOLDER_PATH;
                }
                currentPath = parentPath;
                fetchItemsFromNas(parentPath);
            });

            styleButton(backBtn);
            resultContainer.addView(backBtn);
        }

        for (String folderName : folders) {
            Button btn = new Button(this);
            btn.setText("📁 " + folderName);
            btn.setOnClickListener(v -> {
                String newPath = currentPath;
                if (!newPath.endsWith("/")) newPath += "/";
                fetchItemsFromNas(newPath + folderName);
            });
            styleButton(btn);
            resultContainer.addView(btn);
        }

        for (PDF pdf : pdfs) {
            Button btn = new Button(this);
            btn.setText("📄 " + pdf.name);
            btn.setOnClickListener(v -> {
                v.setEnabled(false);
                loader.setVisibility(View.VISIBLE);

                String path = currentPath;
                if (!path.endsWith("/")) path += "/";
                openPdfFromNas(path + pdf.name);
                v.postDelayed(() -> v.setEnabled(true), 2000);
            });
            styleButton(btn);
            resultContainer.addView(btn);
        }
    }
    //**************************************************Dynamic search inside a Folder**************************************************\\
    @SuppressLint("SetTextI18n")
    private void updateResults(String query) {
        resultContainer.removeAllViews();
        if (query.length() >= 3) {
            for (PDF pdf : allPdf) {
                if (pdf.name.toLowerCase().contains(query.toLowerCase())) {
                    Button btn = new Button(this);
                    btn.setText("📄 " + pdf.name.substring(pdf.name.lastIndexOf('/') + 1) + "\n📁 " + pdf.name.substring(0, pdf.name.lastIndexOf('/')));
                    btn.setOnClickListener(v -> {
                        v.setEnabled(false);
                        openPdfFromNas(pdf.name);
                        loader.setVisibility(View.VISIBLE);

                        v.postDelayed(() -> v.setEnabled(true), 2000);
                    });
                    styleButton(btn);
                    resultContainer.addView(btn);
                }
                if (resultContainer.getChildCount() >= 100) {
                    break;
                }
            }
        }
        if (query.length() < 3) {
            return;
        }
Log.d("kilimanjaro", String.valueOf(resultContainer.getChildCount()));
    }

    //**************************************************Custom button for every PDF and BackButton**************************************************\\
    private void styleButton(Button btn) {
        btn.setPadding(30, 30, 30, 30);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 30);
        btn.setLayoutParams(params);
        btn.setTextSize(20);
        btn.setBackgroundResource(R.drawable.rounded_edittext);
        btn.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        btn.setTextColor(Color.BLACK);
        btn.setElevation(1);
    }

    ///Saving the current path so bugs don't appear
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentPath", currentPath);
    }
}
