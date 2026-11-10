package hu.unicon.szakdoga.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import hu.unicon.szakdoga.model.Category
import hu.unicon.szakdoga.model.Document
import hu.unicon.szakdoga.model.Folder
import hu.unicon.szakdoga.model.Material
import hu.unicon.szakdoga.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long,
    val username: String,
    val name: String,
    val role: String
) {
    fun toModel(): User = User(id, username, name, role)
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String
) {
    fun toModel(): Category = Category(id, name, description)
}

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey val id: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val name: String,
    val skuCode: String?,
    val details: String?,
    val lastUpdate: String?
) {
    fun toModel(): Material = Material(id, categoryId, categoryName, name, skuCode, details, lastUpdate)
}

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val parentId: Long?
) {
    fun toModel(): Folder = Folder(id, name, parentId)
}

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: Long,
    val folderId: Long?,
    val title: String,
    val fileName: String,
    val storagePath: String?,
    val fileSize: Long?,
    val uploadDate: String?
) {
    fun toModel(): Document = Document(id, folderId, title, fileName, storagePath, fileSize, uploadDate)
}

@Entity(tableName = "performance_metrics")
data class PerformanceMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String,
    val dataSourceMode: String,
    val durationMs: Long,
    val isSuccess: Boolean,
    val queryText: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
