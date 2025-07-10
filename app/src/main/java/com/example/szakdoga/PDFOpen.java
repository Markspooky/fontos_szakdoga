package com.example.szakdoga;

import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.szakdoga.databinding.PdfopenPageBinding;
import com.rajat.pdfviewer.PdfRendererView;
import com.rajat.pdfviewer.PdfViewerActivity;
import com.rajat.pdfviewer.util.CacheStrategy;
import com.rajat.pdfviewer.util.ToolbarTitleBehavior;
import com.rajat.pdfviewer.util.saveTo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PDFOpen extends AppCompatActivity {

    private PdfopenPageBinding binding;

    private final List<String> pdfList = Arrays.asList(
            "https://css4.pub/2015/usenix/example.pdf",
            "https://research.nhm.org/pdfs/10840/10840.pdf",
            "http://192.168.0.72:8001/pw.pdf",
            "https://css4.pub/2017/newsletter/drylab.pdf",
            "https://css4.pub/2015/textbook/somatosensory.pdf"
    );

    private final ActivityResultLauncher<Intent> filePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            final int takeFlags = result.getData().getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Engedély nem sikerült!", Toast.LENGTH_SHORT).show();
                        }
                        launchPdfFromUri(uri.toString());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Binding
        binding = PdfopenPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("PDF Viewer");
        }

        setupListeners();
    }

    private void setupListeners() {
        binding.onlinePdf.setOnClickListener(v -> {
            setupPdfStatusListener();
            launchPdfFromUrl(pdfList.get(3));
        });

        binding.pickPdfButton.setOnClickListener(v -> launchFilePicker());
    }

    private void setupPdfStatusListener() {
        binding.pdfView.setStatusListener(new PdfRendererView.StatusCallBack() {
            @Override
            public void onPdfLoadStart() {
                Log.i("PDF Status", "Loading started");
            }

            @Override
            public void onPdfLoadProgress(int progress, long downloadedBytes, Long totalBytes) {
                Log.i("PDF Status", "Download progress: " + progress + "%");
            }

            @Override
            public void onPdfLoadSuccess(@NonNull String absolutePath) {
                Log.i("PDF Status", "Load successful: " + absolutePath);
            }

            @Override
            public void onError(@NonNull Throwable error) {
                Log.e("PDF Status", "Error loading PDF: " + error.getMessage());
            }

            @Override
            public void onPageChanged(int currentPage, int totalPage) {
                Log.i("PDF Status", "Page changed: " + currentPage + " / " + totalPage);
            }

            @Override
            public void onPdfRenderStart() {
                Log.d("PDF Status", "Render started");
            }

            @Override
            public void onPdfRenderSuccess() {
                Log.d("PDF Status", "Render successful");
                binding.pdfView.jumpToPage(2, true, 2000);
            }
        });

        binding.pdfView.setZoomListener((isZoomedIn, scale) ->
                Log.i("PDF Zoom", "Zoomed in: " + isZoomedIn + ", Scale: " + scale));
    }

    private void launchPdfFromUrl(String url) {
        Map<String, String> headerData = Collections.emptyMap(); // ha nincs szükséged headerre

        Toast.makeText(this, "Opening PDF: " + url, Toast.LENGTH_SHORT).show();

        startActivity(PdfViewerActivity.Companion.launchPdfFromUrl(
                this,
                url,
                "PDF Title",
                saveTo.ASK_EVERYTIME, // A MŰKÖDŐ OPCIÓ
                true,
                true,
                headerData,
                ToolbarTitleBehavior.SINGLE_LINE_SCROLLABLE,
                CacheStrategy.MAXIMIZE_PERFORMANCE
        ));
    }

    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/pdf");
        filePicker.launch(intent);
    }

    private void launchPdfFromUri(String uri) {
        startActivity(PdfViewerActivity.Companion.launchPdfFromPath(
                this,
                uri,
                "Picked PDF",
                saveTo.ASK_EVERYTIME,
                false,
                true,
                ToolbarTitleBehavior.SINGLE_LINE_SCROLLABLE,
                CacheStrategy.MAXIMIZE_PERFORMANCE
        ));
    }
}
