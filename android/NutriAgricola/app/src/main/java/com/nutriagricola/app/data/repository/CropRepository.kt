package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.dao.CropDao
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.farm.Crop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.nutriagricola.app.data.remote.api.CropApi

class CropRepository(
    private val cropDao: CropDao,
    private val cropApi: CropApi,
    private val sessionRepository: SessionRepository
) {

    suspend fun refresh(farmId: String, userId: String) {
        check(sessionRepository.hasSession(userId)) { "No hay una sesión autenticada" }
        val token = requireNotNull(sessionRepository.getAccessToken())
        val crops = cropApi.getByFarm(farmId, "Bearer $token")
        if (!sessionRepository.hasSession(userId)) return
        cropDao.replaceRemoteCrops(farmId, crops.distinctBy { it.id }.map { it.toEntity() })
    }

    suspend fun getByFarmId(farmId: String): List<Crop> {
        return cropDao.getByFarmId(farmId).map { it.toDomain() }
    }

    fun observeByFarmId(farmId: String): Flow<List<Crop>> {
        return cropDao.observeByFarmId(farmId).map { crops -> crops.map { it.toDomain() } }
    }

    suspend fun getById(id: String): Crop? {
        return cropDao.getById(id)?.toDomain()
    }

    suspend fun insert(crop: Crop) {
        cropDao.insert(crop.toEntity())
    }

    suspend fun update(crop: Crop) {
        cropDao.update(crop.toEntity())
    }

    suspend fun delete(crop: Crop) {
        cropDao.delete(crop.toEntity())
    }
}
