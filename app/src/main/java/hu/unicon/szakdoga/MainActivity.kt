package hu.unicon.szakdoga

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var NAME: String? = null
    private var SERVER_IP: String? = null
    private var SHARE_NAME: String? = null
    private var CERNA_PATH: String? = null
    private var PDF_PATH: String? = null
    private var USERNAME: String? = null
    private var PASSWORD: String? = null
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

    fun menuPoint(view: View?) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, REQUEST_CODE_IMPORT_JSON)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMPORT_JSON && resultCode == RESULT_OK) {
            data?.data?.let { importJsonFromUri(it) }
        }
    }

    private fun importJsonFromUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = inputStream.readBytes()
                val jsonContent = String(buffer, StandardCharsets.UTF_8)
                JSONArray(jsonContent) // Validate JSON

                openFileOutput("config.json", MODE_PRIVATE).use { fos ->
                    fos.write(buffer)
                }
                Toast.makeText(this, "Konfigurációs fájl importálva", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Hiba az importálás során: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun selectConfigFromJsonFile(view: View?) {
        val fileName = "config.json"
        try {
            openFileInput(fileName).use { fis ->
                val buffer = fis.readBytes()
                val json = String(buffer, StandardCharsets.UTF_8)
                val jsonArray = JSONArray(json)

                val configNames = mutableListOf<String>()
                val configObjects = mutableListOf<JSONObject>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    configNames.add(obj.optString("name", "Névtelen $i"))
                    configObjects.add(obj)
                }

                AlertDialog.Builder(this)
                    .setTitle("Válassz konfigurációt")
                    .setItems(configNames.toTypedArray()) { _, which ->
                        applyConfig(configObjects[which], fileName)
                    }
                    .setNegativeButton("Mégse", null)
                    .show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Hiba a konfiguráció betöltésekor", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun applyConfig(config: JSONObject, fileName: String) {
        try {
            NAME = config.getString("name")
            USERNAME = config.getString("username")
            PASSWORD = config.getString("password")
            SERVER_IP = config.getString("nas_ip")
            SHARE_NAME = config.getString("share_name")
            CERNA_PATH = config.getString("cerna_path")
            PDF_PATH = config.getString("pdf_path")

            getSharedPreferences("config", MODE_PRIVATE).edit().apply {
                putString("active_file", fileName)
                putString("active_name", NAME)
                apply()
            }

            Toast.makeText(this, "Konfiguráció betöltve: $NAME", Toast.LENGTH_SHORT).show()
            configButton?.text = NAME
        } catch (e: Exception) {
            Toast.makeText(this, "Nem sikerült betölteni a konfigurációt", Toast.LENGTH_SHORT).show()
        }
    }

    fun loggingIn(view: View?) {
        configurationApplier()
    }

    private fun configurationApplier() {
        if (SERVER_IP == null || USERNAME == null || PASSWORD == null || SHARE_NAME == null || CERNA_PATH == null) {
            Toast.makeText(this, "Előbb válassz konfigurációt!", Toast.LENGTH_LONG).show()
            return
        }

        Thread {
            val client = SMBClient()
            try {
                client.connect(SERVER_IP).use { connection ->
                    val session = connection.authenticate(
                        AuthenticationContext(USERNAME, PASSWORD?.toCharArray(), "")
                    )
                    (session.connectShare(SHARE_NAME) as DiskShare).use { share ->
                        val csvFiles = mutableListOf<String>()
                        for (fileInfo in share.list(CERNA_PATH)) {
                            val fileName = fileInfo.fileName
                            if (fileName.lowercase(Locale.getDefault()).endsWith(".csv")) {
                                csvFiles.add(fileName)
                            }
                        }
                        runOnUiThread {
                            if (csvFiles.isEmpty()) {
                                Toast.makeText(this, "Nincsenek CSV fájlok a mappában", Toast.LENGTH_SHORT).show()
                                return@runOnUiThread
                            }
                            val selectedFile = csvFiles[0]
                            val intent = Intent(this, MaterialSearch::class.java).apply {
                                putExtra("NAS_IP", SERVER_IP)
                                putExtra("USERNAME", USERNAME)
                                putExtra("PASSWORD", PASSWORD)
                                putExtra("SHARE_NAME", SHARE_NAME)
                                putExtra("CERNA_PATH", CERNA_PATH)
                                putExtra("CSV_FILE_NAME", selectedFile)
                            }
                            startActivity(intent)
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Hiba a fájlok betöltésekor: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    fun pdfOpen(view: View?) {
        if (SERVER_IP == null || USERNAME == null || PASSWORD == null || SHARE_NAME == null || PDF_PATH == null) {
            Toast.makeText(this, "Előbb válassz konfigurációt!", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(this, PDFOpen::class.java).apply {
            putExtra("NAME", NAME)
            putExtra("NAS_IP", SERVER_IP)
            putExtra("USERNAME", USERNAME)
            putExtra("PASSWORD", PASSWORD)
            putExtra("SHARE_NAME", SHARE_NAME)
            putExtra("PDF_PATH", PDF_PATH)
        }
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_CODE_IMPORT_JSON = 2001
    }
}
