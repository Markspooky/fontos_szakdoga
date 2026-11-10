package hu.unicon.szakdoga.repository

enum class DataSourceMode(val displayName: String) {
    ORACLE_REST("Oracle DB (REST API)"),
    LOCAL_SQLITE("Helyi SQLite Adatbázis")
}
