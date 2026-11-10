package hu.unicon.szakdoga.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        MaterialEntity::class,
        FolderEntity::class,
        DocumentEntity::class,
        PerformanceMetricEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun materialDao(): MaterialDao
    abstract fun folderDao(): FolderDao
    abstract fun documentDao(): DocumentDao
    abstract fun performanceMetricDao(): PerformanceMetricDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_sqlite_database.db"
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                INSTANCE = instance
                seedInitialData(instance)
                instance
            }
        }

        private fun seedInitialData(db: AppDatabase) {
            if (db.userDao().getUserCount() == 0) {
                db.userDao().insertAll(
                    listOf(
                        UserEntity(1, "dolgozo", "Teszt Dolgozó", "DOLGOZO"),
                        UserEntity(2, "muszakvezeto", "Kovács István Műszakvezető", "MUSZAKVEZETO"),
                        UserEntity(3, "admin", "Rendszer Adminisztrátor", "ADMIN")
                    )
                )
            }

            if (db.categoryDao().getCategoryCount() == 0) {
                db.categoryDao().insertAll(
                    listOf(
                        CategoryEntity(1, "Alkatrészek", "Gyártási alkatrészek és hidraulika elemek"),
                        CategoryEntity(2, "Kábelek & Vezetékek", "Villamos csatlakozók és kábelkorbácsok"),
                        CategoryEntity(3, "Elektromos modulok", "Szenzorok, PLC egységek és relék")
                    )
                )
            }

            if (db.materialDao().getMaterialCount() == 0) {
                db.materialDao().insertAll(
                    listOf(
                        MaterialEntity(101, 1, "Alkatrészek", "Hidraulika szivattyú H-200", "SKU-HYD-01", "Magas nyomású szivattyú 200 bar", "2026-02-15"),
                        MaterialEntity(102, 1, "Alkatrészek", "Acél csapágy 6205-ZZ", "SKU-BRG-05", "Golyóscsapágy gépészeti egységekhez", "2026-02-18"),
                        MaterialEntity(201, 2, "Kábelek & Vezetékek", "Ipari Ethernet Kábel CAT6a 10m", "SKU-CAB-06", "Árnyékolt FTP kábel rögzített csatlakozóval", "2026-02-20"),
                        MaterialEntity(301, 3, "Elektromos modulok", "Optikai Érzékelő PNP 24V", "SKU-SEN-24", "Reflexiós optikai érzékelő M12", "2026-02-22")
                    )
                )
            }

            if (db.folderDao().getFolderCount() == 0) {
                db.folderDao().insertAll(
                    listOf(
                        FolderEntity(1, "Gyártási Útmutatók", null),
                        FolderEntity(2, "Kapcsolási Rajzok", null),
                        FolderEntity(3, "Minőségbiztosítás", null),
                        FolderEntity(11, "Hidraulika Leírások", 1),
                        FolderEntity(21, "PLC kapcsolások", 2)
                    )
                )
            }

            if (db.documentDao().getDocumentCount() == 0) {
                db.documentDao().insertAll(
                    listOf(
                        DocumentEntity(1001, 1, "Gyártási Standard Működési Eljárás 2026.pdf", "gyartas_sop_2026.pdf", "/docs/sop.pdf", 1024000L, "2026-01-10"),
                        DocumentEntity(1002, 11, "Hidraulikus Szivattyú Karbantartási Útmutató.pdf", "hidraulika_karbantartas.pdf", "/docs/hyd.pdf", 2048000L, "2026-01-12"),
                        DocumentEntity(2001, 21, "Főelosztó Szekrény Kapcsolási Rajz v2.pdf", "kapcsolasi_rajz_v2.pdf", "/docs/rajz.pdf", 5120000L, "2026-02-01")
                    )
                )
            }
        }
    }
}
