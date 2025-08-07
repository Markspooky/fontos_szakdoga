package hu.unicon.szakdoga;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
    private String NAME;

    private String SERVER_IP;
    private String SHARE_NAME;
    private String CERNA_PATH;
    private String PDF_PATH;
    private String USERNAME;
    private String PASSWORD;
    private static final int REQUEST_CODE_IMPORT_JSON = 2001;
    Button configButton;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configButton = findViewById(R.id.load_config_button);


        View title = findViewById(R.id.title);
        View belepes = findViewById(R.id.belepes);
        View pdfek = findViewById(R.id.pdfek);

        Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);
        Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);
        Animation anim3 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);

        anim2.setStartOffset(150);
        anim3.setStartOffset(300);

        title.startAnimation(anim1);
        belepes.startAnimation(anim2);
        pdfek.startAnimation(anim3);
    }


    //********************JSON Importing**************************\\

    public void menuPoint(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQUEST_CODE_IMPORT_JSON);
    }

    //*************Checks for codes when importing a JSON*************\\
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMPORT_JSON && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                importJsonFromUri(uri);
            }
        }
    }

    //***************Saves every JSON file as config.json****************\\
    private void importJsonFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            assert inputStream != null;
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            String jsonContent = new String(buffer, StandardCharsets.UTF_8);
            new JSONArray(jsonContent);

            FileOutputStream fos = openFileOutput("config.json", MODE_PRIVATE);
            fos.write(buffer);
            fos.close();

            Toast.makeText(this, "Konfigurációs fájl importálva", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Hiba az importálás során: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    //********************Loading the configuration**************************\\

    public void selectConfigFromJsonFile(View view) {
        String fileName = "config.json";

        try {
            FileInputStream fis = openFileInput(fileName);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            List<String> configNames = new ArrayList<>();
            List<JSONObject> configObjects = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                configNames.add(obj.optString("name", "Névtelen " + i));
                configObjects.add(obj);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Válassz konfigurációt");


            builder.setItems(configNames.toArray(new String[0]), (dialog, which) -> {
                JSONObject selectedConfig = configObjects.get(which);
                applyConfig(selectedConfig, fileName);
            });

            builder.setNegativeButton("Mégse", null);
            builder.show();

        } catch (Exception e) {
            Toast.makeText(this, "Hiba a konfiguráció betöltésekor", Toast.LENGTH_SHORT).show();
        }
    }
//*****************This applies the current JSON file as the configuration******************\\
    @SuppressLint("SetTextI18n")
    private void applyConfig(JSONObject config, String fileName) {
        try {
            NAME = config.getString("name");
            USERNAME = config.getString("username");
            PASSWORD = config.getString("password");
            SERVER_IP = config.getString("nas_ip");
            SHARE_NAME = config.getString("share_name");
            CERNA_PATH = config.getString("cerna_path");
            PDF_PATH = config.getString("pdf_path");
//            PR4_PATH = config.getString("pr4_path");

            SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("active_file", fileName);
            editor.putString("active_name", config.getString("name"));
            editor.apply();

            Toast.makeText(this, "Konfiguráció betöltve: " + config.getString("name"), Toast.LENGTH_SHORT).show();
            configButton.setText(NAME);

        } catch (Exception e) {
            Toast.makeText(this, "Nem sikerült betölteni a konfigurációt", Toast.LENGTH_SHORT).show();
        }
    }
    public void loggingIn(View view) {
        configurationApplier();
    }

    private void configurationApplier() {
        if (SERVER_IP == null || USERNAME == null || PASSWORD == null || SHARE_NAME == null || CERNA_PATH == null) {
            Toast.makeText(this, "Előbb válassz konfigurációt!", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(() -> {
            SMBClient client = new SMBClient();

            try (Connection connection = client.connect(SERVER_IP)) {
                Session session = connection.authenticate(
                        new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), "")
                );

                try (DiskShare share = (DiskShare) session.connectShare(SHARE_NAME)) {

                    List<String> csvFiles = new ArrayList<>();
                    for (com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation fileInfo : share.list(CERNA_PATH)) {
                        String fileName = fileInfo.getFileName();
                        if (fileName.toLowerCase().endsWith(".csv")) {
                            csvFiles.add(fileName);
                        }
                    }

                    runOnUiThread(() -> {
                        if (csvFiles.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Nincsenek CSV fájlok a mappában", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String selectedFile = csvFiles.get(0);
                        Intent intent = new Intent(MainActivity.this, MaterialSearch.class);
                        intent.putExtra("NAS_IP", SERVER_IP);
                        intent.putExtra("USERNAME", USERNAME);
                        intent.putExtra("PASSWORD", PASSWORD);
                        intent.putExtra("SHARE_NAME", SHARE_NAME);
                        intent.putExtra("CERNA_PATH", CERNA_PATH);
                        intent.putExtra("CSV_FILE_NAME", selectedFile);
                        startActivity(intent);
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Hiba a NAS elérésekor: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Hiba a fájlok betöltésekor: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

        }).start();
    }


    //***********************Going to the PDF page*****************************\\

    public void pdfOpen(View view) {
        if (SERVER_IP == null || USERNAME == null || PASSWORD == null || SHARE_NAME == null || PDF_PATH == null) {
            Toast.makeText(this, "Előbb válassz konfigurációt!", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(MainActivity.this, PDFOpen.class);
        intent.putExtra("NAME",NAME);
        intent.putExtra("NAS_IP", SERVER_IP);
        intent.putExtra("USERNAME", USERNAME);
        intent.putExtra("PASSWORD", PASSWORD);
        intent.putExtra("SHARE_NAME", SHARE_NAME);
        intent.putExtra("PDF_PATH", PDF_PATH);
        startActivity(intent);
    }


}
