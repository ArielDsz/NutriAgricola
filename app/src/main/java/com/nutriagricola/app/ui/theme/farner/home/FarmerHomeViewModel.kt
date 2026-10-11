package com.nutriagricola.app.ui.theme.farner.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.domain.model.farm.Farm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FarmerHomeViewModel(
    private val farmRepository: FarmRepository
) : ViewModel() {

    private val _farms = MutableStateFlow<List<Farm>>(emptyList())

    val farms: StateFlow<List<Farm>> = _farms.asStateFlow()

    fun loadFarms() {
        viewModelScope.launch {
            _farms.value = farmRepository.getAll()
        }
    }
}