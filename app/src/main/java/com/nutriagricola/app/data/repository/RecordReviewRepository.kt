package com.nutriagricola.app.data.repository

import androidx.room.withTransaction
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.advisor.RecordReview
import com.nutriagricola.app.domain.model.advisor.ReviewResult
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.user.UserRole

class RecordReviewRepository(
    private val database: AppDatabase
) {
    private val recordReviewDao = database.recordReviewDao()
    private val recordDao = database.recordDao()

    suspend fun saveReviewAndUpdateStatus(review: RecordReview) {
        require(review.result != ReviewResult.OBSERVED || review.comment.isNotBlank())
        database.withTransaction {
            val advisor = database.userDao().getById(review.advisorId)
            require(advisor != null && advisor.active && advisor.role == UserRole.ADVISOR.name) {
                "Sesión de asesor no válida"
            }
            val recordEntity = requireNotNull(recordDao.getById(review.recordId)) {
                "Análisis no encontrado"
            }
            require(recordEntity.status in listOf(
                RecordStatus.PENDING.name, RecordStatus.VIEWED.name, RecordStatus.VALIDATED.name
            )) { "Este análisis no está disponible para revisión" }
            val newStatus = when (review.result) {
                ReviewResult.OBSERVED -> RecordStatus.PENDING
                ReviewResult.VALIDATED -> RecordStatus.VALIDATED
            }
            recordReviewDao.insert(review.toEntity())
            recordDao.update(recordEntity.copy(status = newStatus.name))
        }
    }

    suspend fun getReviewsForRecord(recordId: String): List<RecordReview> {
        return recordReviewDao.getByRecordId(recordId).map { it.toDomain() }
    }

    suspend fun getLatestReviews(): Map<String, RecordReview> {
        return recordReviewDao.getAll().groupBy { it.recordId }
            .mapValues { (_, reviews) -> reviews.first().toDomain() }
    }
}
