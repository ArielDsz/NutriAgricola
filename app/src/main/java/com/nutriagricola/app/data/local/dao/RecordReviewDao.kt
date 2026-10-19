package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nutriagricola.app.data.local.entity.RecordReviewEntity

@Dao
interface RecordReviewDao {

    @Insert
    suspend fun insert(review: RecordReviewEntity)

    @Query("SELECT * FROM record_reviews WHERE recordId = :recordId ORDER BY date DESC, rowid DESC")
    suspend fun getByRecordId(recordId: String): List<RecordReviewEntity>

    @Query("SELECT * FROM record_reviews ORDER BY date DESC, rowid DESC")
    suspend fun getAll(): List<RecordReviewEntity>
}
