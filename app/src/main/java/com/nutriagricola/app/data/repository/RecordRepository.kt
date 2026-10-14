package com.nutriagricola.app.data.repository

import androidx.room.withTransaction
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.entity.FoliarPhotoEntity
import com.nutriagricola.app.data.local.entity.RecordEntity
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.record.FoliarData
import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.record.RecordData
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.domain.model.record.SoilData
import com.nutriagricola.app.domain.model.record.WaterData
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecordRepository(
    private val database: AppDatabase
) {

    private val recordDao = database.recordDao()
    private val soilDataDao = database.soilDataDao()
    private val waterDataDao = database.waterDataDao()
    private val foliarDataDao = database.foliarDataDao()
    private val foliarPhotoDao = database.foliarPhotoDao()

    suspend fun getAll(): List<Record> {
        return recordDao.getAll().mapNotNull {
            getCompleteRecord(it)
        }
    }

    suspend fun getById(id: String): Record? {
        val record = recordDao.getById(id) ?: return null
        return getCompleteRecord(record)
    }

    suspend fun getByCropId(cropId: String): List<Record> {
        return recordDao.getByCropId(cropId).mapNotNull {
            getCompleteRecord(it)
        }
    }

    fun observeByUserAndFarm(userId: String, farmId: String): Flow<List<Record>> {
        return recordDao.observeByUserAndFarm(userId, farmId).map { records ->
            database.withTransaction {
                records.mapNotNull { getCompleteRecord(it) }
            }
        }
    }

    suspend fun insert(record: Record) {
        database.withTransaction {

            recordDao.insert(record.toEntity())

            when (val data = record.data) {

                is SoilData -> {
                    soilDataDao.insert(
                        data.toEntity(record.id)
                    )
                }

                is WaterData -> {
                    waterDataDao.insert(
                        data.toEntity(record.id)
                    )
                }

                is FoliarData -> {
                    foliarDataDao.insert(
                        data.toEntity(record.id)
                    )

                    data.photos.forEach { path ->
                        foliarPhotoDao.insert(
                            FoliarPhotoEntity(
                                id = UUID.randomUUID().toString(),
                                recordId = record.id,
                                path = path
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun update(record: Record) {
        database.withTransaction {

            recordDao.update(record.toEntity())

            when (val data = record.data) {

                is SoilData -> {
                    soilDataDao.update(
                        data.toEntity(record.id)
                    )
                }

                is WaterData -> {
                    waterDataDao.update(
                        data.toEntity(record.id)
                    )
                }

                is FoliarData -> {
                    foliarDataDao.update(
                        data.toEntity(record.id)
                    )
                }
            }
        }
    }

    suspend fun delete(record: Record) {
        recordDao.delete(record.toEntity())
    }

    private suspend fun getCompleteRecord(
        record: RecordEntity
    ): Record? {

        val data: RecordData = when (
            RecordType.valueOf(record.type)
        ) {

            RecordType.SOIL -> {
                soilDataDao
                    .getByRecordId(record.id)
                    ?.toDomain()
                    ?: return null
            }

            RecordType.WATER -> {
                waterDataDao
                    .getByRecordId(record.id)
                    ?.toDomain()
                    ?: return null
            }

            RecordType.FOLIAR -> {
                val foliarData =
                    foliarDataDao.getByRecordId(record.id)
                        ?: return null

                val photos =
                    foliarPhotoDao
                        .getByRecordId(record.id)
                        .map { it.path }

                foliarData.toDomain(photos)
            }
        }

        return record.toDomain(data)
    }
}
