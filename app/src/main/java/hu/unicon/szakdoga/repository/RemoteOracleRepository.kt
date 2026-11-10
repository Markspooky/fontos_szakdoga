package hu.unicon.szakdoga.repository

import android.content.Context
import hu.unicon.szakdoga.analytics.AnalyticsManager
import hu.unicon.szakdoga.api.ApiClient
import hu.unicon.szakdoga.model.Category
import hu.unicon.szakdoga.model.Document
import hu.unicon.szakdoga.model.Folder
import hu.unicon.szakdoga.model.Material
import hu.unicon.szakdoga.model.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RemoteOracleRepository(private val context: Context) : AppRepository {

    override fun login(username: String, callback: (Result<User>) -> Unit) {
        val startTime = System.currentTimeMillis()
        ApiClient.getService(context).login(username).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val duration = System.currentTimeMillis() - startTime
                if (response.isSuccessful && response.body() != null) {
                    AnalyticsManager.trackOperation(context, "LOGIN", DataSourceMode.ORACLE_REST, duration, true, username)
                    callback(Result.success(response.body()!!))
                } else {
                    AnalyticsManager.trackOperation(context, "LOGIN", DataSourceMode.ORACLE_REST, duration, false, username)
                    callback(Result.failure(Exception("Sikertelen bejelentkezés (HTTP ${response.code()})")))
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                val duration = System.currentTimeMillis() - startTime
                AnalyticsManager.trackOperation(context, "LOGIN", DataSourceMode.ORACLE_REST, duration, false, username)
                callback(Result.failure(t))
            }
        })
    }

    override fun getCategories(callback: (Result<List<Category>>) -> Unit) {
        val startTime = System.currentTimeMillis()
        ApiClient.getService(context).getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                val duration = System.currentTimeMillis() - startTime
                if (response.isSuccessful && response.body() != null) {
                    AnalyticsManager.trackOperation(context, "GET_CATEGORIES", DataSourceMode.ORACLE_REST, duration, true)
                    callback(Result.success(response.body()!!))
                } else {
                    AnalyticsManager.trackOperation(context, "GET_CATEGORIES", DataSourceMode.ORACLE_REST, duration, false)
                    callback(Result.failure(Exception("Hiba a kategóriák lekérésekor")))
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                val duration = System.currentTimeMillis() - startTime
                AnalyticsManager.trackOperation(context, "GET_CATEGORIES", DataSourceMode.ORACLE_REST, duration, false)
                callback(Result.failure(t))
            }
        })
    }

    override fun getMaterials(query: String?, callback: (Result<List<Material>>) -> Unit) {
        val startTime = System.currentTimeMillis()
        ApiClient.getService(context).getMaterials(query).enqueue(object : Callback<List<Material>> {
            override fun onResponse(call: Call<List<Material>>, response: Response<List<Material>>) {
                val duration = System.currentTimeMillis() - startTime
                if (response.isSuccessful && response.body() != null) {
                    AnalyticsManager.trackOperation(context, "SEARCH_MATERIAL", DataSourceMode.ORACLE_REST, duration, true, query)
                    callback(Result.success(response.body()!!))
                } else {
                    AnalyticsManager.trackOperation(context, "SEARCH_MATERIAL", DataSourceMode.ORACLE_REST, duration, false, query)
                    callback(Result.failure(Exception("Hiba az anyagok lekérésekor")))
                }
            }

            override fun onFailure(call: Call<List<Material>>, t: Throwable) {
                val duration = System.currentTimeMillis() - startTime
                AnalyticsManager.trackOperation(context, "SEARCH_MATERIAL", DataSourceMode.ORACLE_REST, duration, false, query)
                callback(Result.failure(t))
            }
        })
    }

    override fun getMaterialById(id: Long, callback: (Result<Material>) -> Unit) {
        val startTime = System.currentTimeMillis()
        ApiClient.getService(context).getMaterialById(id).enqueue(object : Callback<Material> {
            override fun onResponse(call: Call<Material>, response: Response<Material>) {
                val duration = System.currentTimeMillis() - startTime
                if (response.isSuccessful && response.body() != null) {
                    AnalyticsManager.trackOperation(context, "GET_MATERIAL_BY_ID", DataSourceMode.ORACLE_REST, duration, true, id.toString())
                    callback(Result.success(response.body()!!))
                } else {
                    AnalyticsManager.trackOperation(context, "GET_MATERIAL_BY_ID", DataSourceMode.ORACLE_REST, duration, false, id.toString())
                    callback(Result.failure(Exception("Material nem található")))
                }
            }

            override fun onFailure(call: Call<Material>, t: Throwable) {
                val duration = System.currentTimeMillis() - startTime
                AnalyticsManager.trackOperation(context, "GET_MATERIAL_BY_ID", DataSourceMode.ORACLE_REST, duration, false, id.toString())
                callback(Result.failure(t))
            }
        })
    }

    override fun getFolders(parentId: Long?, callback: (Result<List<Folder>>) -> Unit) {
        val startTime = System.currentTimeMillis()
        ApiClient.getService(context).getFolders(parentId).enqueue(object : Callback<List<Folder>> {
            override fun onResponse(call: Call<List<Folder>>, response: Response<List<Folder>>) {
                val duration = System.currentTimeMillis() - startTime
                if (response.isSuccessful && response.body() != null) {
                    AnalyticsManager.trackOperation(context, "GET_FOLDERS", DataSourceMode.ORACLE_REST, duration, true, parentId?.toString())
                    callback(Result.success(response.body()!!))
                } else {
                    AnalyticsManager.trackOperation(context, "GET_FOLDERS", DataSourceMode.ORACLE_REST, duration, false, parentId?.toString())
                    callback(Result.failure(Exception("Hiba a mappák lekérésekor")))
                }
            }

            override fun onFailure(call: Call<List<Folder>>, t: Throwable) {
                val duration = System.currentTimeMillis() - startTime
                AnalyticsManager.trackOperation(context, "GET_FOLDERS", DataSourceMode.ORACLE_REST, duration, false, parentId?.toString())
                callback(Result.failure(t))
            }
        })
    }

    override fun getDocuments(folderId: Long?, callback: (Result<List<Document>>) -> Unit) {
        val startTime = System.currentTimeMillis()
        ApiClient.getService(context).getDocuments(folderId).enqueue(object : Callback<List<Document>> {
            override fun onResponse(call: Call<List<Document>>, response: Response<List<Document>>) {
                val duration = System.currentTimeMillis() - startTime
                if (response.isSuccessful && response.body() != null) {
                    AnalyticsManager.trackOperation(context, "GET_DOCUMENTS", DataSourceMode.ORACLE_REST, duration, true, folderId?.toString())
                    callback(Result.success(response.body()!!))
                } else {
                    AnalyticsManager.trackOperation(context, "GET_DOCUMENTS", DataSourceMode.ORACLE_REST, duration, false, folderId?.toString())
                    callback(Result.failure(Exception("Hiba a dokumentumok lekérésekor")))
                }
            }

            override fun onFailure(call: Call<List<Document>>, t: Throwable) {
                val duration = System.currentTimeMillis() - startTime
                AnalyticsManager.trackOperation(context, "GET_DOCUMENTS", DataSourceMode.ORACLE_REST, duration, false, folderId?.toString())
                callback(Result.failure(t))
            }
        })
    }

    override fun searchDocuments(query: String, callback: (Result<List<Document>>) -> Unit) {
        val startTime = System.currentTimeMillis()
        ApiClient.getService(context).searchDocuments(query).enqueue(object : Callback<List<Document>> {
            override fun onResponse(call: Call<List<Document>>, response: Response<List<Document>>) {
                val duration = System.currentTimeMillis() - startTime
                if (response.isSuccessful && response.body() != null) {
                    AnalyticsManager.trackOperation(context, "SEARCH_DOCUMENT", DataSourceMode.ORACLE_REST, duration, true, query)
                    callback(Result.success(response.body()!!))
                } else {
                    AnalyticsManager.trackOperation(context, "SEARCH_DOCUMENT", DataSourceMode.ORACLE_REST, duration, false, query)
                    callback(Result.failure(Exception("Hiba a kereséskor")))
                }
            }

            override fun onFailure(call: Call<List<Document>>, t: Throwable) {
                val duration = System.currentTimeMillis() - startTime
                AnalyticsManager.trackOperation(context, "SEARCH_DOCUMENT", DataSourceMode.ORACLE_REST, duration, false, query)
                callback(Result.failure(t))
            }
        })
    }

    override fun downloadDocument(id: Long, callback: (Result<ResponseBody>) -> Unit) {
        val startTime = System.currentTimeMillis()
        ApiClient.getService(context).downloadDocument(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val duration = System.currentTimeMillis() - startTime
                if (response.isSuccessful && response.body() != null) {
                    AnalyticsManager.trackOperation(context, "DOWNLOAD_DOCUMENT", DataSourceMode.ORACLE_REST, duration, true, id.toString())
                    callback(Result.success(response.body()!!))
                } else {
                    AnalyticsManager.trackOperation(context, "DOWNLOAD_DOCUMENT", DataSourceMode.ORACLE_REST, duration, false, id.toString())
                    callback(Result.failure(Exception("Hiba a fájl letöltésekor")))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                val duration = System.currentTimeMillis() - startTime
                AnalyticsManager.trackOperation(context, "DOWNLOAD_DOCUMENT", DataSourceMode.ORACLE_REST, duration, false, id.toString())
                callback(Result.failure(t))
            }
        })
    }
}
