package hu.unicon.szakdoga;


public interface CsvFileCallback {
    void onCsvLoaded(String csvContent);
    void onError(Exception e);
}

