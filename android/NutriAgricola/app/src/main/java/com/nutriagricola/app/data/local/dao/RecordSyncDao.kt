package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nutriagricola.app.data.local.entity.RecordSyncEntity

@Dao
interface RecordSyncDao {
    @Query("SELECT * FROM record_sync WHERE recordId = :recordId")
    suspend fun getById(recordId: String): RecordSyncEntity?

    @Upsert
    suspend fun upsert(state: RecordSyncEntity)

    @Query("UPDATE record_sync SET dirty = 1 WHERE recordId = :recordId")
    suspend fun markDirty(recordId: String)
}
