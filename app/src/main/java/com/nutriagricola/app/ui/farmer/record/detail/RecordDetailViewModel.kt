package com.nutriagricola.app.ui.farmer.record.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.domain.model.farm.Farm
import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.domain.model.user.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class RecordDetailUiState(
    val farmId: String? = null,
    val record: Record? = null,
    val crop: Crop? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class RecordDetailViewModel(
    recordId: String,
    recordRepository: RecordRepository,
    cropRepository: CropRepository,
    getCurrentUser: () -> User?,
    selectedFarm: Flow<Farm?>
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            selectedFarm.collectLatest { farm ->
                // Clear previously loaded data before validating the new context.
                _uiState.value = RecordDetailUiState(farmId = farm?.id)
                try {
                    val user = getCurrentUser()
                    if (user == null || !user.active || user.role != UserRole.FARMER || farm == null) {
                        _uiState.value = RecordDetailUiState(
                            farmId = farm?.id, isLoading = false,
                            error = "Registro no disponible para el usuario y predio actuales."
                        )
                        return@collectLatest
                    }
                    val record = recordRepository.getById(recordId)
                    val crop = if (record?.userId == user.id) {
                        cropRepository.getById(record.cropId)
                    } else null

                    _uiState.value = if (record != null && crop != null && crop.farmId == farm.id) {
                        RecordDetailUiState(
                            farmId = farm.id, record = record, crop = crop, isLoading = false
                        )
                    } else {
                        RecordDetailUiState(
                            farmId = farm.id, isLoading = false,
                            error = "Registro no disponible para el usuario y predio actuales."
                        )
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    _uiState.value = RecordDetailUiState(
                        farmId = farm?.id, isLoading = false,
                        error = "No se pudo cargar el registro. Vuelve a intentarlo."
                    )
                }
            }
        }
    }
}
