package com.nutriagricola.app.data.repository

import androidx.room.withTransaction
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.advisor.RecordReview
import com.nutriagricola.app.domain.model.record.RecordStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecordReviewRepository(
    private val database: AppDatabase
) {
    private val recordReviewDao = database.recordReviewDao()
    private val recordDao = database.recordDao()

    suspend fun saveReviewAndUpdateStatus(review: RecordReview, newStatus: RecordStatus) {
        database.withTransaction {
            recordReviewDao.insert(review.toEntity())
            val recordEntity = recordDao.getById(review.recordId)
            if (recordEntity != null) {
                recordDao.update(
                    recordEntity.copy(status = newStatus.name)
                )
            }
        }
    }

    suspend fun getReviewForRecord(recordId: String): RecordReview? {
        return recordReviewDao.getByRecordId(recordId)?.toDomain()
    }

    fun observeAllReviews(): Flow<List<RecordReview>> {
        return recordReviewDao.observeAll().map { list ->
            list.map { it.toDomain() }
        }
    }
}
