package com.example.szakdoga;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.rajat.pdfviewer.PdfViewerActivity;
import com.rajat.pdfviewer.util.CacheStrategy;
import com.rajat.pdfviewer.util.ToolbarTitleBehavior;
import com.rajat.pdfviewer.util.saveTo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

public class SearchPDF extends AppCompatActivity {

    EditText searchEditText;
    LinearLayout resultContainer;
    private static final String SERVER_IP = "192.168.255.40";
    private static final String SHARE_NAME = "Megosztas";
    private static final String FOLDER_PATH = "Dokumentumok/B&B/Div+Pen/";
    private static final String USERNAME = "bb";
    private static final String PASSWORD = "Unicornis911!";

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
        setContentView(R.layout.activity_logged_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchEditText = findViewById(R.id.searchEditText);
        resultContainer = findViewById(R.id.resultContainer);

        // Text change listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateResults(s.toString());
            }
        });

        fetchPdfsFromNas();
    }

    private void fetchPdfsFromNas() {
        new Thread(() -> {
            SMBClient client = new SMBClient();
            try (Connection connection = client.connect(SERVER_IP)) {
                Session session = connection.authenticate(
                        new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), "DOMAIN")
                );
                try (DiskShare share = (DiskShare) session.connectShare(SHARE_NAME)) {
                    List<PDF> tempList = new ArrayList<>();
                    for (FileIdBothDirectoryInformation item : share.list(FOLDER_PATH)) {
                        String fileName = item.getFileName();
                        if (fileName.endsWith(".pdf")) {
                            tempList.add(new PDF(fileName, item));
                        }
                    }
                    runOnUiThread(() -> {
                        allPdf.clear();
                        allPdf.addAll(tempList);
                        updateResults("");
                    });
                }
            } catch (Exception e) {
                Log.e("SMB", "Error accessing NAS", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Hiba a NAS elérésekor", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void openPdfFromNas(String fileName) {
        new Thread(() -> {
            try {
                // Create temp file in cache
                File cacheDir = getCacheDir();
                File tempFile = new File(cacheDir, fileName);

                // Connect to NAS and download file
                SMBClient client = new SMBClient();
                try (Connection connection = client.connect(SERVER_IP)) {
                    Session session = connection.authenticate(
                            new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), "DOMAIN")
                    );
                    try (DiskShare share = (DiskShare) session.connectShare(SHARE_NAME)) {
                        String filePath = FOLDER_PATH + fileName;
                        try (com.hierynomus.smbj.share.File smbFile = share.openFile(
                                filePath,
                                EnumSet.of(AccessMask.GENERIC_READ),
                                null,
                                SMB2ShareAccess.ALL,
                                SMB2CreateDisposition.FILE_OPEN,
                                null)) {

                            try (InputStream is = smbFile.getInputStream();
                                 OutputStream os = new FileOutputStream(tempFile)) {
                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    os.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                    }
                }

                // Open the downloaded file
                runOnUiThread(() -> launchPdf(tempFile.getAbsolutePath(), fileName));

            } catch (Exception e) {
                Log.e("PDF_OPEN", "Error opening PDF", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Hiba a PDF megnyitásakor", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void launchPdf(String path, String fileName) {
        Toast.makeText(this, "PDF megnyitása: " + fileName, Toast.LENGTH_SHORT).show();

        startActivity(PdfViewerActivity.Companion.launchPdfFromPath(
                this,
                path,
                fileName,
                saveTo.ASK_EVERYTIME,
                true,
                true,
                ToolbarTitleBehavior.SINGLE_LINE_SCROLLABLE,
                CacheStrategy.MAXIMIZE_PERFORMANCE
        ));
    }

    @SuppressLint("SetTextI18n")
    private void updateResults(String query) {
        resultContainer.removeAllViews();
        for (PDF pdf : allPdf) {
            if (pdf.name.toLowerCase().contains(query.toLowerCase())) {
                Button btn = new Button(this);
                btn.setText(pdf.name);
                btn.setPadding(30, 30, 30, 30);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 30);

                btn.setLayoutParams(params);
                btn.setTextSize(25);
                btn.setBackgroundResource(R.drawable.rounded_edittext);
                btn.setTextAlignment(android.view.View.TEXT_ALIGNMENT_VIEW_START);
                btn.setTextColor(Color.BLACK);
                btn.setElevation(1);

                btn.setOnClickListener(v -> openPdfFromNas(pdf.name));
                resultContainer.addView(btn);
            }
        }
    }
}