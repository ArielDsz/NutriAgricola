package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.entity.FoliarPhotoEntity
import com.nutriagricola.app.data.local.entity.RemotePhotoEntity
import com.nutriagricola.app.data.remote.ApiClient
import com.nutriagricola.app.data.remote.PhotoFileTransfer
import com.nutriagricola.app.domain.model.record.Record

class RecordPhotoRepository(
    private val database: AppDatabase,
    private val api: ApiClient,
    private val sessions: SessionRepository,
    private val files: PhotoFileTransfer,
    private val records: RecordRepository
) {
    suspend fun download(recordId: String, userId: String): Record {
        check(sessions.hasSession(userId)) { "Vuelve a iniciar sesión para descargar las fotos." }
        val authorization = "Bearer " + requireNotNull(sessions.getAccessToken())
        var metadata = database.remotePhotoDao().getByRecordId(recordId)
        if (metadata.isEmpty()) {
            val detail = api.recordApi.getDetail(recordId, authorization)
            require(detail.id == recordId)
            metadata = detail.foliarPhotos.map { RemotePhotoEntity(it.id, recordId, it.uploaded) }
            database.remotePhotoDao().upsertAll(metadata)
        }
        for (photo in metadata.filter { it.uploaded }) {
            check(sessions.hasSession(userId))
            val local = records.getPhotos(recordId).find { it.id == photo.id }
            if (local != null && files.isAvailable(recordId, local.path)) continue
            val response = api.recordApi.downloadPhoto(recordId, photo.id, authorization)
            val path = files.saveDownloaded(recordId, photo.id, response)
            if (!sessions.hasSession(userId)) throw UploadSessionRequiredException()
            database.foliarPhotoDao().upsert(FoliarPhotoEntity(photo.id, recordId, path))
        }
        return requireNotNull(records.getById(recordId))
    }
}
