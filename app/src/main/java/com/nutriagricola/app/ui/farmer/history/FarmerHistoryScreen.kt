package com.nutriagricola.app.ui.farmer.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.ui.farmer.FarmerContextUiState
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomBar
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomItem
import com.nutriagricola.app.ui.farmer.record.components.RecordHistoryCard
import com.nutriagricola.app.ui.farmer.record.components.historyLabel

@Composable
fun FarmerHistoryScreen(
    viewModel: FarmerHistoryViewModel,
    contextState: FarmerContextUiState,
    onHomeClick: () -> Unit,
    onRecordClick: (String) -> Unit
) {
    val loadedState by viewModel.uiState.collectAsState()
    val farm = contextState.selectedFarm
    // Never render cached rows from a previously selected farm.
    val state = if (loadedState.farmId == farm?.id) loadedState
    else FarmerHistoryUiState(farmId = farm?.id, isLoading = true)
    var cropMenuExpanded by remember(farm?.id) { mutableStateOf(false) }
    val error = contextState.error ?: state.error

    Scaffold(
        bottomBar = {
            FarmerBottomBar(
                selectedItem = FarmerBottomItem.HISTORY,
                onHomeClick = onHomeClick,
                onHistoryClick = {},
                onProgramClick = {},
                onFarmsClick = {}
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text("Historial", style = MaterialTheme.typography.titleLarge)
                    farm?.let { Text(it.name, style = MaterialTheme.typography.bodyMedium) }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedType == null,
                        onClick = { viewModel.selectType(null) },
                        label = { Text("Todos") },
                        enabled = farm != null
                    )
                    RecordType.entries.forEach { type ->
                        FilterChip(
                            selected = state.selectedType == type,
                            onClick = { viewModel.selectType(type) },
                            label = { Text(type.historyLabel()) },
                            enabled = farm != null
                        )
                    }
                }
            }
            item {
                Column {
                    Text("Cultivo", style = MaterialTheme.typography.labelMedium)
                    TextButton(
                        onClick = { cropMenuExpanded = true },
                        enabled = farm != null && !state.isLoading
                    ) {
                        val crop = state.crops.find { it.id == state.selectedCropId }
                        Text(
                            text = if (crop == null) "Todos los cultivos"
                            else if (crop.variety.isNullOrBlank()) crop.name
                            else "${crop.name} - ${crop.variety}"
                        )
                    }
                    DropdownMenu(
                        expanded = cropMenuExpanded,
                        onDismissRequest = { cropMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los cultivos") },
                            onClick = {
                                viewModel.selectCrop(null)
                                cropMenuExpanded = false
                            }
                        )
                        state.crops.forEach { crop ->
                            DropdownMenuItem(
                                text = {
                                    Text(if (crop.variety.isNullOrBlank()) crop.name else "${crop.name} - ${crop.variety}")
                                },
                                onClick = {
                                    viewModel.selectCrop(crop.id)
                                    cropMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            when {
                error != null -> item { Text(error, color = MaterialTheme.colorScheme.error) }
                contextState.isLoading || (farm != null && state.isLoading) ->
                    item { CircularProgressIndicator() }
                farm == null -> item { Text("No hay un predio seleccionado") }
                state.records.isEmpty() -> item { Text("No hay registros para esta selección") }
                else -> items(state.records, key = { it.id }) { record ->
                    RecordHistoryCard(
                        record = record,
                        cropName = state.crops.find { it.id == record.cropId }?.name ?: "Cultivo",
                        onClick = { onRecordClick(record.id) }
                    )
                }
            }
        }
    }
}
