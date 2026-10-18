package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutriagricola.app.data.local.entity.RecordReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: RecordReviewEntity)

    @Query("SELECT * FROM record_reviews WHERE recordId = :recordId ORDER BY date DESC LIMIT 1")
    suspend fun getByRecordId(recordId: String): RecordReviewEntity?

    @Query("SELECT * FROM record_reviews ORDER BY date DESC")
    fun observeAll(): Flow<List<RecordReviewEntity>>

    @Query("SELECT * FROM record_reviews ORDER BY date DESC")
    suspend fun getAll(): List<RecordReviewEntity>
}
