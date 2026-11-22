package com.nutriagricola.app.data.local.dao

import androidx.room.Upsert

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nutriagricola.app.data.local.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Query("""SELECT r.* FROM records r
        JOIN crops c ON c.id = r.cropId
        JOIN farm_users fu ON fu.farmId = c.farmId
        WHERE fu.userId = :userId ORDER BY r.date DESC, r.id DESC""")
    suspend fun getByAssignedUser(userId: String): List<RecordEntity>

    @Upsert
    suspend fun upsert(record: RecordEntity)

    @Insert
    suspend fun insert(record: RecordEntity)

    @Query("SELECT * FROM records")
    suspend fun getAll(): List<RecordEntity>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: String): RecordEntity?

    @Query("SELECT * FROM records WHERE cropId = :cropId")
    suspend fun getByCropId(cropId: String): List<RecordEntity>

    @Query("""
        SELECT r.* FROM records AS r
        INNER JOIN crops AS c ON c.id = r.cropId
        WHERE r.userId = :userId AND c.farmId = :farmId
        ORDER BY r.date DESC, r.rowid DESC
    """)
    fun observeByUserAndFarm(userId: String, farmId: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE status = :status")
    suspend fun getByStatus(status: String): List<RecordEntity>

    @Update
    suspend fun update(record: RecordEntity)

    @Delete
    suspend fun delete(record: RecordEntity)
}
