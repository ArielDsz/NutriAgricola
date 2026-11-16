package com.nutriagricola.app.ui.farmer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.domain.model.farm.Farm
import com.nutriagricola.app.domain.model.record.Record
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

data class FarmerHomeUiState(
    val farmId: String? = null,
    val crops: List<Crop> = emptyList(),
    val recentRecords: List<Record> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class FarmerHomeViewModel(
    userId: String,
    selectedFarm: Flow<Farm?>,
    cropRepository: CropRepository,
    recordRepository: RecordRepository
) : ViewModel() {
    val uiState = selectedFarm.distinctUntilChangedBy { it?.id }.flatMapLatest { farm ->
        if (farm == null) {
            flowOf(FarmerHomeUiState())
        } else {
            combine(
                cropRepository.observeByFarmId(farm.id),
                recordRepository.observeByUserAndFarm(userId, farm.id)
            ) { crops, records ->
                FarmerHomeUiState(farmId = farm.id, crops = crops, recentRecords = records.take(3))
            }.onStart {
                emit(FarmerHomeUiState(farmId = farm.id, isLoading = true))
            }.catch {
                emit(FarmerHomeUiState(farmId = farm.id, error = "No se pudieron cargar los datos del predio"))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, FarmerHomeUiState(isLoading = true))
}
