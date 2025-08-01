package hu.unicon.szakdoga;


import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import hu.unicon.szakdoga.databinding.PdfopenPageBinding;
import com.rajat.pdfviewer.PdfRendererView;
import com.rajat.pdfviewer.PdfViewerActivity;
import com.rajat.pdfviewer.util.CacheStrategy;
import com.rajat.pdfviewer.util.ToolbarTitleBehavior;
import com.rajat.pdfviewer.util.saveTo;

import java.io.File;

public class PDFOpen extends AppCompatActivity {

    private PdfopenPageBinding binding;


    //A letoltesek mappaban turkalo filekereso
    @SuppressLint("Range")
    private final ActivityResultLauncher<Intent> filePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    assert uri != null;
                    String uriString = uri.toString();
                    File myFile = new File(uriString);
                    String path = myFile.getAbsolutePath();
                    String displayName = null;
                    try {
                        final int takeFlags = result.getData().getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                        //A pdf megnyitas engedelyezese
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                        if (uriString.startsWith("content://")) {
                            Cursor cursor = null;
                            try {
                                cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                                if (cursor != null && cursor.moveToFirst()) {
                                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                }
                            } finally {
                                assert cursor != null;
                                cursor.close();
                            }
                        } else if (uriString.startsWith("file://")) {
                            displayName = myFile.getName();
                        }
                    } catch (SecurityException e) {
                        Toast.makeText(this, "Engedély nem sikerült!", Toast.LENGTH_SHORT).show();
                    }
                    launchPdfFromUri(uri.toString(),displayName );
                }
            }
    );

    private Context getActivity() {
        return this;
    }


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

        // Edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Binding
        binding = PdfopenPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("PDF Megjelenito");
        }

      setupListeners();
    }

    private void setupListeners() {
        binding.searchPdf.setOnClickListener(v -> {
            setupPdfStatusListener();
            pdfSearch(v);
        });

        binding.pickPdfButton.setOnClickListener(v -> launchFilePicker());
    }

    public void pdfSearch(View view) {
        Intent intent = new Intent(PDFOpen.this, SearchPDF.class);
        startActivity(intent);
    }

    private void setupPdfStatusListener() {
        binding.pdfView.setStatusListener(new PdfRendererView.StatusCallBack() {
            @Override
            public void onPdfLoadStart() {
                Log.i("PDF Statusz", "Betoltes");
            }

            @Override
            public void onPdfLoadProgress(int progress, long downloadedBytes, Long totalBytes) {
                Log.i("PDF Statusz", "Letoltes folyamatban:" + progress + "%");
            }

            @Override
            public void onPdfLoadSuccess(@NonNull String absolutePath) {
                Log.i("PDF Statusz", "Sikeres betoltes: " + absolutePath);
            }

            @Override
            public void onError(@NonNull Throwable error) {
                Log.e("PDF Statusz", "Hiba a PDF betoltesekor: " + error.getMessage());
            }

            @Override
            public void onPageChanged(int currentPage, int totalPage) {
                Log.i("PDF Statusz", "PDF ablak megvaltoztatva: " + currentPage + " / " + totalPage);
            }

            @Override
            public void onPdfRenderStart() {
                Log.d("PDF Statusz", "Renderereles megkezdve");
            }

            @Override
            public void onPdfRenderSuccess() {
                Log.d("PDF Statusz", "Sikeres rendereles");
                binding.pdfView.jumpToPage(2, true, 2000);
            }
        });

        binding.pdfView.setZoomListener((isZoomedIn, scale) ->
                Log.i("PDF Nagyitas", "Belenagyitva: " + isZoomedIn + ", Meret: " + scale));
    }

    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/pdf");
        filePicker.launch(intent);
    }

    private void launchPdfFromUri(String uri, String name) {
        startActivity(PdfViewerActivity.Companion.launchPdfFromPath(
                this,
                uri,
                name,
                saveTo.ASK_EVERYTIME,
                false,
                true,
                ToolbarTitleBehavior.SINGLE_LINE_SCROLLABLE,
                CacheStrategy.MAXIMIZE_PERFORMANCE
        ));
    }
}
