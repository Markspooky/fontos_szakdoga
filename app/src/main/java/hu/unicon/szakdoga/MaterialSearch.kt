package hu.unicon.szakdoga

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import java.util.EnumSet
import java.util.Locale

class MaterialSearch : AppCompatActivity() {
    private var searchEditText: EditText? = null
    private var resultContainer: LinearLayout? = null

    private var res = ArrayList<Cerna>()

    private var SERVER_IP: String? = null
    private var SHARE_NAME: String? = null
    private var CERNA_PATH: String? = null
    private var USERNAME: String? = null
    private var PASSWORD: String? = null

    internal data class Cerna(val name: String?, val prettyDetails: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_material_search)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SERVER_IP = intent.getStringExtra("NAS_IP")
        USERNAME = intent.getStringExtra("USERNAME")
        PASSWORD = intent.getStringExtra("PASSWORD")
        SHARE_NAME = intent.getStringExtra("SHARE_NAME")
        CERNA_PATH = intent.getStringExtra("CERNA_PATH")
        val fileName = intent.getStringExtra("CSV_FILE_NAME")

        if (fileName == null || SERVER_IP == null || USERNAME == null || PASSWORD == null || SHARE_NAME == null || CERNA_PATH == null) {
            Toast.makeText(this, "Hiányos konfiguráció", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val fullCernaPath = if (CERNA_PATH!!.endsWith("/")) CERNA_PATH + fileName else "$CERNA_PATH/$fileName"

        val clearButton = findViewById<ImageView>(R.id.clearButton)
        searchEditText = findViewById(R.id.searchEditText)
        resultContainer = findViewById(R.id.resultContainer)

        updateResults("")

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable) {
                updateResults(s.toString())
            }
        })

        clearButton.setOnClickListener {
            searchEditText?.setText("")
        }

        loadCsvFromNas(fullCernaPath, object : CsvFileCallback {
            override fun onCsvLoaded(csvContent: String?) {
                updateResults(searchEditText?.text.toString())
            }

            override fun onError(e: Exception?) {
                Log.d("DEBUG_PATH", "CSV full path: $fullCernaPath")
                Toast.makeText(this@MaterialSearch, "Hiba a CSV betöltésekor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCsvFromNas(csvFullPath: String?, callback: CsvFileCallback) {
        Thread {
            val client = SMBClient()
            try {
                client.connect(SERVER_IP).use { connection ->
                    val session = connection.authenticate(
                        AuthenticationContext(USERNAME, PASSWORD?.toCharArray(), "")
                    )
                    (session.connectShare(SHARE_NAME) as DiskShare).use { share ->
                        share.openFile(
                            csvFullPath,
                            EnumSet.of(AccessMask.GENERIC_READ),
                            null,
                            SMB2ShareAccess.ALL,
                            SMB2CreateDisposition.FILE_OPEN,
                            null
                        ).use { smbFile ->
                            smbFile.inputStream.use { inputStream ->
                                val csvContent = inputStream.bufferedReader().readText()
                                
                                runOnUiThread {
                                    val lines = csvContent.split(Regex("\\r?\\n")).filter { it.isNotBlank() }
                                    if (lines.isNotEmpty()) {
                                        val header = lines[0].split(",").map { it.trim() }
                                        for (i in 1 until lines.size) {
                                            val values = lines[i].split(",").map { it.trim() }
                                            val builder = StringBuilder()
                                            var j = 0
                                            while (j < header.size && j < values.size) {
                                                builder.append("\n${header[j]}:\n${values[j]}\n")
                                                j++
                                            }
                                            res.add(Cerna(values.getOrNull(0), builder.toString()))
                                        }
                                    }
                                    callback.onCsvLoaded(csvContent)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Log.e("CSV_LOAD", "Hiba történt a fájl betöltésekor", e)
                    callback.onError(e)
                }
            }
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun updateResults(query: String) {
        resultContainer?.removeAllViews()
        val upperQuery = query.uppercase(Locale.getDefault())
        var count = 0
        for (cerna in res) {
            if (cerna.prettyDetails.uppercase(Locale.getDefault()).contains(upperQuery)) {
                val textView = TextView(this).apply {
                    setTextColor(resources.getColor(R.color.white, null))
                    text = cerna.prettyDetails
                    setPadding(30, 30, 30, 30)
                    textSize = 25f
                    setBackgroundResource(R.drawable.rounded_edittext)
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                    elevation = 1f
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 30)
                    }
                }
                resultContainer?.addView(textView)
                count++
            }
            if (count > 100) break
        }
    }
}
