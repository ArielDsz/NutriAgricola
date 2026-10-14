package com.nutriagricola.app.ui.farmer.record.foliar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val observations: String = ""
)

class FoliarRecordViewModel(
    val cropId: String,
    private val cropRepository: CropRepository,
    private val recordRepository: RecordRepository,
    private val getCurrentUser: () -> User?
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoliarRecordUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
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

    private fun updateField(field: String, transform: (FoliarRecordUiState) -> FoliarRecordUiState) {
        _uiState.update {
            if (it.isSaving || it.isSaved) it
            else transform(it).copy(fieldErrors = it.fieldErrors - field, error = null)
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isLoading || state.crop == null || state.isSaving || state.isSaved) return

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
            state.symptoms
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
                photos = emptyList()
            )
        )

        _uiState.update { it.copy(isSaving = true, error = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            try {
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
