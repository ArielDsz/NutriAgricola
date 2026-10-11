package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.dao.FarmDao
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.farm.Farm

class FarmRepository(
    private val farmDao: FarmDao
) {

    suspend fun getAll(): List<Farm> {
        return farmDao.getAll().map { it.toDomain() }
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