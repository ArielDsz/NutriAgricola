package com.nutriagricola.app.data.local.dao

import androidx.room.*
import com.nutriagricola.app.data.local.entity.RemotePhotoEntity

@Dao
interface RemotePhotoDao {
    @Upsert
    suspend fun upsertAll(photos: List<RemotePhotoEntity>)

    @Query("SELECT * FROM remote_photos WHERE recordId = :recordId ORDER BY id")
    suspend fun getByRecordId(recordId: String): List<RemotePhotoEntity>
}
