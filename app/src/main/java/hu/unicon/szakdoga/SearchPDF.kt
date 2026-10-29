package hu.unicon.szakdoga

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.unicon.szakdoga.adapter.ExplorerAdapter
import hu.unicon.szakdoga.adapter.ExplorerItem
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
    private var recyclerView: RecyclerView? = null
    private var loader: ProgressBar? = null
    private var emptyStateTextView: TextView? = null

    private var currentFolderId: Long? = null
    private val folderStack = mutableListOf<Pair<Long?, String>>()

    private val currentFolders = mutableListOf<Folder>()
    private val currentDocuments = mutableListOf<Document>()

    private lateinit var explorerAdapter: ExplorerAdapter

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
        recyclerView = findViewById(R.id.explorerRecyclerView)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)

        explorerAdapter = ExplorerAdapter { item ->
            when (item) {
                is ExplorerItem.Back -> {
                    if (folderStack.isNotEmpty()) {
                        folderStack.removeAt(folderStack.size - 1)
                        currentFolderId = folderStack.lastOrNull()?.first
                        loadCurrentFolderContent()
                    }
                }
                is ExplorerItem.FolderItem -> {
                    folderStack.add(Pair(item.folder.id, item.folder.name))
                    currentFolderId = item.folder.id
                    loadCurrentFolderContent()
                }
                is ExplorerItem.DocumentItem -> {
                    downloadPdfAndOpen(item.document)
                }
            }
        }

        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = explorerAdapter

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
        emptyStateTextView?.visibility = View.GONE

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
        emptyStateTextView?.visibility = View.GONE

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

    private fun displayCurrentDirectory() {
        val items = mutableListOf<ExplorerItem>()

        if (folderStack.isNotEmpty()) {
            items.add(ExplorerItem.Back)
        }

        for (folder in currentFolders) {
            items.add(ExplorerItem.FolderItem(folder))
        }

        for (doc in currentDocuments) {
            items.add(ExplorerItem.DocumentItem(doc))
        }

        if (items.isEmpty() || (items.size == 1 && items.first() is ExplorerItem.Back)) {
            emptyStateTextView?.text = "Üres mappa"
            emptyStateTextView?.visibility = View.VISIBLE
            explorerAdapter.updateItems(items)
        } else {
            emptyStateTextView?.visibility = View.GONE
            explorerAdapter.updateItems(items)
        }
    }

    private fun displaySearchResults(docs: List<Document>) {
        if (docs.isEmpty()) {
            emptyStateTextView?.text = "Nincs a keresésnek megfelelő PDF"
            emptyStateTextView?.visibility = View.VISIBLE
            explorerAdapter.updateItems(emptyList())
        } else {
            emptyStateTextView?.visibility = View.GONE
            val items = docs.map { ExplorerItem.DocumentItem(it) }
            explorerAdapter.updateItems(items)
        }
    }
}
