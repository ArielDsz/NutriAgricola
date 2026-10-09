package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nutriagricola.app.data.local.entity.FoliarDataEntity

@Dao
interface FoliarDataDao {

    @Insert
    suspend fun insert(foliarData: FoliarDataEntity)

    @Query("SELECT * FROM foliar_data WHERE recordId = :recordId")
    suspend fun getByRecordId(recordId: String): FoliarDataEntity?

    @Update
    suspend fun update(foliarData: FoliarDataEntity)

    @Delete
    suspend fun delete(foliarData: FoliarDataEntity)
}