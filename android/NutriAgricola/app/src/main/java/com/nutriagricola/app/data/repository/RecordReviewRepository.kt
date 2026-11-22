package com.nutriagricola.app.data.repository

import androidx.room.withTransaction
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.advisor.RecordReview
import com.nutriagricola.app.domain.model.advisor.ReviewResult
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.user.UserRole
import com.nutriagricola.app.data.remote.dto.ReviewCreateResponseDto
import com.nutriagricola.app.data.local.entity.RecordReviewEntity

class RecordReviewRepository(
    private val database: AppDatabase
) {
    private val recordReviewDao = database.recordReviewDao()
    private val recordDao = database.recordDao()

    suspend fun saveConfirmedRemoteReview(response: ReviewCreateResponseDto) {
        val status = RecordStatus.valueOf(response.recordStatus)
        require(status == RecordStatus.PENDING || status == RecordStatus.VALIDATED)
        ReviewResult.valueOf(response.result)
        database.withTransaction {
            val record = requireNotNull(recordDao.getById(response.recordId))
            val sync = requireNotNull(database.recordSyncDao().getById(response.recordId))
            check(!sync.dirty) { "Hay cambios locales pendientes" }
            recordReviewDao.upsert(RecordReviewEntity(
                response.id, response.recordId, response.advisorId, response.advisorName,
                response.date, response.result, response.comment
            ))
            recordDao.update(record.copy(status = status.name))
        }
    }

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
            database.recordSyncDao().markDirty(review.recordId)
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
