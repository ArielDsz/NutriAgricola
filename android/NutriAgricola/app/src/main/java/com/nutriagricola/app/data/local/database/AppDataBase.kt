package com.nutriagricola.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nutriagricola.app.data.local.dao.*
import com.nutriagricola.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        UserCredentialEntity::class,
        FarmEntity::class,
        FarmUserEntity::class,
        CropEntity::class,
        RecordEntity::class,
        RecordSyncEntity::class,
        RemotePhotoEntity::class,
        SoilDataEntity::class,
        WaterDataEntity::class,
        FoliarDataEntity::class,
        FoliarPhotoEntity::class,
        RecordReviewEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun userCredentialDao(): UserCredentialDao
    abstract fun farmDao(): FarmDao
    abstract fun cropDao(): CropDao
    abstract fun recordDao(): RecordDao
    abstract fun recordSyncDao(): RecordSyncDao
    abstract fun remotePhotoDao(): RemotePhotoDao
    abstract fun soilDataDao(): SoilDataDao
    abstract fun waterDataDao(): WaterDataDao
    abstract fun foliarDataDao(): FoliarDataDao
    abstract fun foliarPhotoDao(): FoliarPhotoDao
    abstract fun recordReviewDao(): RecordReviewDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutri_agricola_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
