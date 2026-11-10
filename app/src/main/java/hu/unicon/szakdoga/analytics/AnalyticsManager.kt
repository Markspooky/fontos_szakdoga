package hu.unicon.szakdoga.analytics

import android.content.Context
import hu.unicon.szakdoga.db.AppDatabase
import hu.unicon.szakdoga.db.PerformanceMetricEntity
import hu.unicon.szakdoga.repository.DataSourceMode
import java.util.concurrent.Executors

object AnalyticsManager {
    private val executor = Executors.newSingleThreadExecutor()

    fun trackOperation(
        context: Context,
        operationType: String,
        mode: DataSourceMode,
        durationMs: Long,
        isSuccess: Boolean,
        queryText: String? = null
    ) {
        executor.execute {
            try {
                val db = AppDatabase.getInstance(context)
                val metric = PerformanceMetricEntity(
                    operationType = operationType,
                    dataSourceMode = mode.name,
                    durationMs = durationMs,
                    isSuccess = isSuccess,
                    queryText = queryText,
                    timestamp = System.currentTimeMillis()
                )
                db.performanceMetricDao().insertMetric(metric)
            } catch (e: Exception) {
                // Background telemetry logging error should never disrupt user experience
            }
        }
    }

    fun getSummary(context: Context): AnalyticsSummary {
        val db = AppDatabase.getInstance(context)
        val dao = db.performanceMetricDao()

        val total = dao.getTotalCount()
        val success = dao.getSuccessCount()
        val failure = dao.getFailureCount()
        val successRate = if (total > 0) (success.toDouble() / total.toDouble()) * 100.0 else 0.0

        val avgMaterialOracle = dao.getAverageDurationMs("SEARCH_MATERIAL", DataSourceMode.ORACLE_REST.name) ?: 0.0
        val avgMaterialSqlite = dao.getAverageDurationMs("SEARCH_MATERIAL", DataSourceMode.LOCAL_SQLITE.name) ?: 0.0

        val avgDocumentOracle = dao.getAverageDurationForOpsMs("GET_DOCUMENTS", "SEARCH_DOCUMENT", DataSourceMode.ORACLE_REST.name) ?: 0.0
        val avgDocumentSqlite = dao.getAverageDurationForOpsMs("GET_DOCUMENTS", "SEARCH_DOCUMENT", DataSourceMode.LOCAL_SQLITE.name) ?: 0.0

        return AnalyticsSummary(
            totalOperations = total,
            successCount = success,
            failureCount = failure,
            successRatePercent = successRate,
            avgMaterialOracleRestMs = avgMaterialOracle,
            avgMaterialLocalSqliteMs = avgMaterialSqlite,
            avgDocumentOracleRestMs = avgDocumentOracle,
            avgDocumentLocalSqliteMs = avgDocumentSqlite
        )
    }

    fun generateCsvReport(context: Context): String {
        val db = AppDatabase.getInstance(context)
        val metrics = db.performanceMetricDao().getAllMetrics()

        val sb = java.lang.StringBuilder()
        sb.append("ID,Timestamp,Operation,DataSourceMode,DurationMs,IsSuccess,QueryText\n")
        for (m in metrics) {
            sb.append("${m.id},${m.timestamp},${m.operationType},${m.dataSourceMode},${m.durationMs},${m.isSuccess},\"${m.queryText ?: ""}\"\n")
        }
        return sb.toString()
    }

    fun clearAll(context: Context) {
        executor.execute {
            try {
                AppDatabase.getInstance(context).performanceMetricDao().clearAllMetrics()
            } catch (e: Exception) {
            }
        }
    }
}

data class AnalyticsSummary(
    val totalOperations: Int,
    val successCount: Int,
    val failureCount: Int,
    val successRatePercent: Double,
    val avgMaterialOracleRestMs: Double,
    val avgMaterialLocalSqliteMs: Double,
    val avgDocumentOracleRestMs: Double,
    val avgDocumentLocalSqliteMs: Double
)
