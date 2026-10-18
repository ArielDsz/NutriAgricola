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
        CropEntity::class,
        RecordEntity::class,
        SoilDataEntity::class,
        WaterDataEntity::class,
        FoliarDataEntity::class,
        FoliarPhotoEntity::class,
        RecordReviewEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun userCredentialDao(): UserCredentialDao
    abstract fun farmDao(): FarmDao
    abstract fun cropDao(): CropDao
    abstract fun recordDao(): RecordDao
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
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
