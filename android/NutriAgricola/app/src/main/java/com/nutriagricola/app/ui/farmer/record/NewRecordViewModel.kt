package com.nutriagricola.app.ui.farmer.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.domain.model.record.RecordType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class NewRecordViewModel(
    private val cropRepository: CropRepository
) : ViewModel() {
    private var cropsJob: Job? = null

    private val _crops = MutableStateFlow<List<Crop>>(emptyList())
    val crops: StateFlow<List<Crop>> = _crops.asStateFlow()

    private val _selectedCrop = MutableStateFlow<Crop?>(null)
    val selectedCrop: StateFlow<Crop?> = _selectedCrop.asStateFlow()

    private val _selectedType = MutableStateFlow<RecordType?>(null)
    val selectedType: StateFlow<RecordType?> = _selectedType.asStateFlow()

    fun loadCrops(farmId: String) {
        cropsJob?.cancel()
        _crops.value = emptyList()
        _selectedCrop.value = null
        cropsJob = viewModelScope.launch {
            cropRepository.observeByFarmId(farmId).collect { crops ->
                _crops.value = crops.filter { it.active }
                _selectedCrop.value = _crops.value.find { it.id == _selectedCrop.value?.id }
            }
        }
    }

    fun selectCrop(crop: Crop) {
        _selectedCrop.value = crop
    }

    fun selectType(type: RecordType) {
        _selectedType.value = type
    }
}
