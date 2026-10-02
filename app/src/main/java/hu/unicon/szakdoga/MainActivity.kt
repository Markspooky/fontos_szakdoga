package hu.unicon.szakdoga

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hu.unicon.szakdoga.api.ApiClient

class MainActivity : AppCompatActivity() {
    private var configButton: Button? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configButton = findViewById(R.id.load_config_button)
        updateConfigButtonText()

        val title = findViewById<View>(R.id.title)
        val belepes = findViewById<View>(R.id.belepes)
        val pdfek = findViewById<View>(R.id.pdfek)

        val anim1 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up)
        val anim2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up).apply { startOffset = 150 }
        val anim3 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up).apply { startOffset = 300 }

        title.startAnimation(anim1)
        belepes.startAnimation(anim2)
        pdfek.startAnimation(anim3)
    }

    private fun updateConfigButtonText() {
        val currentUrl = ApiClient.getBaseUrl(this)
        configButton?.text = "API: $currentUrl"
    }

    fun menuPoint(view: View?) {
        showServerUrlDialog()
    }

    fun selectConfigFromJsonFile(view: View?) {
        showServerUrlDialog()
    }

    private fun showServerUrlDialog() {
        val currentUrl = ApiClient.getBaseUrl(this)
        val input = EditText(this).apply {
            setText(currentUrl)
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Oracle DB REST API Cím")
            .setMessage("Módosítsd az API szerver elérési útvonalát:")
            .setView(input)
            .setPositiveButton("Mentés") { _, _ ->
                val newUrl = input.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    ApiClient.setBaseUrl(this, newUrl)
                    updateConfigButtonText()
                    Toast.makeText(this, "API URL elmentve!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Mégse", null)
            .show()
    }

    fun loggingIn(view: View?) {
        val intent = Intent(this, MaterialSearch::class.java)
        startActivity(intent)
    }

    fun pdfOpen(view: View?) {
        val intent = Intent(this, PDFOpen::class.java)
        startActivity(intent)
    }
}
