package hu.unicon.szakdoga.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class DataSourcePreferencesTest {

    @Test
    fun defaultMode_isOracleRest() {
        val fakePrefs = FakeSharedPreferences()
        val modeStr = fakePrefs.getString("data_source_mode", DataSourceMode.ORACLE_REST.name)
        val mode = DataSourceMode.valueOf(modeStr!!)
        assertEquals(DataSourceMode.ORACLE_REST, mode)
    }

    @Test
    fun setMode_savesToSharedPreferences() {
        val fakePrefs = FakeSharedPreferences()
        fakePrefs.edit().putString("data_source_mode", DataSourceMode.LOCAL_SQLITE.name).apply()

        val savedModeStr = fakePrefs.getString("data_source_mode", null)
        val savedMode = DataSourceMode.valueOf(savedModeStr!!)
        assertEquals(DataSourceMode.LOCAL_SQLITE, savedMode)
    }
}
