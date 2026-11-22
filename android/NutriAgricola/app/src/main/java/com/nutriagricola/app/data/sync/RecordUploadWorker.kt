package com.nutriagricola.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.remote.ApiClient
import com.nutriagricola.app.data.remote.PhotoFileTransfer
import com.nutriagricola.app.data.repository.*
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

class RecordUploadWorker(
    context: Context,
    parameters: WorkerParameters,
    private val uploader: RecordUploadRepository
) : CoroutineWorker(context, parameters) {
    constructor(context: Context, parameters: WorkerParameters) :
        this(context, parameters, repository(context))

    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId")?.takeIf { it.isNotBlank() }
            ?: return failure("No se pudo identificar la cuenta del envío.")
        return try {
            val count = uploader.uploadPending(userId)
            Result.success(workDataOf("uploaded" to count, "finishedAt" to System.currentTimeMillis()))
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: UploadSessionRequiredException) {
            failure("Inicia sesión nuevamente para enviar los registros pendientes.")
        } catch (_: IOException) {
            Result.retry()
        } catch (exception: HttpException) {
            if (exception.code() >= 500 || exception.code() in listOf(408, 429)) Result.retry()
            else failure(when (exception.code()) {
                401 -> "La sesión venció. Inicia sesión nuevamente para enviar pendientes."
                403 -> "No tienes permiso para enviar un registro. Actualiza tus predios."
                409 -> "Un registro cambió en el servidor. Revisa sus datos antes de reenviar."
                else -> "Un registro o foto no pudo enviarse. Revisa los datos y vuelve a intentar."
            })
        } catch (exception: IllegalArgumentException) {
            failure(exception.message ?: "Revisa el formato y tamaño de las fotos.")
        } catch (_: Exception) {
            failure("No se pudo completar el envío. Los registros siguen guardados en el dispositivo.")
        }
    }

    private fun failure(message: String) = Result.failure(workDataOf(
        "message" to message, "finishedAt" to System.currentTimeMillis()
    ))

    companion object {
        private fun repository(context: Context): RecordUploadRepository {
            val database = AppDatabase.getDatabase(context)
            val api = ApiClient()
            val sessions = SessionRepository(context, UserRepository(database.userDao()), api.authApi)
            return RecordUploadRepository(database, api, sessions, RecordRepository(database), PhotoFileTransfer(context))
        }
    }
}
