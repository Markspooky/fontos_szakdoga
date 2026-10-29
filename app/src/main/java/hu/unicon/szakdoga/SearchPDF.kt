package hu.unicon.szakdoga

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
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
    private var breadcrumbChipGroup: ChipGroup? = null

    private var currentFolderId: Long? = null
    private val folderStack = mutableListOf<Pair<Long?, String>>()

    private val currentFolders = mutableListOf<Folder>()
    private val currentDocuments = mutableListOf<Document>()

    private lateinit var explorerAdapter: ExplorerAdapter
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_pdf)

        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loader = findViewById(R.id.loader)
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.explorerRecyclerView)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        breadcrumbChipGroup = findViewById(R.id.breadcrumbChipGroup)

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
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    val query = s.toString().trim()
                    if (query.isEmpty()) {
                        loadCurrentFolderContent()
                    } else if (query.length >= 2) {
                        searchDocumentsInApi(query)
                    }
                }
                searchHandler.postDelayed(searchRunnable!!, 300)
            }
        })

        loadCurrentFolderContent()
    }

    override fun onDestroy() {
        super.onDestroy()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
    }

    private fun loadCurrentFolderContent() {
        // Immediate UI feedback on touch/navigation
        loader?.visibility = View.VISIBLE
        emptyStateTextView?.visibility = View.GONE
        currentFolders.clear()
        currentDocuments.clear()
        displayCurrentDirectory()
        updateBreadcrumbsUI()

        val apiService = ApiClient.getService(this)
        val targetFolderId = currentFolderId

        var foldersFetched = false
        var docsFetched = false
        var hasError = false

        fun checkAndDisplay() {
            if (hasError) return
            if (foldersFetched && docsFetched) {
                loader?.visibility = View.GONE
                displayCurrentDirectory()
            }
        }

        // Parallel Request 1: Folders
        apiService.getFolders(targetFolderId).enqueue(object : Callback<List<Folder>> {
            override fun onResponse(call: Call<List<Folder>>, response: Response<List<Folder>>) {
                if (response.isSuccessful) {
                    currentFolders.clear()
                    response.body()?.let { currentFolders.addAll(it) }
                    foldersFetched = true
                    checkAndDisplay()
                } else {
                    if (!hasError) {
                        hasError = true
                        loader?.visibility = View.GONE
                        showErrorSnackbar("Hiba a mappák betöltésekor") { loadCurrentFolderContent() }
                    }
                }
            }

            override fun onFailure(call: Call<List<Folder>>, t: Throwable) {
                if (!hasError) {
                    hasError = true
                    loader?.visibility = View.GONE
                    Log.e("SearchPDF", "Hiba a mappák lekérésekor", t)
                    showErrorSnackbar("Hálózati hiba történt") { loadCurrentFolderContent() }
                }
            }
        })

        // Parallel Request 2: Documents
        apiService.getDocuments(targetFolderId).enqueue(object : Callback<List<Document>> {
            override fun onResponse(call: Call<List<Document>>, response: Response<List<Document>>) {
                if (response.isSuccessful) {
                    currentDocuments.clear()
                    response.body()?.let { currentDocuments.addAll(it) }
                    docsFetched = true
                    checkAndDisplay()
                } else {
                    if (!hasError) {
                        hasError = true
                        loader?.visibility = View.GONE
                        showErrorSnackbar("Hiba a dokumentumok betöltésekor") { loadCurrentFolderContent() }
                    }
                }
            }

            override fun onFailure(call: Call<List<Document>>, t: Throwable) {
                if (!hasError) {
                    hasError = true
                    loader?.visibility = View.GONE
                    Log.e("SearchPDF", "Hiba a dokumentumok lekérésekor", t)
                    showErrorSnackbar("Hálózati hiba történt") { loadCurrentFolderContent() }
                }
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
                    showErrorSnackbar("Hiba a keresés során") {
                        searchDocumentsInApi(query)
                    }
                }
            }

            override fun onFailure(call: Call<List<Document>>, t: Throwable) {
                loader?.visibility = View.GONE
                Log.e("SearchPDF", "Hiba a kereséskor", t)
                showErrorSnackbar("Hálózati hiba történt") {
                    searchDocumentsInApi(query)
                }
            }
        })
    }

    private fun updateBreadcrumbsUI() {
        breadcrumbChipGroup?.removeAllViews()

        val rootChip = Chip(this).apply {
            text = "📁 Gyökér"
            isCheckable = false
            setOnClickListener {
                folderStack.clear()
                currentFolderId = null
                loadCurrentFolderContent()
            }
        }
        breadcrumbChipGroup?.addView(rootChip)

        for (i in folderStack.indices) {
            val (folderId, folderName) = folderStack[i]
            val chip = Chip(this).apply {
                text = "› $folderName"
                isCheckable = false
                setOnClickListener {
                    while (folderStack.size > i + 1) {
                        folderStack.removeAt(folderStack.size - 1)
                    }
                    currentFolderId = folderId
                    loadCurrentFolderContent()
                }
            }
            breadcrumbChipGroup?.addView(chip)
        }
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
                                showErrorSnackbar("Hiba a fájl mentésekor") {
                                    downloadPdfAndOpen(doc)
                                }
                            }
                        }
                    }.start()
                } else {
                    loader?.visibility = View.GONE
                    showErrorSnackbar("Hiba a PDF letöltésekor") {
                        downloadPdfAndOpen(doc)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                loader?.visibility = View.GONE
                Log.e("SearchPDF", "Hiba a letöltéskor", t)
                showErrorSnackbar("Hálózati hiba letöltéskor") {
                    downloadPdfAndOpen(doc)
                }
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
            if (loader?.visibility != View.VISIBLE) {
                emptyStateTextView?.text = "Üres mappa"
                emptyStateTextView?.visibility = View.VISIBLE
            } else {
                emptyStateTextView?.visibility = View.GONE
            }
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

    private fun showErrorSnackbar(message: String, onRetry: () -> Unit) {
        val mainView = findViewById<View>(R.id.main) ?: return
        Snackbar.make(mainView, message, Snackbar.LENGTH_INDEFINITE)
            .setAction("ÚJRA") { onRetry() }
            .show()
    }
}
