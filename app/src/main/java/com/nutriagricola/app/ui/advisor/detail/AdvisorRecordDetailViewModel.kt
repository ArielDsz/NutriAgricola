package com.nutriagricola.app.ui.advisor.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.data.repository.RecordReviewRepository
import com.nutriagricola.app.data.repository.UserRepository
import com.nutriagricola.app.domain.model.advisor.RecordReview
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.domain.model.farm.Farm
import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.user.User
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdvisorRecordDetailUiState(
    val record: Record? = null,
    val farm: Farm? = null,
    val crop: Crop? = null,
    val farmerUser: User? = null,
    val review: RecordReview? = null,
    val commentInput: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AdvisorRecordDetailViewModel(
    private val recordId: String,
    private val getCurrentUser: () -> User?,
    private val recordRepository: RecordRepository,
    private val recordReviewRepository: RecordReviewRepository,
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvisorRecordDetailUiState())
    val uiState: StateFlow<AdvisorRecordDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val record = recordRepository.getById(recordId)
                if (record == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Análisis no encontrado") }
                    return@launch
                }
                val crop = cropRepository.getById(record.cropId)
                val farm = crop?.let { farmRepository.getAll().find { f -> f.id == it.farmId } }
                val farmerUser = userRepository.getById(record.userId)
                val review = recordReviewRepository.getReviewForRecord(recordId)

                _uiState.update {
                    it.copy(
                        record = record,
                        crop = crop,
                        farm = farm,
                        farmerUser = farmerUser,
                        review = review,
                        commentInput = review?.comment ?: "",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "No se pudieron cargar los detalles del análisis")
                }
            }
        }
    }

    fun onCommentChange(comment: String) {
        _uiState.update { it.copy(commentInput = comment) }
    }

    fun submitReview(result: RecordStatus) {
        val currentUser = getCurrentUser()
        if (currentUser == null) {
            _uiState.update { it.copy(error = "Sesión de asesor no válida") }
            return
        }

        val comment = _uiState.value.commentInput.trim()
        if (result == RecordStatus.VIEWED && comment.isEmpty()) {
            _uiState.update { it.copy(error = "Para observar un análisis debes ingresar una observación") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val review = RecordReview(
                    id = UUID.randomUUID().toString(),
                    recordId = recordId,
                    advisorId = currentUser.id,
                    advisorName = "${currentUser.firstName} ${currentUser.lastName}",
                    date = LocalDateTime.now(),
                    result = result,
                    comment = if (comment.isEmpty()) "Análisis validado formalmente por el asesor." else comment
                )

                recordReviewRepository.saveReviewAndUpdateStatus(review, result)

                val message = if (result == RecordStatus.VALIDATED) {
                    "Análisis validado exitosamente"
                } else {
                    "Análisis observado y marcado como revisado"
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        review = review,
                        record = it.record?.copy(status = result),
                        successMessage = message
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "No se pudo guardar la revisión")
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
