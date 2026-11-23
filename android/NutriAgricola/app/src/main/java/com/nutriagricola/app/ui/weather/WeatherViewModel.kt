package com.nutriagricola.app.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.WeatherRepository
import com.nutriagricola.app.ui.farmer.home.components.WeatherUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()
    private val mutableState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val state = mutableState.asStateFlow()
    private var request: Job? = null

    fun load() {
        request?.cancel()
        request = viewModelScope.launch {
            mutableState.value = WeatherUiState.Loading
            mutableState.value = repository.getWeather().fold(
                onSuccess = { WeatherUiState.Success(it) },
                onFailure = { WeatherUiState.Error(it.message ?: "Clima no disponible") }
            )
        }
    }
}
