package com.nutriagricola.app.data.local.dao

import androidx.room.*
import com.nutriagricola.app.data.local.entity.ProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Query("SELECT * FROM programs ORDER BY startDate DESC, id ASC")
    fun observeAll(): Flow<List<ProgramEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSummary(row: ProgramEntity)

    @Query("UPDATE programs SET startDate = :startDate, summaryJson = :json WHERE id = :id")
    suspend fun updateSummary(id: String, startDate: String, json: String)

    @Transaction
    suspend fun cacheSummaries(rows: List<ProgramEntity>) {
        rows.forEach { row ->
            insertSummary(row)
            updateSummary(row.id, row.startDate, row.summaryJson)
        }
    }

    @Upsert
    suspend fun cacheDetail(row: ProgramEntity)
}
