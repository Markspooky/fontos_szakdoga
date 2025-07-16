package com.example.szakdoga;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rajat.pdfviewer.PdfViewerActivity;
import com.rajat.pdfviewer.util.CacheStrategy;
import com.rajat.pdfviewer.util.ToolbarTitleBehavior;
import com.rajat.pdfviewer.util.saveTo;

public class CustomPDFViewer extends AppCompatActivity {

    public static final String EXTRA_PDF_PATH = "extra_pdf_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_pdfviewer); // Ez a layout kell!

        String pdfPath = getIntent().getStringExtra(EXTRA_PDF_PATH);

        if (pdfPath != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.pdf_fragment_container, PdfRendererFragment.newInstance(pdfPath))
                    .commit();
            launchPdf(pdfPath,"asd");
        }

    }
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
}
