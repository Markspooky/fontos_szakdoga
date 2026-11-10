package hu.unicon.szakdoga.repository

import hu.unicon.szakdoga.model.Category
import hu.unicon.szakdoga.model.Document
import hu.unicon.szakdoga.model.Folder
import hu.unicon.szakdoga.model.Material
import hu.unicon.szakdoga.model.User
import okhttp3.ResponseBody

interface AppRepository {
    fun login(username: String, callback: (Result<User>) -> Unit)
    fun getCategories(callback: (Result<List<Category>>) -> Unit)
    fun getMaterials(query: String?, callback: (Result<List<Material>>) -> Unit)
    fun getMaterialById(id: Long, callback: (Result<Material>) -> Unit)
    fun getFolders(parentId: Long?, callback: (Result<List<Folder>>) -> Unit)
    fun getDocuments(folderId: Long?, callback: (Result<List<Document>>) -> Unit)
    fun searchDocuments(query: String, callback: (Result<List<Document>>) -> Unit)
    fun downloadDocument(id: Long, callback: (Result<ResponseBody>) -> Unit)
}
