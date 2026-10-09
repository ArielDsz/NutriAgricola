package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nutriagricola.app.data.local.entity.RecordEntity

@Dao
interface RecordDao {

    @Insert
    suspend fun insert(record: RecordEntity)

    @Query("SELECT * FROM records")
    suspend fun getAll(): List<RecordEntity>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: String): RecordEntity?

    @Query("SELECT * FROM records WHERE cropId = :cropId")
    suspend fun getByCropId(cropId: String): List<RecordEntity>

    @Query("SELECT * FROM records WHERE status = :status")
    suspend fun getByStatus(status: String): List<RecordEntity>

    @Update
    suspend fun update(record: RecordEntity)

    @Delete
    suspend fun delete(record: RecordEntity)
}