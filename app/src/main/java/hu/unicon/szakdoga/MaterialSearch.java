package hu.unicon.szakdoga;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;

public class MaterialSearch extends AppCompatActivity {

    private String NAME;
    EditText searchEditText;
    LinearLayout resultContaier;


    ArrayList<Cerna> res = new ArrayList<>();

    private String SERVER_IP;
    private String SHARE_NAME;
    private String FILE_PATH;
    private String USERNAME;
    private String PASSWORD;


    static class Cerna {
        String name;
        String prettyDetails;

        Cerna(String name, String prettyDetails) {
            this.name = name;
            this.prettyDetails = prettyDetails;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_material_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SERVER_IP = getIntent().getStringExtra("NAS_IP");
        USERNAME = getIntent().getStringExtra("USERNAME");
        PASSWORD = getIntent().getStringExtra("PASSWORD");
        SHARE_NAME = getIntent().getStringExtra("SHARE_NAME");
        FILE_PATH = getIntent().getStringExtra("FILE_PATH");
        String fileName = getIntent().getStringExtra("CSV_FILE_NAME");

        if (fileName == null || SERVER_IP == null || USERNAME == null || PASSWORD == null || SHARE_NAME == null || FILE_PATH == null) {
            Toast.makeText(this, "Hiányos konfiguráció", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FILE_PATH = FILE_PATH + fileName;


        ImageView clearButton = findViewById(R.id.clearButton);
        searchEditText = findViewById(R.id.searchEditText);
        resultContaier = findViewById(R.id.resultContainer);


        updateResults("");


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {
                updateResults(s.toString());
            }
        });
//**************************************************X button deletes the searchEditText's contents**************************************************\\
        clearButton.setOnClickListener(v -> {
            searchEditText.setText("");
        });

        loadCsvFromNas(FILE_PATH, new CsvFileCallback() {
            public void onCsvLoaded(String csvContent) {
                updateResults(searchEditText.getText().toString());
            }

            public void onError(Exception e) {

                Toast.makeText(MaterialSearch.this, "Hiba a CSV betöltésekor", Toast.LENGTH_SHORT).show();
            }
        });

    }
    //**************************************************Custom interface for CSV Files to callback**************************************************\\
    interface CsvFileCallback {
        void onCsvLoaded(String csvContent);
        void onError(Exception e);
    }


    //**************************************************Loads CSV from NAS**************************************************\\
    private void loadCsvFromNas(String csvFullPath, CsvFileCallback callback) {
        new Thread(() -> {
            SMBClient client = new SMBClient();

            try (Connection connection = client.connect(SERVER_IP)) {
                Session session = (connection).authenticate(
                        new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), "")
                );

                try (DiskShare share = (DiskShare) session.connectShare(SHARE_NAME)) {
                    try (com.hierynomus.smbj.share.File smbFile = share.openFile(
                            csvFullPath,
                            EnumSet.of(AccessMask.GENERIC_READ),
                            null,
                            SMB2ShareAccess.ALL,
                            SMB2CreateDisposition.FILE_OPEN,
                            null)) {

                        try (InputStream is = smbFile.getInputStream()) {
                            StringBuilder csvContent = new StringBuilder();
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                csvContent.append(new String(buffer, 0, bytesRead));
                            }

                            runOnUiThread(() -> {
                                String[] lines = csvContent.toString().split("\\r?\\n");
                                String[] header = lines[0].split(",");

                                for (int i = 1; i < lines.length; i++) {

                                    String[] values = lines[i].split(",");
                                    StringBuilder builder = new StringBuilder();
                                    for (int j = 0; j < header.length && j < values.length; j++) {
                                        builder.append("\n" +header[j].trim()).append(":\n").append(values[j].trim()).append("\n");
                                    }
                                    res.add(new Cerna(values[0], builder.toString()));

                                }

                            });

                            runOnUiThread(() -> {
                                callback.onCsvLoaded(csvContent.toString());
                            });

                        }
                    }
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Log.e("CSV_LOAD", "Hiba történt a fájl betöltésekor", e);
                    callback.onError(e);
                });
            }

        }).start();
    }

    //**************************************************Dynamic search within the file**************************************************\\
    @SuppressLint("SetTextI18n")
    private void updateResults(String query) {
        resultContaier.removeAllViews();
        for(Cerna cerna : res) {
            if (cerna.prettyDetails.toUpperCase().contains(query.toUpperCase())) {
                TextView btn = new TextView(this);
                btn.setText(cerna.prettyDetails);
                btn.setPadding(30,30,30,30);
                LinearLayout.LayoutParams vau = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                btn.setTextSize(25);
                btn.setBackgroundResource(R.drawable.rounded_edittext);
                btn.setTextAlignment(ViewGroup.TEXT_ALIGNMENT_VIEW_START);
                btn.setTextColor(Color.BLACK);
                btn.setElevation(1);
                vau.setMargins(0,0,0,30);
                btn.setLayoutParams(vau);
                resultContaier.addView(btn);

            }
         if (resultContaier.getChildCount() > 100) {break;}
        }
    }

}

