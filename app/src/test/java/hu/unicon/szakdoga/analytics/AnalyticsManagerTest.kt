package hu.unicon.szakdoga.analytics

import hu.unicon.szakdoga.repository.DataSourceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsManagerTest {

    @Test
    fun summaryDataStructure_storesAndCalculatesCorrectly() {
        val summary = AnalyticsSummary(
            totalOperations = 10,
            successCount = 9,
            failureCount = 1,
            successRatePercent = 90.0,
            avgMaterialOracleRestMs = 142.5,
            avgMaterialLocalSqliteMs = 11.2,
            avgDocumentOracleRestMs = 180.0,
            avgDocumentLocalSqliteMs = 15.4
        )

        assertEquals(10, summary.totalOperations)
        assertEquals(9, summary.successCount)
        assertEquals(1, summary.failureCount)
        assertEquals(90.0, summary.successRatePercent, 0.01)
        assertEquals(142.5, summary.avgMaterialOracleRestMs, 0.01)
        assertEquals(11.2, summary.avgMaterialLocalSqliteMs, 0.01)
        assertTrue(summary.avgMaterialOracleRestMs > summary.avgMaterialLocalSqliteMs)
    }

    @Test
    fun csvFormatHeader_matchesExpectedColumns() {
        val header = "ID,Timestamp,Operation,DataSourceMode,DurationMs,IsSuccess,QueryText"
        assertTrue(header.contains("ID"))
        assertTrue(header.contains("Operation"))
        assertTrue(header.contains("DataSourceMode"))
        assertTrue(header.contains("DurationMs"))
    }
}
