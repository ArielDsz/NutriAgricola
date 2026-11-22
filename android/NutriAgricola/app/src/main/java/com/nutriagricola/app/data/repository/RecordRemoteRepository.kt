package com.nutriagricola.app.data.repository

import androidx.room.withTransaction
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.data.remote.ApiClient
import com.nutriagricola.app.data.remote.dto.RecordPageDto
import com.nutriagricola.app.data.remote.dto.ReviewCreateRequestDto
import com.nutriagricola.app.data.remote.dto.ReviewCreateResponseDto
import com.nutriagricola.app.domain.model.advisor.RecordReview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

class RecordRemoteRepository(
    private val database: AppDatabase,
    private val apiClient: ApiClient,
    private val sessions: SessionRepository,
    private val farms: FarmRepository,
    private val records: RecordRepository
) {
    private val operationMutex = Mutex()

    suspend fun submitReview(review: RecordReview): ReviewCreateResponseDto = operationMutex.withLock {
        check(sessions.hasSession(review.advisorId)) { "Sesión de asesor no válida" }
        val sync = requireNotNull(records.getSyncState(review.recordId)) { "El análisis no se ha descargado" }
        check(!sync.dirty) { "Hay cambios locales pendientes" }
        val token = requireNotNull(sessions.getAccessToken())
        val response = apiClient.reviewApi.create("Bearer " + token, ReviewCreateRequestDto(
            review.id, review.recordId, review.date.toString(), review.result.name, review.comment
        ))
        require(response.id == review.id && response.recordId == review.recordId && response.advisorId == review.advisorId)
        if (sessions.hasSession(review.advisorId)) {
            RecordReviewRepository(database).saveConfirmedRemoteReview(response)
        }
        response
    }

    suspend fun downloadPage(userId: String, page: Int): RecordPageDto = operationMutex.withLock { coroutineScope {
        check(sessions.hasSession(userId)) { "No hay una sesión autenticada" }
        val authorization = "Bearer " + requireNotNull(sessions.getAccessToken())
        farms.refresh(userId)
        val result = apiClient.recordApi.getHistory(authorization, page)
        require(result.page == page && result.content.size <= 20)
        // Bound detail requests to four at a time and never download photo files here.
        val limit = Semaphore(4)
        val details = result.content.map { summary ->
            async {
                limit.withPermit {
                    apiClient.recordApi.getDetail(summary.id, authorization).also {
                        require(it.id == summary.id && it.cropId == summary.cropId && it.userId == summary.userId)
                    }
                }
            }
        }.awaitAll()
        val assignedFarmIds = farms.getByUser(userId).map { it.id }.toSet()
        val crops = details.map { it.cropId }.distinct().map { id ->
            apiClient.cropApi.getById(id, authorization).also {
                require(it.id == id && it.farmId in assignedFarmIds)
            }
        }
        if (!sessions.hasSession(userId)) return@coroutineScope result
        database.withTransaction {
            database.cropDao().upsertAll(crops.map { it.toEntity() })
            details.forEach { records.cacheRemoteRecord(it) }
        }
        result
    } }
}
