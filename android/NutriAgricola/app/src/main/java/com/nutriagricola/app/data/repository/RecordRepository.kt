package com.nutriagricola.app.data.repository

import androidx.room.withTransaction
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.entity.FoliarPhotoEntity
import com.nutriagricola.app.data.local.entity.RecordEntity
import com.nutriagricola.app.data.local.entity.RecordSyncEntity
import com.nutriagricola.app.data.local.entity.UserEntity
import com.nutriagricola.app.data.local.entity.RecordReviewEntity
import com.nutriagricola.app.data.remote.dto.RecordDetailDto
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.record.FoliarData
import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.record.RecordData
import com.nutriagricola.app.domain.model.record.RecordStatus
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

    suspend fun getByAssignedUser(userId: String): List<Record> =
        recordDao.getByAssignedUser(userId).mapNotNull { getCompleteRecord(it) }

    suspend fun getSyncState(id: String) = database.recordSyncDao().getById(id)

    suspend fun cacheRemoteRecord(detail: RecordDetailDto): Boolean {
        val record = detail.toDomain()
        return database.withTransaction {
            val existing = recordDao.getById(record.id)
            val sync = database.recordSyncDao().getById(record.id)
            if (existing != null && (sync == null || sync.dirty)) return@withTransaction false
            if (existing != null) {
                require(existing.cropId == record.cropId && existing.userId == record.userId && existing.type == record.type.name)
            }
            ensureReferenceUser(record.userId, "FARMER")
            recordDao.upsert(record.toEntity())
            when (val data = record.data) {
                is SoilData -> soilDataDao.upsert(data.toEntity(record.id))
                is WaterData -> waterDataDao.upsert(data.toEntity(record.id))
                is FoliarData -> foliarDataDao.upsert(data.toEntity(record.id))
            }
            detail.reviews.forEach { review ->
                ensureReferenceUser(review.advisorId, "ADVISOR")
                database.recordReviewDao().upsert(RecordReviewEntity(
                    review.id, record.id, review.advisorId, review.advisorName,
                    review.date, review.result, review.comment
                ))
            }
            database.recordSyncDao().upsert(RecordSyncEntity(
                record.id, false, detail.foliarPhotos.count { it.uploaded }
            ))
            true
        }
    }

    private suspend fun ensureReferenceUser(id: String, role: String) {
        if (database.userDao().getById(id) == null) {
            // Only the UUID is available. Do not invent names, emails or an active account.
            database.userDao().insert(UserEntity(id, "", "", "", role, false))
        }
    }

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

    suspend fun getByStatus(status: RecordStatus): List<Record> {
        return recordDao.getByStatus(status.name).mapNotNull {
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

            database.recordSyncDao().markDirty(record.id)

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
