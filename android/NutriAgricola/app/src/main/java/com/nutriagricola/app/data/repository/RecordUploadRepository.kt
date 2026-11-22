package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.mapper.toCreateRequest
import com.nutriagricola.app.data.mapper.toUpdateRequest
import com.nutriagricola.app.data.remote.ApiClient
import com.nutriagricola.app.data.remote.PhotoFileTransfer

class UploadSessionRequiredException : Exception("Vuelve a iniciar sesión para enviar los registros pendientes.")

class RecordUploadRepository(
    private val database: AppDatabase,
    private val api: ApiClient,
    private val sessions: SessionRepository,
    private val records: RecordRepository,
    private val photos: PhotoFileTransfer
) {
    suspend fun uploadPending(userId: String): Int {
        var uploaded = 0
        var permanentFailure: Exception? = null
        for (record in records.getPendingUploads(userId)) {
            try {
                if (uploadRecord(userId, record)) uploaded++
            } catch (exception: retrofit2.HttpException) {
                if (exception.code() >= 500 || exception.code() in listOf(401, 408, 429)) throw exception
                // One invalid record must not prevent other valid records from being sent.
                if (permanentFailure == null) permanentFailure = exception
            } catch (exception: IllegalArgumentException) {
                if (permanentFailure == null) permanentFailure = exception
            }
        }
        permanentFailure?.let { throw it }
        return uploaded
    }

    private suspend fun uploadRecord(userId: String,
        record: com.nutriagricola.app.domain.model.record.Record): Boolean {
        if (!sessions.hasSession(userId)) throw UploadSessionRequiredException()
        val user = database.userDao().getById(userId)
        check(user?.active == true && user.role == "FARMER") { "Solo el responsable puede enviar sus registros." }
        val authorization = "Bearer " + (sessions.getAccessToken() ?: throw UploadSessionRequiredException())
        val localPhotos = records.getPhotos(record.id)
        val request = record.toCreateRequest(localPhotos)
        val sync = records.getSyncState(record.id)
        if (sync == null) {
            val created = api.recordApi.create(authorization, request)
            require(created.id == record.id)
        } else {
            val updated = api.recordApi.update(record.id, authorization, request.toUpdateRequest())
            require(updated.id == record.id)
        }
        var detail = api.recordApi.getDetail(record.id, authorization)
        for (photo in localPhotos) {
            if (!sessions.hasSession(userId)) throw UploadSessionRequiredException()
            val serverPhoto = detail.foliarPhotos.find { it.id == photo.id }
            requireNotNull(serverPhoto) { "Las fotos del análisis cambiaron. Revisa el registro." }
            if (!serverPhoto.uploaded) {
                api.recordApi.uploadPhoto(record.id, photo.id, authorization,
                    photos.uploadPart(record.id, photo.path))
            }
        }
        if (localPhotos.isNotEmpty()) detail = api.recordApi.getDetail(record.id, authorization)
        if (!localPhotos.all { local -> detail.foliarPhotos.any { it.id == local.id && it.uploaded } }) {
            throw java.io.IOException("El servidor aún no confirmó todas las fotos.")
        }
        if (!sessions.hasSession(userId)) throw UploadSessionRequiredException()
        return records.confirmUpload(record, detail)
    }
}
