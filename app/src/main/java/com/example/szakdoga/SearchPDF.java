package com.example.szakdoga;

import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.rajat.pdfviewer.PdfViewerActivity;
import com.rajat.pdfviewer.util.CacheStrategy;
import com.rajat.pdfviewer.util.ToolbarTitleBehavior;
import com.rajat.pdfviewer.util.saveTo;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;



public class SearchPDF extends AppCompatActivity {

    EditText searchEditText;
    LinearLayout resultContaier;

    static class Asd {
        String url,name;
        Asd(String url,String name) {
        this.url = url;
        this.name = name;
        }
    }

    List<Asd> allPdf;


//    @SuppressLint("Range")
//    private final ActivityResultLauncher<Intent> filePicker = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                    Uri uri = result.getData().getData();
//                    assert uri != null;
//                    String uriString = uri.toString();
//                    File myFile = new File(uriString);
//                    String path = myFile.getAbsolutePath();
//                    String displayName = null;
//                    try {
//                        final int takeFlags = result.getData().getFlags()
//                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
//                        //A pdf megnyitas engedelyezese
//                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
//                        if (uriString.startsWith("content://")) {
//                            Cursor cursor = null;
//                            try {
//                                cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
//                                if (cursor != null && cursor.moveToFirst()) {
//                                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                                }
//                            } finally {
//                                assert cursor != null;
//                                cursor.close();
//                            }
//                        } else if (uriString.startsWith("file://")) {
//                            displayName = myFile.getName();
//                        }
//                    } catch (SecurityException e) {
//                        e.printStackTrace();
//                        Toast.makeText(this, "Engedély nem sikerült!", Toast.LENGTH_SHORT).show();
//                    }
//                    launchPdfFromUrl(uri.toString(),displayName );
//                }
//            }
//    );

    private Context getActivity() {
        return this;
    }


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
        resultContaier = findViewById(R.id.resultContainer);

        allPdf = Arrays.asList(
                new Asd("https://morth.nic.in/sites/default/files/dd12-13_0.pdf","pdf1"),
                new Asd("https://morth.nic.in/sites/default/files/dd12-13_0.pdf","pdf2"),
                new Asd("https://morth.nic.in/sites/default/files/dd12-13_0.pdf","pdf3")
        );

        updateResults("");


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

    }

    private void launchPdfFromUrl(String url,String name) {
        Map<String, String> headerData = Collections.emptyMap();

        Toast.makeText(this, "PDF Megnyitasa: " + url, Toast.LENGTH_SHORT).show();

        startActivity(PdfViewerActivity.Companion.launchPdfFromUrl(
                this,
                url,
                name,
                saveTo.ASK_EVERYTIME,
                true,
                true,
                headerData,
                ToolbarTitleBehavior.SINGLE_LINE_SCROLLABLE,
                CacheStrategy.MAXIMIZE_PERFORMANCE
        ));
    }

    @SuppressLint("SetTextI18n")
    private void updateResults(String query) {
        resultContaier.removeAllViews();

        for(Asd asd : allPdf) {
            if (asd.name.toLowerCase().contains(query.toLowerCase())) {
                Button btn = new Button(this);
                btn.setText(asd.name);
                btn.setPadding(30,30,30,30);
                LinearLayout.LayoutParams vau = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                btn.setTextSize(25);
                btn.setBackgroundResource(R.drawable.rounded_edittext);
                btn.setTextAlignment(ViewGroup.TEXT_ALIGNMENT_VIEW_START);
                btn.setTextColor(android.graphics.Color.BLACK);
                btn.setElevation(1);
                vau.setMargins(0,0,0,30);
                btn.setLayoutParams(vau);

                resultContaier.addView(btn);

                btn.setOnClickListener(v -> launchPdfFromUrl(asd.url, "asd"));
            }
        }
    }


}

