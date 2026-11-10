package hu.unicon.szakdoga.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import hu.unicon.szakdoga.analytics.AnalyticsManager
import hu.unicon.szakdoga.db.AppDatabase
import hu.unicon.szakdoga.model.Category
import hu.unicon.szakdoga.model.Document
import hu.unicon.szakdoga.model.Folder
import hu.unicon.szakdoga.model.Material
import hu.unicon.szakdoga.model.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.Executors

class LocalSqliteRepository(private val context: Context) : AppRepository {
    private val db = AppDatabase.getInstance(context)
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun login(username: String, callback: (Result<User>) -> Unit) {
        val startTime = System.currentTimeMillis()
        executor.execute {
            val userEntity = db.userDao().getUserByUsername(username)
            val duration = System.currentTimeMillis() - startTime
            val isSuccess = userEntity != null
            AnalyticsManager.trackOperation(context, "LOGIN", DataSourceMode.LOCAL_SQLITE, duration, isSuccess, username)

            mainHandler.post {
                if (userEntity != null) {
                    callback(Result.success(userEntity.toModel()))
                } else {
                    callback(Result.failure(Exception("Nem található ilyen felhasználó az SQLite adatbázisban: $username")))
                }
            }
        }
    }

    override fun getCategories(callback: (Result<List<Category>>) -> Unit) {
        val startTime = System.currentTimeMillis()
        executor.execute {
            val list = db.categoryDao().getAllCategories().map { it.toModel() }
            val duration = System.currentTimeMillis() - startTime
            AnalyticsManager.trackOperation(context, "GET_CATEGORIES", DataSourceMode.LOCAL_SQLITE, duration, true)

            mainHandler.post { callback(Result.success(list)) }
        }
    }

    override fun getMaterials(query: String?, callback: (Result<List<Material>>) -> Unit) {
        val startTime = System.currentTimeMillis()
        executor.execute {
            val list = db.materialDao().getMaterials(query).map { it.toModel() }
            val duration = System.currentTimeMillis() - startTime
            AnalyticsManager.trackOperation(context, "SEARCH_MATERIAL", DataSourceMode.LOCAL_SQLITE, duration, true, query)

            mainHandler.post { callback(Result.success(list)) }
        }
    }

    override fun getMaterialById(id: Long, callback: (Result<Material>) -> Unit) {
        val startTime = System.currentTimeMillis()
        executor.execute {
            val item = db.materialDao().getMaterialById(id)
            val duration = System.currentTimeMillis() - startTime
            val isSuccess = item != null
            AnalyticsManager.trackOperation(context, "GET_MATERIAL_BY_ID", DataSourceMode.LOCAL_SQLITE, duration, isSuccess, id.toString())

            mainHandler.post {
                if (item != null) {
                    callback(Result.success(item.toModel()))
                } else {
                    callback(Result.failure(Exception("Nem található alkatrész az azonosítóval: $id")))
                }
            }
        }
    }

    override fun getFolders(parentId: Long?, callback: (Result<List<Folder>>) -> Unit) {
        val startTime = System.currentTimeMillis()
        executor.execute {
            val list = db.folderDao().getFoldersByParentId(parentId).map { it.toModel() }
            val duration = System.currentTimeMillis() - startTime
            AnalyticsManager.trackOperation(context, "GET_FOLDERS", DataSourceMode.LOCAL_SQLITE, duration, true, parentId?.toString())

            mainHandler.post { callback(Result.success(list)) }
        }
    }

    override fun getDocuments(folderId: Long?, callback: (Result<List<Document>>) -> Unit) {
        val startTime = System.currentTimeMillis()
        executor.execute {
            val list = db.documentDao().getDocumentsByFolderId(folderId).map { it.toModel() }
            val duration = System.currentTimeMillis() - startTime
            AnalyticsManager.trackOperation(context, "GET_DOCUMENTS", DataSourceMode.LOCAL_SQLITE, duration, true, folderId?.toString())

            mainHandler.post { callback(Result.success(list)) }
        }
    }

    override fun searchDocuments(query: String, callback: (Result<List<Document>>) -> Unit) {
        val startTime = System.currentTimeMillis()
        executor.execute {
            val list = db.documentDao().searchDocuments(query).map { it.toModel() }
            val duration = System.currentTimeMillis() - startTime
            AnalyticsManager.trackOperation(context, "SEARCH_DOCUMENT", DataSourceMode.LOCAL_SQLITE, duration, true, query)

            mainHandler.post { callback(Result.success(list)) }
        }
    }

    override fun downloadDocument(id: Long, callback: (Result<ResponseBody>) -> Unit) {
        val startTime = System.currentTimeMillis()
        executor.execute {
            val dummyPdfContent = "%PDF-1.4 Mock SQLite PDF Document Content".toByteArray()
            val responseBody = dummyPdfContent.toResponseBody("application/pdf".toMediaTypeOrNull())
            val duration = System.currentTimeMillis() - startTime
            AnalyticsManager.trackOperation(context, "DOWNLOAD_DOCUMENT", DataSourceMode.LOCAL_SQLITE, duration, true, id.toString())

            mainHandler.post { callback(Result.success(responseBody)) }
        }
    }
}
