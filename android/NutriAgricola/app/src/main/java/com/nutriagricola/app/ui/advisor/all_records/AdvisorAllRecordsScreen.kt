package com.nutriagricola.app.ui.advisor.all_records

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.advisor.ReviewResult
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.ui.advisor.components.*
import com.nutriagricola.app.ui.components.HeaderActions
import com.nutriagricola.app.ui.components.SectionHeader
import com.nutriagricola.app.ui.farmer.record.components.RecordHistoryCard
import com.nutriagricola.app.ui.records.RecordDownloadStatus
import com.nutriagricola.app.ui.records.RecordDownloadUiState

@Composable
fun AdvisorAllRecordsScreen(
    onAccountClick: () -> Unit,
    viewModel: AdvisorAllRecordsViewModel, downloadState: RecordDownloadUiState,
    onRefreshRecords: () -> Unit, onMoreRecords: () -> Unit, onUploadRecords: () -> Unit,
    onBackClick: () -> Unit, onHomeClick: () -> Unit,
    onProgramClick: () -> Unit, onRecordClick: (String) -> Unit,
    headerInitials: String = "", onLogoutClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showFarms by remember { mutableStateOf(false) }
    LaunchedEffect(downloadState.completedRefresh) {
        if (downloadState.completedRefresh > 0) viewModel.loadData()
    }
    AdvisorRefreshOnResume { viewModel.loadData() }
    Scaffold(bottomBar = {
        AdvisorBottomBar(AdvisorBottomItem.ALL_RECORDS, onHomeClick, {}, onProgramClick, onAccountClick)
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { SectionHeader("Registros") { HeaderActions(headerInitials, onLogoutClick) } }
            item { RecordDownloadStatus(downloadState, onRefreshRecords, onMoreRecords, onUploadRecords) }
            item {
                OutlinedTextField(state.searchQuery, viewModel::setSearchQuery, placeholder = { Text("Buscar") },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_search), null, Modifier.size(22.dp)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), singleLine = true,
                    shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant))
            }
            item {
                val colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(state.selectedStatusFilter == null && !state.observedOnly, { viewModel.setStatusFilter(null) },
                        label = { Text("Todos", style = MaterialTheme.typography.bodyMedium) },
                        colors = colors, shape = RoundedCornerShape(20.dp), modifier = Modifier.heightIn(min = 48.dp))
                    FilterChip(state.selectedStatusFilter == RecordStatus.PENDING, { viewModel.setStatusFilter(RecordStatus.PENDING) },
                        label = { Text("Pendientes", style = MaterialTheme.typography.bodyMedium) },
                        colors = colors, shape = RoundedCornerShape(20.dp), modifier = Modifier.heightIn(min = 48.dp))
                    FilterChip(state.observedOnly, viewModel::setObservedFilter,
                        label = { Text("Observados", style = MaterialTheme.typography.bodyMedium) },
                        colors = colors, shape = RoundedCornerShape(20.dp), modifier = Modifier.heightIn(min = 48.dp))
                    FilterChip(state.selectedStatusFilter == RecordStatus.VALIDATED, { viewModel.setStatusFilter(RecordStatus.VALIDATED) },
                        label = { Text("Validados", style = MaterialTheme.typography.bodyMedium) },
                        colors = colors, shape = RoundedCornerShape(20.dp), modifier = Modifier.heightIn(min = 48.dp))
                }
            }
            if (state.farms.isNotEmpty()) item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Predio", style = MaterialTheme.typography.bodyLarge)
                    Box {
                        Card(onClick = { showFarms = true }, shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(R.drawable.ic_farms), null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(state.farms.find { it.id == state.selectedFarmId }?.name ?: "Todos los predios",
                                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                Icon(painterResource(R.drawable.ic_expand_more), "Seleccionar predio", Modifier.size(24.dp))
                            }
                        }
                        DropdownMenu(showFarms, { showFarms = false }) {
                            DropdownMenuItem(text = { Text("Todos los predios") }, onClick = { viewModel.setFarmFilter(null); showFarms = false })
                            state.farms.forEach { farm -> DropdownMenuItem(text = { Text(farm.name) },
                                onClick = { viewModel.setFarmFilter(farm.id); showFarms = false }) }
                        }
                    }
                }
            }
            if (state.isLoading) item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            state.error?.let { error -> item { Text(error, Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.error) } }
            if (!state.isLoading && state.filteredRecords.isEmpty()) item {
                Text("No hay registros", Modifier.padding(24.dp), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(state.filteredRecords, key = { it.record.id }) { item ->
                Box(Modifier.padding(horizontal = 16.dp)) {
                    RecordHistoryCard(item.record, item.cropName, { onRecordClick(item.record.id) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                        cropImagePath = item.cropImagePath, showCropThumbnail = true,
                        extraLabel = item.farmerName,
                        statusLabel = if (item.record.status == RecordStatus.PENDING && item.reviewResult == ReviewResult.OBSERVED) "Observado" else null)
                }
            }
        }
    }
}
