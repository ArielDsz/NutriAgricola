package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.dao.ProgramDao
import com.nutriagricola.app.data.local.entity.ProgramEntity
import com.nutriagricola.app.data.mapper.*
import com.nutriagricola.app.data.remote.api.ProgramApi
import com.nutriagricola.app.data.remote.dto.*
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ProgramRepository(
    private val dao: ProgramDao,
    private val api: ProgramApi,
    private val authorization: suspend (String) -> String
) {
    constructor(dao: ProgramDao, api: ProgramApi, session: SessionRepository) : this(dao, api, { userId ->
        check(session.hasSession(userId)) { "Debes volver a iniciar sesión" }
        "Bearer ${checkNotNull(session.getAccessToken()) { "Debes volver a iniciar sesión" }}"
    })

    private val json = Json { ignoreUnknownKeys = true }

    fun observePrograms() = dao.observeAll().map { rows -> rows.map { row ->
        val summary = json.decodeFromString<ProgramSummaryDto>(row.summaryJson)
        summary.toDomain(row.detailJson?.let { json.decodeFromString<ProgramResponseDto>(it) })
    } }

    suspend fun downloadPage(userId: String, page: Int): Boolean {
        val result = api.list(authorization(userId), page)
        authorization(userId) // No confirmar una respuesta después de cambiar/cerrar la sesión.
        dao.cacheSummaries(result.content.map { ProgramEntity(it.id, it.startDate, json.encodeToString(it), null) })
        return result.page + 1 < result.totalPages
    }

    suspend fun downloadDetail(userId: String, id: String) {
        val detail = api.get(authorization(userId), id)
        check(detail.id == id) { "El programa recibido no coincide con el solicitado" }
        authorization(userId)
        cache(detail)
    }

    suspend fun create(userId: String, request: ProgramRequestDto) {
        val result = api.create(authorization(userId), request)
        check(result.id == request.id && result.advisorId == userId) { "No se pudo confirmar el programa guardado" }
        authorization(userId)
        cache(result)
    }

    private suspend fun cache(detail: ProgramResponseDto) {
        dao.cacheDetail(ProgramEntity(detail.id, detail.content.startDate,
            json.encodeToString(detail.toSummary()), json.encodeToString(detail)))
    }
}
