package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.dao.CropDao
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.farm.Crop

class CropRepository(
    private val cropDao: CropDao
) {

    suspend fun getByFarmId(farmId: String): List<Crop> {
        return cropDao.getByFarmId(farmId).map { it.toDomain() }
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