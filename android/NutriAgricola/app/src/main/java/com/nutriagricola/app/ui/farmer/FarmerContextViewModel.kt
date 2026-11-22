package com.nutriagricola.app.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmerPreferencesRepository
import com.nutriagricola.app.domain.model.farm.Farm
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import retrofit2.HttpException
import java.io.IOException

data class FarmerContextUiState(
    val farms: List<Farm> = emptyList(),
    val selectedFarm: Farm? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isRefreshingCrops: Boolean = false,
    val cropError: String? = null,
    val error: String? = null
)

class FarmerContextViewModel(
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository,
    private val preferencesRepository: FarmerPreferencesRepository,
    private val userId: String,
    private val onSessionExpired: () -> Unit
) : ViewModel() {
    private val _uiState = MutableStateFlow(FarmerContextUiState())
    val uiState = _uiState.asStateFlow()
    val selectedFarm = uiState.map { it.selectedFarm }.distinctUntilChanged()
    private var cropsJob: Job? = null
    private var cropsRequest = 0

    init {
        viewModelScope.launch {
            try {
                val savedFarmId = preferencesRepository.getSelectedFarmId(userId)
                farmRepository.observeByUser(userId).collect { farms ->
                    _uiState.update { state ->
                        state.copy(
                            farms = farms,
                            selectedFarm = farms.find { it.id == state.selectedFarm?.id }
                                ?: farms.find { it.id == savedFarmId }
                                ?: farms.firstOrNull(),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.value = FarmerContextUiState(
                    isLoading = false,
                    error = "No se pudieron cargar los predios"
                )
            }
        }
        viewModelScope.launch {
            selectedFarm.map { it?.id }.distinctUntilChanged().collect { farmId ->
                cropsRequest++
                cropsJob?.cancel()
                _uiState.update { it.copy(isRefreshingCrops = false, cropError = null) }
                if (farmId != null) refreshCrops(farmId)
            }
        }
        refreshFarms()
    }

    fun refreshFarms() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            try {
                farmRepository.refresh(userId)
                val farmId = _uiState.value.selectedFarm?.id
                if (farmId != null && !_uiState.value.isRefreshingCrops) refreshCrops(farmId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: IOException) {
                _uiState.update { it.copy(error = "Sin conexión al servicio. Se muestran los predios guardados; si aún no descargaste ninguno, conéctate y pulsa Actualizar predios.") }
            } catch (exception: HttpException) {
                if (exception.code() == 401 || exception.code() == 403) {
                    onSessionExpired()
                } else {
                    _uiState.update { it.copy(error = "No se pudieron actualizar los predios. Se conservan los datos guardados.") }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "No se pudieron actualizar los predios. Inténtalo nuevamente.") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun refreshCrops(farmId: String) {
        val request = ++cropsRequest
        cropsJob?.cancel()
        _uiState.update { it.copy(isRefreshingCrops = true, cropError = null) }
        cropsJob = viewModelScope.launch {
            try {
                cropRepository.refresh(farmId, userId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: IOException) {
                setCropError(farmId, "No se pudo conectar para actualizar los cultivos. Se conservan los cultivos guardados.")
            } catch (exception: HttpException) {
                if (exception.code() == 401) onSessionExpired()
                else if (exception.code() == 403) {
                    setCropError(farmId, "No tienes acceso a los cultivos de este predio. Actualiza los predios para comprobar tus asignaciones.")
                } else {
                    setCropError(farmId, "No se pudieron actualizar los cultivos. Inténtalo nuevamente.")
                }
            } catch (_: Exception) {
                setCropError(farmId, "No se pudieron actualizar los cultivos. Se conservan los datos guardados.")
            } finally {
                if (request == cropsRequest && _uiState.value.selectedFarm?.id == farmId) {
                    _uiState.update { it.copy(isRefreshingCrops = false) }
                }
            }
        }
    }

    private fun setCropError(farmId: String, message: String) {
        if (_uiState.value.selectedFarm?.id == farmId) {
            _uiState.update { it.copy(cropError = message) }
        }
    }

    fun selectFarm(farmId: String) {
        if (_uiState.value.farms.none { it.id == farmId }) return
        _uiState.update { state ->
            val farm = state.farms.find { it.id == farmId }
            if (farm == null) state else state.copy(selectedFarm = farm)
        }
        viewModelScope.launch {
            try {
                preferencesRepository.saveSelectedFarmId(userId, farmId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "No se pudo recordar el predio seleccionado") }
            }
        }
    }
}
