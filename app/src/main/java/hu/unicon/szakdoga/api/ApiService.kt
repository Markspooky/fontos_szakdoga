package hu.unicon.szakdoga.api

import hu.unicon.szakdoga.model.Category
import hu.unicon.szakdoga.model.Document
import hu.unicon.szakdoga.model.Folder
import hu.unicon.szakdoga.model.Material
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {

    @GET("categories")
    fun getCategories(): Call<List<Category>>

    @GET("materials")
    fun getMaterials(@Query("search") search: String? = null): Call<List<Material>>

    @GET("materials/{id}")
    fun getMaterialById(@Path("id") id: Long): Call<Material>

    @GET("folders")
    fun getFolders(@Query("parentId") parentId: Long? = null): Call<List<Folder>>

    @GET("documents")
    fun getDocuments(@Query("folderId") folderId: Long? = null): Call<List<Document>>

    @GET("documents/search")
    fun searchDocuments(@Query("query") query: String): Call<List<Document>>

    @GET("documents/{id}/download")
    @Streaming
    fun downloadDocument(@Path("id") id: Long): Call<ResponseBody>
}
