package com.nutriagricola.app.ui.farmer.record.soil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.domain.model.farm.Crop
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SoilRecordUiState(
    val crop: Crop? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val ph: String = "",
    val electricalConductivity: String = "",
    val nitrogen: String = "",
    val phosphorus: String = "",
    val potassium: String = "",
    val calcium: String = "",
    val magnesium: String = "",
    val organicMatter: String = "",
    val observations: String = ""
)

class SoilRecordViewModel(
    val cropId: String,
    private val cropRepository: CropRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoilRecordUiState())
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

    fun updatePh(value: String) {
        _uiState.update { it.copy(ph = value) }
    }

    fun updateElectricalConductivity(value: String) {
        _uiState.update { it.copy(electricalConductivity = value) }
    }

    fun updateNitrogen(value: String) {
        _uiState.update { it.copy(nitrogen = value) }
    }

    fun updatePhosphorus(value: String) {
        _uiState.update { it.copy(phosphorus = value) }
    }

    fun updatePotassium(value: String) {
        _uiState.update { it.copy(potassium = value) }
    }

    fun updateCalcium(value: String) {
        _uiState.update { it.copy(calcium = value) }
    }

    fun updateMagnesium(value: String) {
        _uiState.update { it.copy(magnesium = value) }
    }

    fun updateOrganicMatter(value: String) {
        _uiState.update { it.copy(organicMatter = value) }
    }

    fun updateObservations(value: String) {
        _uiState.update { it.copy(observations = value) }
    }
}
