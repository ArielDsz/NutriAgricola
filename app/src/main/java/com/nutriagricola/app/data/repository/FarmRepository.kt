package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.dao.FarmDao
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.farm.Farm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FarmRepository(
    private val farmDao: FarmDao
) {

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
