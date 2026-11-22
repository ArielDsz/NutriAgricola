package com.nutriagricola.app.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.FarmerPreferencesRepository
import com.nutriagricola.app.domain.model.farm.Farm
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class FarmerContextUiState(
    val farms: List<Farm> = emptyList(),
    val selectedFarm: Farm? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class FarmerContextViewModel(
    private val farmRepository: FarmRepository,
    private val preferencesRepository: FarmerPreferencesRepository,
    private val userId: String,
    private val onSessionExpired: () -> Unit
) : ViewModel() {
    private val _uiState = MutableStateFlow(FarmerContextUiState())
    val uiState = _uiState.asStateFlow()
    val selectedFarm = uiState.map { it.selectedFarm }.distinctUntilChanged()

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
        refreshFarms()
    }

    fun refreshFarms() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            try {
                farmRepository.refresh(userId)
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
