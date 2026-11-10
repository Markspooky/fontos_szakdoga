package hu.unicon.szakdoga.repository

import android.content.Context

object RepositoryProvider {
    private var repositoryInstance: AppRepository? = null
    private var activeMode: DataSourceMode? = null

    fun getRepository(context: Context): AppRepository {
        val currentMode = DataSourcePreferences.getMode(context)
        if (repositoryInstance == null || activeMode != currentMode) {
            activeMode = currentMode
            repositoryInstance = when (currentMode) {
                DataSourceMode.ORACLE_REST -> RemoteOracleRepository(context.applicationContext)
                DataSourceMode.LOCAL_SQLITE -> LocalSqliteRepository(context.applicationContext)
            }
        }
        return repositoryInstance!!
    }

    fun reset() {
        repositoryInstance = null
        activeMode = null
    }
}
