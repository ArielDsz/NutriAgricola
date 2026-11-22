package com.nutriagricola.app.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.RecordRemoteRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.data.sync.RecordUploadScheduler
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class RecordDownloadUiState(
    val isBusy: Boolean = false,
    val hasMore: Boolean = false,
    val completedRefresh: Int = 0,
    val error: String? = null,
    val pendingUploads: Int = 0,
    val isUploading: Boolean = false,
    val uploadMessage: String? = null
)

class RecordDownloadViewModel(
    private val repository: RecordRemoteRepository,
    private val records: RecordRepository,
    private val uploads: RecordUploadScheduler,
    private val canUpload: Boolean,
    private val userId: String,
    private val onSessionExpired: () -> Unit
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordDownloadUiState())
    val uiState = _uiState.asStateFlow()
    private var nextPage = 0
    private var job: Job? = null
    private var uploadObservation: Job? = null

    fun start() {
        if (canUpload && userId.isNotBlank()) {
            uploadObservation?.cancel()
            uploadObservation = viewModelScope.launch {
                combine(records.observePendingUploadCount(userId), uploads.observe(userId)) { count, work ->
                    val active = work.any { !it.state.isFinished }
                    val running = work.any { it.state == WorkInfo.State.RUNNING }
                    val last = work.maxByOrNull { it.outputData.getLong("finishedAt", 0) }
                    val message = if (count > 0 && !active && last?.state == WorkInfo.State.FAILED)
                        last.outputData.getString("message") else null
                    Triple(count, running, message)
                }.collect { (count, running, message) ->
                    _uiState.update { it.copy(pendingUploads = count, isUploading = running, uploadMessage = message) }
                }
            }
            uploads.enqueue(userId)
        }
        refresh()
    }

    fun uploadPending() { if (canUpload) uploads.enqueue(userId, replaceWaiting = true) }

    fun refresh() = download(0)
    fun loadMore() { if (_uiState.value.hasMore) download(nextPage) }
    fun cancelDownload() { job?.cancel(); uploadObservation?.cancel() }

    private fun download(page: Int) {
        if (_uiState.value.isBusy || userId.isBlank()) return
        _uiState.update { it.copy(isBusy = true, error = null) }
        job = viewModelScope.launch {
            try {
                val result = repository.downloadPage(userId, page)
                nextPage = result.page + 1
                _uiState.update { it.copy(hasMore = nextPage < result.totalPages,
                    completedRefresh = it.completedRefresh + 1) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: IOException) {
                _uiState.update { it.copy(error = "No se pudo conectar. Se conservan los registros guardados.") }
            } catch (exception: HttpException) {
                if (exception.code() == 401) onSessionExpired()
                else _uiState.update { it.copy(error = "No se pudieron descargar los registros. Inténtalo nuevamente.") }
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "No se pudieron actualizar los registros. Se conservan los datos locales.") }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
            }
        }
    }
}
