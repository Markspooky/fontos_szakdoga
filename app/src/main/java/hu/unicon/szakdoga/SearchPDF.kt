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
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import java.io.File
import java.io.FileOutputStream
import java.util.EnumSet
import java.util.Locale

class SearchPDF : AppCompatActivity() {
    private var searchEditText: EditText? = null
    private var resultContainer: LinearLayout? = null
    private var currentPath: String = ""
    private var loader: ProgressBar? = null

    private var NAME: String? = null
    private var SERVER_IP: String? = null
    private var SHARE_NAME: String? = null
    private var PDF_PATH: String? = null
    private var USERNAME: String? = null
    private var PASSWORD: String? = null

    internal data class PDF(val name: String, val fileID: FileIdBothDirectoryInformation?)

    private val allPdf = mutableListOf<PDF>()

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

        NAME = intent.getStringExtra("NAME")
        SERVER_IP = intent.getStringExtra("NAS_IP")
        USERNAME = intent.getStringExtra("USERNAME")
        PASSWORD = intent.getStringExtra("PASSWORD")
        SHARE_NAME = intent.getStringExtra("SHARE_NAME")
        PDF_PATH = intent.getStringExtra("PDF_PATH")

        if (NAME == null || SERVER_IP == null || USERNAME == null || PASSWORD == null || SHARE_NAME == null || PDF_PATH == null) {
            Toast.makeText(this, "Hiányos konfiguráció", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentPath = savedInstanceState?.getString("currentPath") ?: PDF_PATH!!

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    fetchItemsFromNas(currentPath)
                } else if (query.length >= 3) {
                    showLoadingMessage("Töltés...")
                    searchRecursivelyOnNas(PDF_PATH!!, query)
                } else {
                    showLoadingMessage("Legalább 3 karakterrel keress!")
                }
            }
        })

        fetchItemsFromNas(currentPath)
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

    private fun searchRecursivelyOnNas(startPath: String, query: String) {
        Thread {
            val client = SMBClient()
            try {
                client.connect(SERVER_IP).use { connection ->
                    val session = connection.authenticate(
                        AuthenticationContext(USERNAME, PASSWORD?.toCharArray(), "")
                    )
                    (session.connectShare(SHARE_NAME) as DiskShare).use { share ->
                        val matchedPdfs = mutableListOf<PDF>()
                        recursiveSearch(share, startPath, query, matchedPdfs)
                        runOnUiThread {
                            allPdf.clear()
                            allPdf.addAll(matchedPdfs)
                            updateResults(query)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SMB_SEARCH", "Rekurzív keresés hiba", e)
                runOnUiThread {
                    Toast.makeText(this, "Hiba a rekurzív keresés során", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun recursiveSearch(share: DiskShare, path: String, query: String, resultList: MutableList<PDF>) {
        val items = try { share.list(path) } catch (e: Exception) { emptyList() }
        for (item in items) {
            val name = item.fileName
            if (name == "." || name == "..") continue

            val fullPath = if (path.endsWith("/")) path + name else "$path/$name"

            if ((item.fileAttributes and FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value) != 0L) {
                recursiveSearch(share, fullPath, query, resultList)
            } else if (name.lowercase().endsWith(".pdf") && name.lowercase().contains(query.lowercase())) {
                resultList.add(PDF(fullPath, item))
            }
        }
    }

    private fun fetchItemsFromNas(folderPath: String) {
        Thread {
            val client = SMBClient()
            try {
                client.connect(SERVER_IP).use { connection ->
                    val session = connection.authenticate(
                        AuthenticationContext(USERNAME, PASSWORD?.toCharArray(), "")
                    )
                    (session.connectShare(SHARE_NAME) as DiskShare).use { share ->
                        val tempList = mutableListOf<PDF>()
                        val folderList = mutableListOf<String>()

                        for (item in share.list(folderPath)) {
                            val name = item.fileName
                            if (name == "." || name == "..") continue

                            if ((item.fileAttributes and FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value) != 0L) {
                                folderList.add(name)
                            } else if (name.lowercase().endsWith(".pdf")) {
                                tempList.add(PDF(name, item))
                            }
                        }
                        runOnUiThread {
                            allPdf.clear()
                            allPdf.addAll(tempList)
                            currentPath = folderPath
                            updateResultsWithFolders(folderList, tempList)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SMB", "Hiba a NAS elérésekor", e)
                runOnUiThread {
                    Toast.makeText(this, "Hiba a NAS elérésekor", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun openPdfFromNas(fullFilePath: String) {
        Thread {
            try {
                val fileName = fullFilePath.substring(fullFilePath.lastIndexOf('/') + 1)
                val tempFile = File(cacheDir, fileName)

                val client = SMBClient()
                client.connect(SERVER_IP).use { connection ->
                    val session = connection.authenticate(
                        AuthenticationContext(USERNAME, PASSWORD?.toCharArray(), "")
                    )
                    (session.connectShare(SHARE_NAME) as DiskShare).use { share ->
                        share.openFile(
                            fullFilePath,
                            EnumSet.of(AccessMask.GENERIC_READ),
                            null,
                            SMB2ShareAccess.ALL,
                            SMB2CreateDisposition.FILE_OPEN,
                            null
                        ).use { smbFile ->
                            smbFile.inputStream.use { inputStream ->
                                FileOutputStream(tempFile).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                        }
                    }
                }
                runOnUiThread {
                    loader?.visibility = View.GONE
                    launchPdf(tempFile.absolutePath, fileName)
                }
            } catch (e: Exception) {
                Log.e("PDF_OPEN", "Hiba a PDF megnyitásakor", e)
                runOnUiThread {
                    loader?.visibility = View.GONE
                    Toast.makeText(this, "Hiba a PDF megnyitásakor", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun launchPdf(path: String, fileName: String) {
        Toast.makeText(this, "PDF megnyitása: $fileName", Toast.LENGTH_SHORT).show()

        val fileUri = Uri.fromFile(File(path)).toString();

        val intent = Intent(this, PDFOpen::class.java).apply {
            putExtra("PDF_URI", fileUri);
        }
        startActivity(intent)

    }

    @SuppressLint("SetTextI18n")
    private fun updateResultsWithFolders(folders: List<String>, pdfs: List<PDF>) {
        resultContainer?.removeAllViews()

        if (normalizePath(currentPath) != normalizePath(PDF_PATH!!)) {
            val backBtn = Button(this).apply {
                text = "<-- Vissza"
                setOnClickListener {
                    val parentPath = if (currentPath.startsWith(PDF_PATH!!) && currentPath.length > PDF_PATH!!.length) {
                        val p = currentPath.substring(0, currentPath.lastIndexOf('/'))
                        if (p.endsWith("/")) p.substring(0, p.length - 1) else p
                    } else {
                        PDF_PATH!!
                    }
                    fetchItemsFromNas(parentPath)
                }
            }
            styleButton(backBtn)
            resultContainer?.addView(backBtn)
        }

        if (folders.isEmpty() && pdfs.isEmpty()) {
            showLoadingMessage("Hoppá!\nÚgy tűnik üres a mappa")
            return
        }

        for (folderName in folders) {
            val btn = Button(this).apply {
                text = "📁 $folderName"
                setOnClickListener {
                    val newPath = if (currentPath.endsWith("/")) currentPath + folderName else "$currentPath/$folderName"
                    fetchItemsFromNas(newPath)
                }
            }
            styleButton(btn)
            resultContainer?.addView(btn)
        }

        for (pdf in pdfs) {
            val btn = Button(this).apply {
                text = "📄 ${pdf.name}"
                setOnClickListener {
                    it.isEnabled = false
                    loader?.visibility = View.VISIBLE
                    val path = if (currentPath.endsWith("/")) currentPath + pdf.name else "$currentPath/${pdf.name}"
                    openPdfFromNas(path)
                    it.postDelayed({ it.isEnabled = true }, 2000)
                }
            }
            styleButton(btn)
            resultContainer?.addView(btn)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateResults(query: String) {
        resultContainer?.removeAllViews()

        if (query.length >= 3) {
            var addedCount = 0
            val lowerQuery = query.lowercase()
            for (pdf in allPdf) {
                if (pdf.name.lowercase().contains(lowerQuery)) {
                    val btn = Button(this).apply {
                        val fileName = pdf.name.substring(pdf.name.lastIndexOf('/') + 1)
                        val folderPath = pdf.name.substring(0, pdf.name.lastIndexOf('/'))
                        text = "📄 $fileName\n📁 $folderPath"
                        setOnClickListener {
                            it.isEnabled = false
                            loader?.visibility = View.VISIBLE
                            openPdfFromNas(pdf.name)
                            it.postDelayed({ it.isEnabled = true }, 2000)
                        }
                    }
                    styleButton(btn)
                    resultContainer?.addView(btn)
                    addedCount++
                    if (addedCount >= 100) break
                }
            }

            if (addedCount == 0) {
                showLoadingMessage("Hoppá!\nNincs a keresésnek megfelelő találat")
            }
        }
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentPath", currentPath)
    }

    private fun normalizePath(path: String): String {
        return path.removeSuffix("/")
    }
}
