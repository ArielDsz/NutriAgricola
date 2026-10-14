package com.nutriagricola.app.ui.farmer.crop.detail

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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

data class CropDetailUiState(
    val farmId: String? = null,
    val crop: Crop? = null,
    val latestRecords: List<Record> = emptyList(),
    val records: List<Record> = emptyList(),
    val selectedType: RecordType? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class CropDetailViewModel(
    cropId: String,
    userId: String,
    selectedFarm: Flow<Farm?>,
    cropRepository: CropRepository,
    recordRepository: RecordRepository
) : ViewModel() {
    private val selectedType = MutableStateFlow<RecordType?>(null)

    private val data = selectedFarm.distinctUntilChangedBy { it?.id }.flatMapLatest { farm ->
        if (farm == null) {
            flowOf(CropDetailUiState(
                isLoading = false,
                error = "No hay un predio seleccionado"
            ))
        } else {
            combine(
                cropRepository.observeByFarmId(farm.id),
                recordRepository.observeByUserAndFarm(userId, farm.id)
            ) { crops, records ->
                val crop = crops.find { it.id == cropId && it.farmId == farm.id }
                if (crop == null) {
                    CropDetailUiState(
                        farmId = farm.id, isLoading = false,
                        error = "Cultivo no disponible en el predio seleccionado"
                    )
                } else {
                    // The repository preserves descending date and insertion order.
                    val cropRecords = records.filter { it.cropId == crop.id && it.userId == userId }
                    CropDetailUiState(
                        farmId = farm.id,
                        crop = crop,
                        latestRecords = RecordType.entries.mapNotNull { type ->
                            cropRecords.firstOrNull { it.type == type }
                        },
                        records = cropRecords,
                        isLoading = false
                    )
                }
            }.onStart {
                emit(CropDetailUiState(farmId = farm.id))
            }.catch {
                emit(CropDetailUiState(
                    farmId = farm.id, isLoading = false,
                    error = "No se pudieron cargar el cultivo y sus registros"
                ))
            }
        }
    }

    val uiState = combine(data, selectedType) { state, type ->
        state.copy(
            selectedType = type,
            records = state.records.filter { type == null || it.type == type }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CropDetailUiState())

    fun selectType(type: RecordType?) {
        selectedType.value = type
    }
}
