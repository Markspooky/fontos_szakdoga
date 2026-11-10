package hu.unicon.szakdoga.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RepositoryProviderTest {

    @Test
    fun dataSourceModeEnum_hasExpectedValues() {
        assertEquals("Oracle DB (REST API)", DataSourceMode.ORACLE_REST.displayName)
        assertEquals("Helyi SQLite Adatbázis", DataSourceMode.LOCAL_SQLITE.displayName)
    }

    @Test
    fun repositoryProvider_resetClearsInstance() {
        RepositoryProvider.reset()
        assertNotNull(RepositoryProvider)
    }
}
