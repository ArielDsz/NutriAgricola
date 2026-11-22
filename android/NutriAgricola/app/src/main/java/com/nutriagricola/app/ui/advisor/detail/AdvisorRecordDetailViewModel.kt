package com.nutriagricola.app.ui.advisor.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.data.repository.RecordRemoteRepository
import retrofit2.HttpException
import java.io.IOException
import com.nutriagricola.app.data.repository.RecordReviewRepository
import com.nutriagricola.app.data.repository.UserRepository
import com.nutriagricola.app.domain.model.advisor.RecordReview
import com.nutriagricola.app.domain.model.advisor.ReviewResult
import com.nutriagricola.app.domain.model.user.UserRole
import kotlinx.coroutines.CancellationException
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
    val reviews: List<RecordReview> = emptyList(),
    val commentInput: String = "",
    val isLoading: Boolean = true,
    val remotePhotoCount: Int = 0,
    val isSaving: Boolean = false,
    val canReview: Boolean = false,
    val error: String? = null,
    val loadError: String? = null,
    val successMessage: String? = null
)

class AdvisorRecordDetailViewModel(
    private val recordId: String,
    private val getCurrentUser: () -> User?,
    private val recordRepository: RecordRepository,
    private val recordRemoteRepository: RecordRemoteRepository,
    private val recordReviewRepository: RecordReviewRepository,
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvisorRecordDetailUiState())
    val uiState: StateFlow<AdvisorRecordDetailUiState> = _uiState.asStateFlow()
    private var pendingReview: RecordReview? = null

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            try {
                val record = recordRepository.getById(recordId)
                val currentUser = getCurrentUser()
                val assignedIds = currentUser?.let { farmRepository.getByUser(it.id).map { farm -> farm.id } }.orEmpty()
                val assignedCrop = record?.let { cropRepository.getById(it.cropId) }
                if (currentUser == null || currentUser.role != UserRole.ADVISOR || assignedCrop?.farmId !in assignedIds) {
                    _uiState.update { it.copy(isLoading = false, loadError = "Análisis no disponible para tu cuenta") }
                    return@launch
                }
                if (record == null) {
                    _uiState.update { it.copy(isLoading = false, loadError = "Análisis no encontrado") }
                    return@launch
                }
                val crop = cropRepository.getById(record.cropId)
                val farm = crop?.let { farmRepository.getAll().find { f -> f.id == it.farmId } }
                val farmerUser = userRepository.getById(record.userId)
                val reviews = recordReviewRepository.getReviewsForRecord(recordId)
                val sync = recordRepository.getSyncState(recordId)

                _uiState.update {
                    it.copy(
                        record = record,
                        crop = crop,
                        farm = farm,
                        farmerUser = farmerUser,
                        reviews = reviews,
                        canReview = sync != null && !sync.dirty && record.status !in listOf(RecordStatus.DRAFT, RecordStatus.DELETED),
                        remotePhotoCount = recordRepository.getSyncState(record.id)?.remotePhotoCount ?: 0,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, loadError = "No se pudieron cargar los detalles del análisis")
                }
            }
        }
    }

    fun onCommentChange(comment: String) {
        _uiState.update { it.copy(commentInput = comment) }
    }

    fun submitReview(result: ReviewResult) {
        if (_uiState.value.isSaving || _uiState.value.isLoading || !_uiState.value.canReview || _uiState.value.record == null) return
        val currentUser = getCurrentUser()
        if (currentUser == null || !currentUser.active || currentUser.role != UserRole.ADVISOR) {
            _uiState.update { it.copy(error = "Sesión de asesor no válida") }
            return
        }

        val comment = _uiState.value.commentInput.trim()
        if (result == ReviewResult.OBSERVED && comment.isEmpty()) {
            _uiState.update { it.copy(error = "Para observar un análisis debes ingresar una observación") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val finalComment = if (comment.isEmpty()) "Análisis validado formalmente por el asesor." else comment
                // Keep the UUID and date on retry if the first response was lost.
                val review = pendingReview?.takeIf { it.result == result && it.comment == finalComment } ?: RecordReview(
                    id = UUID.randomUUID().toString(),
                    recordId = recordId,
                    advisorId = currentUser.id,
                    advisorName = "${currentUser.firstName} ${currentUser.lastName}",
                    date = LocalDateTime.now(),
                    result = result,
                    comment = finalComment
                )
                pendingReview = review

                val confirmed = recordRemoteRepository.submitReview(review)
                val newStatus = RecordStatus.valueOf(confirmed.recordStatus)
                val savedReview = review.copy(advisorName = confirmed.advisorName,
                    date = LocalDateTime.parse(confirmed.date), result = ReviewResult.valueOf(confirmed.result),
                    comment = confirmed.comment)
                pendingReview = null

                val message = "Revisión confirmada y guardada en el servidor"

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        reviews = (listOf(savedReview) + it.reviews).distinctBy { item -> item.id },
                        commentInput = "",
                        record = it.record?.copy(status = newStatus),
                        successMessage = message
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                _uiState.update { it.copy(isSaving = false,
                    error = "No se pudo confirmar el envío. Conservamos tu comentario; vuelve a intentarlo con conexión.") }
            } catch (e: HttpException) {
                _uiState.update { it.copy(isSaving = false, error = when (e.code()) {
                    401 -> "La sesión venció. Vuelve a iniciar sesión para enviar la revisión."
                    403 -> "No tienes permiso para revisar este análisis."
                    409 -> "El análisis cambió. Actualiza los registros antes de reintentar."
                    else -> "El servidor no pudo confirmar la revisión. Inténtalo nuevamente."
                }) }
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
