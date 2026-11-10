package hu.unicon.szakdoga.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<UserEntity>)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(categories: List<CategoryEntity>)
}

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials WHERE (:query IS NULL OR name LIKE '%' || :query || '%' OR skuCode LIKE '%' || :query || '%' OR details LIKE '%' || :query || '%')")
    fun getMaterials(query: String?): List<MaterialEntity>

    @Query("SELECT * FROM materials WHERE id = :id LIMIT 1")
    fun getMaterialById(id: Long): MaterialEntity?

    @Query("SELECT COUNT(*) FROM materials")
    fun getMaterialCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(materials: List<MaterialEntity>)
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE (:parentId IS NULL AND parentId IS NULL) OR (parentId = :parentId)")
    fun getFoldersByParentId(parentId: Long?): List<FolderEntity>

    @Query("SELECT COUNT(*) FROM folders")
    fun getFolderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(folders: List<FolderEntity>)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE (:folderId IS NULL AND folderId IS NULL) OR (folderId = :folderId)")
    fun getDocumentsByFolderId(folderId: Long?): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' OR fileName LIKE '%' || :query || '%'")
    fun searchDocuments(query: String): List<DocumentEntity>

    @Query("SELECT COUNT(*) FROM documents")
    fun getDocumentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(documents: List<DocumentEntity>)
}

@Dao
interface PerformanceMetricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMetric(metric: PerformanceMetricEntity)

    @Query("SELECT * FROM performance_metrics ORDER BY timestamp DESC")
    fun getAllMetrics(): List<PerformanceMetricEntity>

    @Query("SELECT AVG(durationMs) FROM performance_metrics WHERE operationType = :opType AND dataSourceMode = :mode AND isSuccess = 1")
    fun getAverageDurationMs(opType: String, mode: String): Double?

    @Query("SELECT AVG(durationMs) FROM performance_metrics WHERE (operationType = :opType1 OR operationType = :opType2) AND dataSourceMode = :mode AND isSuccess = 1")
    fun getAverageDurationForOpsMs(opType1: String, opType2: String, mode: String): Double?

    @Query("SELECT COUNT(*) FROM performance_metrics WHERE isSuccess = 1")
    fun getSuccessCount(): Int

    @Query("SELECT COUNT(*) FROM performance_metrics WHERE isSuccess = 0")
    fun getFailureCount(): Int

    @Query("SELECT COUNT(*) FROM performance_metrics")
    fun getTotalCount(): Int

    @Query("DELETE FROM performance_metrics")
    fun clearAllMetrics()
}
