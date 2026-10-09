package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.nutriagricola.app.data.local.entity.FoliarPhotoEntity

@Dao
interface FoliarPhotoDao {

    @Insert
    suspend fun insert(photo: FoliarPhotoEntity)

    @Query("SELECT * FROM foliar_photos WHERE recordId = :recordId")
    suspend fun getByRecordId(recordId: String): List<FoliarPhotoEntity>

    @Delete
    suspend fun delete(photo: FoliarPhotoEntity)
}