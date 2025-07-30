package hu.unicon.szakdoga;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private String SERVER_IP;
    private String SHARE_NAME;
    private String FOLDER_PATH;
    private String USERNAME;
    private String PASSWORD;


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

        loadJson();

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
    //**************************************************Loading JSON**************************************************\\
    private void loadJson() {
        try (InputStream inputStream = getAssets().open("csv_config.json")) {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            USERNAME = jsonObject.getString("username");
            SERVER_IP = jsonObject.getString("nas_ip");
            PASSWORD = jsonObject.getString("password");
            SHARE_NAME = jsonObject.getString("share_name");
            FOLDER_PATH = jsonObject.getString("file_path");

        } catch (Exception e) {
            throw new RuntimeException("Hiba a config.json beolvasásakor", e);
        }
    }

    //**************************************************Going to MaterialSearch**************************************************\\
    public void loggingIn(View view) {
       showCsvSelectionDialog();
    }

    //**************************************************Going to PDFOpen**************************************************\\
    public void pdfOpen(View view) {
        Intent intent = new Intent(MainActivity.this, PDFOpen.class);
        startActivity(intent);

    }
    //**************************************************POP-UP Dialog for CSVs**************************************************\\
    private void showCsvSelectionDialog() {
        new Thread(() -> {
            SMBClient client = new SMBClient();

            try (Connection connection = client.connect(SERVER_IP)) {
                Session session = connection.authenticate(
                        new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), "")
                );

                try (DiskShare share = (DiskShare) session.connectShare(SHARE_NAME)) {

                    List<String> csvFiles = new ArrayList<>();
                    for (com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation fileInfo : share.list(FOLDER_PATH)) {
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Válassz CSV fájlt");


                        builder.setItems(csvFiles.toArray(new String[0]), (dialog, which) -> {
                            String selectedFile = csvFiles.get(which);

                            Intent intent = new Intent(MainActivity.this, MaterialSearch.class);
                            intent.putExtra("CSV_FILE_NAME", selectedFile);
                            startActivity(intent);
                        });

                        builder.setNegativeButton("Mégse", null);
                        builder.show();
                    });

                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Hiba a fájlok betöltésekor: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
            }

        }).start();
    }

}