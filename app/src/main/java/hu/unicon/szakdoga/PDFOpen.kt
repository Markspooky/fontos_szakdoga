package hu.unicon.szakdoga

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alamin5g.pdf.scroll.DefaultScrollHandle
import com.alamin5g.pdf.PDFView.FitPolicy
import hu.unicon.szakdoga.databinding.PdfopenPageBinding

class PDFOpen : AppCompatActivity() {
    private lateinit var binding: PdfopenPageBinding

    // Oracle DB metaadatok (az SMB konfiguráció helyett)
    private var documentId: Long = -1
    private var documentTitle: String? = null

    @SuppressLint("Range")
    private val filePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    // Engedély kérése a fájl tartós eléréséhez
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Log.e("PDF", "Persistable permission failed", e)
                }
                displayPdf(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = PdfopenPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Adatok átvétele az Oracle DB-ből érkező Intent-ből
        documentId = intent.getLongExtra("PDF_ID", -1)
        documentTitle = intent.getStringExtra("PDF_TITLE")

        supportActionBar?.title = documentTitle ?: "PDF Megjelenítő"

        setupListeners()

        // Ha kaptunk URI-t (pl. a keresőből egy letöltött fájlra), jelenítsük meg
        intent.getStringExtra("PDF_URI")?.let {
            displayPdf(Uri.parse(it))
        }
    }

    private fun setupListeners() {
        binding.searchPdf.setOnClickListener {
            pdfSearch()
        }

        binding.pickPdfButton.setOnClickListener {
            launchFilePicker()
        }
    }

    private fun pdfSearch() {
        // A kereső megnyitása - már nem kell átadni SMB adatokat
        val intent = Intent(this, SearchPDF::class.java)
        startActivity(intent)
    }

    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        filePicker.launch(intent)
    }

    private fun displayPdf(uri: Uri) {
        // BartekSC PDFView konfigurálása (3.x verzió szerinti API)
        binding.pdfView.fromUri(uri)
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .enableAntialiasing(true)
            .scrollHandle(DefaultScrollHandle(this))
            .pageFitPolicy(FitPolicy.WIDTH)
            .spacing(10)
            .onLoad { nbPages ->
                Log.i("PDF", "Sikeres betöltés: $nbPages oldal")
            }
            .onError { t ->
                Log.e("PDF", "Hiba a megjelenítéskor", t)
                Toast.makeText(this, "Hiba a PDF megnyitásakor!", Toast.LENGTH_SHORT).show()
            }
            .load()
    }
}
