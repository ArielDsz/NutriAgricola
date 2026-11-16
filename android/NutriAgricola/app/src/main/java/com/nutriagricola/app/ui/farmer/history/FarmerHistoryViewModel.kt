package com.nutriagricola.app.ui.farmer.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.domain.model.farm.Farm
import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.record.RecordType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

data class FarmerHistoryUiState(
    val farmId: String? = null,
    val crops: List<Crop> = emptyList(),
    val records: List<Record> = emptyList(),
    val selectedType: RecordType? = null,
    val selectedCropId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class FarmerHistoryViewModel(
    userId: String,
    selectedFarm: Flow<Farm?>,
    cropRepository: CropRepository,
    recordRepository: RecordRepository
) : ViewModel() {
    private val selectedType = MutableStateFlow<RecordType?>(null)
    private val selectedCropId = MutableStateFlow<String?>(null)

    private val data = selectedFarm.distinctUntilChangedBy { it?.id }.flatMapLatest { farm ->
        selectedCropId.value = null
        if (farm == null) {
            flowOf(FarmerHistoryUiState())
        } else {
            combine(
                cropRepository.observeByFarmId(farm.id),
                recordRepository.observeByUserAndFarm(userId, farm.id)
            ) { crops, records ->
                FarmerHistoryUiState(farmId = farm.id, crops = crops, records = records)
            }.onStart {
                emit(FarmerHistoryUiState(farmId = farm.id, isLoading = true))
            }.catch {
                emit(FarmerHistoryUiState(farmId = farm.id, error = "No se pudo cargar el historial"))
            }
        }
    }.onEach { state ->
        if (!state.isLoading && state.crops.none { it.id == selectedCropId.value }) {
            selectedCropId.value = null
        }
    }

    val uiState = combine(data, selectedType, selectedCropId) { state, type, cropId ->
        state.copy(
            selectedType = type,
            selectedCropId = cropId,
            records = state.records.filter { record ->
                (type == null || record.type == type) && (cropId == null || record.cropId == cropId)
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, FarmerHistoryUiState(isLoading = true))

    fun selectType(type: RecordType?) {
        selectedType.value = type
    }

    fun selectCrop(cropId: String?) {
        if (cropId == null || uiState.value.crops.any { it.id == cropId }) {
            selectedCropId.value = cropId
        }
    }
}
