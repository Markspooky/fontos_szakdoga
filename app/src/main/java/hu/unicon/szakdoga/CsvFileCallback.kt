package hu.unicon.szakdoga


interface CsvFileCallback {
    fun onCsvLoaded(csvContent: String?)
    fun onError(e: Exception?)
}

