package hu.unicon.szakdoga

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hu.unicon.szakdoga.api.ApiClient
import hu.unicon.szakdoga.model.Document
import hu.unicon.szakdoga.model.Folder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class SearchPDF : AppCompatActivity() {
    private var searchEditText: EditText? = null
    private var resultContainer: LinearLayout? = null
    private var loader: ProgressBar? = null

    private var currentFolderId: Long? = null
    private val folderStack = mutableListOf<Pair<Long?, String>>()

    private val currentFolders = mutableListOf<Folder>()
    private val currentDocuments = mutableListOf<Document>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_pdf)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loader = findViewById(R.id.loader)
        searchEditText = findViewById(R.id.searchEditText)
        resultContainer = findViewById(R.id.resultContainer)

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    loadCurrentFolderContent()
                } else if (query.length >= 2) {
                    searchDocumentsInApi(query)
                }
            }
        })

        loadCurrentFolderContent()
    }

    private fun loadCurrentFolderContent() {
        loader?.visibility = View.VISIBLE
        val apiService = ApiClient.getService(this)

        apiService.getFolders(currentFolderId).enqueue(object : Callback<List<Folder>> {
            override fun onResponse(call: Call<List<Folder>>, response: Response<List<Folder>>) {
                if (response.isSuccessful) {
                    currentFolders.clear()
                    response.body()?.let { currentFolders.addAll(it) }
                    loadDocumentsInCurrentFolder()
                } else {
                    loader?.visibility = View.GONE
                    Toast.makeText(this@SearchPDF, "Hiba a mappák betöltésekor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Folder>>, t: Throwable) {
                loader?.visibility = View.GONE
                Log.e("SearchPDF", "Hiba a mappák lekérésekor", t)
                Toast.makeText(this@SearchPDF, "Hálózati hiba: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadDocumentsInCurrentFolder() {
        val apiService = ApiClient.getService(this)

        apiService.getDocuments(currentFolderId).enqueue(object : Callback<List<Document>> {
            override fun onResponse(call: Call<List<Document>>, response: Response<List<Document>>) {
                loader?.visibility = View.GONE
                if (response.isSuccessful) {
                    currentDocuments.clear()
                    response.body()?.let { currentDocuments.addAll(it) }
                    displayCurrentDirectory()
                } else {
                    Toast.makeText(this@SearchPDF, "Hiba a dokumentumok betöltésekor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Document>>, t: Throwable) {
                loader?.visibility = View.GONE
                Log.e("SearchPDF", "Hiba a dokumentumok lekérésekor", t)
                Toast.makeText(this@SearchPDF, "Hálózati hiba", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchDocumentsInApi(query: String) {
        loader?.visibility = View.VISIBLE
        val apiService = ApiClient.getService(this)

        apiService.searchDocuments(query).enqueue(object : Callback<List<Document>> {
            override fun onResponse(call: Call<List<Document>>, response: Response<List<Document>>) {
                loader?.visibility = View.GONE
                if (response.isSuccessful) {
                    val searchResults = response.body() ?: emptyList()
                    displaySearchResults(searchResults)
                } else {
                    Toast.makeText(this@SearchPDF, "Hiba a keresés során", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Document>>, t: Throwable) {
                loader?.visibility = View.GONE
                Log.e("SearchPDF", "Hiba a kereséskor", t)
                Toast.makeText(this@SearchPDF, "Hálózati hiba", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun downloadPdfAndOpen(doc: Document) {
        loader?.visibility = View.VISIBLE
        val apiService = ApiClient.getService(this)

        apiService.downloadDocument(doc.id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    Thread {
                        try {
                            val tempFile = File(cacheDir, doc.fileName)
                            response.body()!!.byteStream().use { inputStream ->
                                FileOutputStream(tempFile).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            runOnUiThread {
                                loader?.visibility = View.GONE
                                launchPdfViewer(tempFile.absolutePath, doc.title, doc.id)
                            }
                        } catch (e: Exception) {
                            Log.e("SearchPDF", "Fájl mentési hiba", e)
                            runOnUiThread {
                                loader?.visibility = View.GONE
                                Toast.makeText(this@SearchPDF, "Hiba a fájl mentésekor", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.start()
                } else {
                    loader?.visibility = View.GONE
                    Toast.makeText(this@SearchPDF, "Hiba a PDF letöltésekor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                loader?.visibility = View.GONE
                Log.e("SearchPDF", "Hiba a letöltéskor", t)
                Toast.makeText(this@SearchPDF, "Hálózati hiba letöltéskor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun launchPdfViewer(path: String, title: String, id: Long) {
        val fileUri = Uri.fromFile(File(path)).toString()
        val intent = Intent(this, PDFOpen::class.java).apply {
            putExtra("PDF_URI", fileUri)
            putExtra("PDF_TITLE", title)
            putExtra("PDF_ID", id)
        }
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun displayCurrentDirectory() {
        resultContainer?.removeAllViews()

        if (folderStack.isNotEmpty()) {
            val backBtn = Button(this).apply {
                text = "<-- Vissza"
                setOnClickListener {
                    folderStack.removeAt(folderStack.size - 1)
                    currentFolderId = folderStack.lastOrNull()?.first
                    loadCurrentFolderContent()
                }
            }
            styleButton(backBtn)
            resultContainer?.addView(backBtn)
        }

        if (currentFolders.isEmpty() && currentDocuments.isEmpty()) {
            showLoadingMessage("Üres mappa")
            return
        }

        for (folder in currentFolders) {
            val btn = Button(this).apply {
                text = "📁 ${folder.name}"
                setOnClickListener {
                    folderStack.add(Pair(folder.id, folder.name))
                    currentFolderId = folder.id
                    loadCurrentFolderContent()
                }
            }
            styleButton(btn)
            resultContainer?.addView(btn)
        }

        for (doc in currentDocuments) {
            val btn = Button(this).apply {
                text = "📄 ${doc.title}"
                setOnClickListener {
                    downloadPdfAndOpen(doc)
                }
            }
            styleButton(btn)
            resultContainer?.addView(btn)
        }
    }

    private fun displaySearchResults(docs: List<Document>) {
        resultContainer?.removeAllViews()

        if (docs.isEmpty()) {
            showLoadingMessage("Nincs a keresésnek megfelelő PDF")
            return
        }

        for (doc in docs) {
            val btn = Button(this).apply {
                text = "📄 ${doc.title}\n(${doc.fileName})"
                setOnClickListener {
                    downloadPdfAndOpen(doc)
                }
            }
            styleButton(btn)
            resultContainer?.addView(btn)
        }
    }

    private fun showLoadingMessage(message: String) {
        resultContainer?.removeAllViews()
        val msg = TextView(this).apply {
            setPadding(30, 30, 30, 30)
            textSize = 20f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(resources.getColor(R.color.white, null))
            text = message
        }
        resultContainer?.addView(msg)
    }

    private fun styleButton(btn: Button) {
        btn.setPadding(30, 30, 30, 30)
        btn.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 30)
        }
        btn.textSize = 20f
        btn.setBackgroundResource(R.drawable.rounded_edittext)
        btn.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        btn.setTextColor(resources.getColor(R.color.white, null))
        btn.elevation = 1f
    }
}
