package com.nutriagricola.app.ui.advisor.home

import kotlinx.coroutines.CancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.data.repository.UserRepository
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.ui.advisor.model.AdvisorRecordItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdvisorHomeUiState(
    val pendingCount: Int = 0,
    val totalRecordsCount: Int = 0,
    val totalFarmsCount: Int = 0,
    val recentPendingRecords: List<AdvisorRecordItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class AdvisorHomeViewModel(
    private val recordRepository: RecordRepository,
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvisorHomeUiState())
    val uiState: StateFlow<AdvisorHomeUiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val allRecords = recordRepository.getAll().filter {
                    it.status != RecordStatus.DELETED && it.status != RecordStatus.DRAFT
                }
                val pendingRecords = allRecords.filter { it.status == RecordStatus.PENDING }
                val farms = farmRepository.getAll()
                val crops = farms.flatMap { cropRepository.getByFarmId(it.id) }.associateBy { it.id }
                val users = userRepository.getAll().associateBy { it.id }
                val farmsMap = farms.associateBy { it.id }

                val recentItems = pendingRecords.sortedByDescending { it.date }.take(3).map { record ->
                    val crop = crops[record.cropId]
                    val farmName = crop?.let { farmsMap[it.farmId]?.name } ?: "Predio"
                    val cropName = crop?.name ?: "Cultivo"
                    val farmerName = users[record.userId]?.let { "${it.firstName} ${it.lastName}" } ?: "Agricultor"
                    AdvisorRecordItem(
                        record = record,
                        farmName = farmName,
                        cropName = cropName,
                        farmerName = farmerName,
                        farmId = crop?.farmId
                    )
                }

                _uiState.update {
                    it.copy(
                        pendingCount = pendingRecords.size,
                        totalRecordsCount = allRecords.size,
                        totalFarmsCount = farms.size,
                        recentPendingRecords = recentItems,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar la información del asesor"
                    )
                }
            }
        }
    }
}
