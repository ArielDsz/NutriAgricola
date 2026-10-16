package com.nutriagricola.app.ui.farmer.record.foliar

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.local.FoliarPhotoStorage
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.domain.model.record.FoliarData
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.domain.model.user.UserRole
import com.nutriagricola.app.ui.farmer.record.RecordFormValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class FoliarRecordUiState(
    val crop: Crop? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val nitrogen: String = "",
    val phosphorus: String = "",
    val potassium: String = "",
    val calcium: String = "",
    val magnesium: String = "",
    val symptoms: String = "",
    val observations: String = "",
    val photos: List<String> = emptyList(),
    val pendingCameraPath: String? = null,
    val isProcessingPhoto: Boolean = false
)

class FoliarRecordViewModel(
    val cropId: String,
    private val cropRepository: CropRepository,
    private val recordRepository: RecordRepository,
    private val getCurrentUser: () -> User?,
    private val photoStorage: FoliarPhotoStorage,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recordId = savedStateHandle.get<String>("recordId")
        ?: UUID.randomUUID().toString().also { savedStateHandle["recordId"] = it }
    private val _uiState = MutableStateFlow(FoliarRecordUiState(
        photos = savedStateHandle.get<ArrayList<String>>("photos")?.toList() ?: emptyList(),
        pendingCameraPath = savedStateHandle["pendingCameraPath"]
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                // A restored form may correspond to an insert that already committed.
                if (recordRepository.getById(recordId) != null) {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                    return@launch
                }
                val crop = cropRepository.getById(cropId)
                _uiState.update {
                    it.copy(
                        crop = crop,
                        isLoading = false,
                        error = if (crop == null) "No se encontró el cultivo seleccionado" else null
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "No se pudo cargar el cultivo")
                }
            }
        }
    }

    fun updateNitrogen(value: String) = updateField("nitrogen") { it.copy(nitrogen = value) }
    fun updatePhosphorus(value: String) = updateField("phosphorus") { it.copy(phosphorus = value) }
    fun updatePotassium(value: String) = updateField("potassium") { it.copy(potassium = value) }
    fun updateCalcium(value: String) = updateField("calcium") { it.copy(calcium = value) }
    fun updateMagnesium(value: String) = updateField("magnesium") { it.copy(magnesium = value) }
    fun updateSymptoms(value: String) = updateField("symptoms") { it.copy(symptoms = value) }
    fun updateObservations(value: String) = updateField("observations") { it.copy(observations = value) }

    private fun updatePhotos(photos: List<String>) {
        savedStateHandle["photos"] = ArrayList(photos)
        _uiState.update { it.copy(photos = photos) }
    }

    private fun updatePendingCamera(path: String?) {
        savedStateHandle["pendingCameraPath"] = path
        _uiState.update { it.copy(pendingCameraPath = path) }
    }

    private fun canChangePhotos(): Boolean {
        val state = _uiState.value
        // A picker result can arrive while Crop is still loading after process recreation.
        return !state.isSaving && !state.isSaved &&
            !state.isProcessingPhoto && state.pendingCameraPath == null
    }

    fun prepareCamera(onReady: (Uri) -> Unit) {
        if (!canChangePhotos()) return
        _uiState.update { it.copy(isProcessingPhoto = true, error = null) }
        viewModelScope.launch {
            try {
                val photo = photoStorage.createCameraPhoto(recordId)
                updatePendingCamera(photo.path)
                _uiState.update { it.copy(isProcessingPhoto = false) }
                onReady(photo.captureUri)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update { it.copy(isProcessingPhoto = false, error = "No se pudo preparar la cámara") }
            }
        }
    }

    fun onCameraResult(success: Boolean, launchFailed: Boolean = false) {
        val path = _uiState.value.pendingCameraPath ?: return
        updatePendingCamera(null)
        _uiState.update { it.copy(isProcessingPhoto = true) }
        viewModelScope.launch {
            try {
                if (success && photoStorage.isValidPhoto(recordId, path)) {
                    updatePhotos(_uiState.value.photos + path)
                    _uiState.update { it.copy(error = null) }
                } else {
                    photoStorage.discardPhoto(recordId, path)
                    if (success || launchFailed) {
                        _uiState.update { it.copy(error = "No se pudo obtener la fotografía de la cámara") }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "No se pudo procesar la fotografía") }
            } finally {
                _uiState.update { it.copy(isProcessingPhoto = false) }
            }
        }
    }

    fun importPhoto(uri: Uri) {
        if (!canChangePhotos()) return
        _uiState.update { it.copy(isProcessingPhoto = true, error = null) }
        viewModelScope.launch {
            try {
                val path = photoStorage.importPhoto(recordId, uri)
                updatePhotos(_uiState.value.photos + path)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "No se pudo copiar la imagen al dispositivo. Inténtalo nuevamente.") }
            } finally {
                _uiState.update { it.copy(isProcessingPhoto = false) }
            }
        }
    }

    fun removePhoto(path: String) {
        if (!canChangePhotos() || path !in _uiState.value.photos) return
        _uiState.update { it.copy(isProcessingPhoto = true, error = null) }
        viewModelScope.launch {
            try {
                photoStorage.discardPhoto(recordId, path)
                updatePhotos(_uiState.value.photos - path)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "No se pudo quitar la fotografía") }
            } finally {
                _uiState.update { it.copy(isProcessingPhoto = false) }
            }
        }
    }

    fun onPickerLaunchFailed() {
        _uiState.update { it.copy(error = "No se pudo abrir el selector de imágenes") }
    }

    override fun onCleared() {
        val state = _uiState.value
        // Rotation does not clear this ViewModel. Never race cleanup against an insert.
        if (!state.isSaved && !state.isSaving) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    photoStorage.discardDraft(recordId)
                } catch (_: Exception) {
                    // Keep files if their ownership cannot be verified against Room.
                }
            }
        }
        super.onCleared()
    }

    private fun updateField(field: String, transform: (FoliarRecordUiState) -> FoliarRecordUiState) {
        _uiState.update {
            if (it.isSaving || it.isSaved) it
            else transform(it).copy(fieldErrors = it.fieldErrors - field, error = null)
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isLoading || state.crop == null || state.isSaving || state.isSaved ||
            state.isProcessingPhoto || state.pendingCameraPath != null) return

        val validation = RecordFormValidator.validateNumbers(
            mapOf(
                "nitrogen" to state.nitrogen,
                "phosphorus" to state.phosphorus,
                "potassium" to state.potassium,
                "calcium" to state.calcium,
                "magnesium" to state.magnesium
            )
        )
        if (validation.errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = validation.errors, error = null) }
            return
        }

        val dataError = RecordFormValidator.validateMinimumData(
            validation.values.values,
            state.symptoms,
            hasPhotos = state.photos.isNotEmpty()
        )
        if (dataError != null) {
            _uiState.update { it.copy(error = dataError, fieldErrors = emptyMap()) }
            return
        }

        val user = getCurrentUser()
        if (user == null || !user.active || user.role != UserRole.FARMER) {
            _uiState.update { it.copy(error = "Inicia sesión como agricultor para guardar el registro") }
            return
        }

        val values = validation.values
        val record = Record(
            id = recordId,
            cropId = cropId,
            userId = user.id,
            type = RecordType.FOLIAR,
            status = RecordStatus.PENDING,
            observations = RecordFormValidator.optionalText(state.observations),
            data = FoliarData(
                nitrogen = values["nitrogen"],
                phosphorus = values["phosphorus"],
                potassium = values["potassium"],
                calcium = values["calcium"],
                magnesium = values["magnesium"],
                symptoms = RecordFormValidator.optionalText(state.symptoms),
                photos = state.photos
            )
        )

        _uiState.update { it.copy(isSaving = true, error = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            try {
                if (state.photos.any { !photoStorage.isValidPhoto(recordId, it) }) {
                    _uiState.update { it.copy(isSaving = false, error = "Una fotografía ya no está disponible. Quítala y vuelve a agregarla.") }
                    return@launch
                }
                recordRepository.insert(record)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "No se pudo guardar el registro. Inténtalo nuevamente.")
                }
            }
        }
    }
}
