package hu.unicon.szakdoga.repository

import android.content.Context
import android.content.SharedPreferences

object DataSourcePreferences {
    private const val PREFS_NAME = "data_source_config"
    private const val KEY_DATA_SOURCE_MODE = "data_source_mode"

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getMode(context: Context): DataSourceMode {
        val prefs = getPrefs(context)
        val modeStr = prefs.getString(KEY_DATA_SOURCE_MODE, DataSourceMode.ORACLE_REST.name)
        return try {
            DataSourceMode.valueOf(modeStr ?: DataSourceMode.ORACLE_REST.name)
        } catch (e: Exception) {
            DataSourceMode.ORACLE_REST
        }
    }

    fun setMode(context: Context, mode: DataSourceMode) {
        val prefs = getPrefs(context)
        prefs.edit().putString(KEY_DATA_SOURCE_MODE, mode.name).apply()
    }
}
