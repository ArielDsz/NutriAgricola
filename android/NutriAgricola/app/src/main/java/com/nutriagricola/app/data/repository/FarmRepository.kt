package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.dao.FarmDao
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.farm.Farm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.nutriagricola.app.data.remote.api.FarmApi

class FarmRepository(
    private val farmDao: FarmDao,
    private val farmApi: FarmApi,
    private val sessionRepository: SessionRepository
) {

    fun observeByUser(userId: String): Flow<List<Farm>> =
        farmDao.observeByUser(userId).map { farms -> farms.map { it.toDomain() } }

    suspend fun refresh(userId: String) {
        check(sessionRepository.hasSession(userId)) { "No hay una sesión autenticada" }
        val token = requireNotNull(sessionRepository.getAccessToken())
        val farms = farmApi.getFarms("Bearer $token")
        // Do not apply a response to an account that has since logged out.
        if (!sessionRepository.hasSession(userId)) return
        farmDao.replaceAssignedFarms(userId, farms.distinctBy { it.id }.map { it.toEntity() })
    }

    suspend fun getAll(): List<Farm> {
        return farmDao.getAll().map { it.toDomain() }
    }

    fun observeAll(): Flow<List<Farm>> {
        return farmDao.observeAll().map { farms -> farms.map { it.toDomain() } }
    }

    suspend fun insert(farm: Farm) {
        farmDao.insert(farm.toEntity())
    }

    suspend fun update(farm: Farm) {
        farmDao.update(farm.toEntity())
    }

    suspend fun delete(farm: Farm) {
        farmDao.delete(farm.toEntity())
    }
}
