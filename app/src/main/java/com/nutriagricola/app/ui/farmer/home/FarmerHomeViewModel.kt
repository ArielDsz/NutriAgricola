package com.nutriagricola.app.ui.farmer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.domain.model.farm.Farm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FarmerHomeViewModel(
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository
) : ViewModel() {

    private val _farms = MutableStateFlow<List<Farm>>(emptyList())
    val farms: StateFlow<List<Farm>> = _farms.asStateFlow()

    private val _crops = MutableStateFlow<List<Crop>>(emptyList())
    val crops: StateFlow<List<Crop>> = _crops.asStateFlow()

    fun loadFarms() {
        viewModelScope.launch {
            _farms.value = farmRepository.getAll()
        }
    }

    fun loadCrops(farmId: String) {
        viewModelScope.launch {
            _crops.value = cropRepository.getByFarmId(farmId)
        }
    }
}