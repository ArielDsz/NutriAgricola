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

class NewRecordViewModel(
    private val cropRepository: CropRepository
) : ViewModel() {

    private val _crops = MutableStateFlow<List<Crop>>(emptyList())
    val crops: StateFlow<List<Crop>> = _crops.asStateFlow()

    private val _selectedCrop = MutableStateFlow<Crop?>(null)
    val selectedCrop: StateFlow<Crop?> = _selectedCrop.asStateFlow()

    private val _selectedType = MutableStateFlow<RecordType?>(null)
    val selectedType: StateFlow<RecordType?> = _selectedType.asStateFlow()

    fun loadCrops(farmId: String) {
        viewModelScope.launch {
            _crops.value = cropRepository.getByFarmId(farmId)
        }
    }

    fun selectCrop(crop: Crop) {
        _selectedCrop.value = crop
    }

    fun selectType(type: RecordType) {
        _selectedType.value = type
    }
}