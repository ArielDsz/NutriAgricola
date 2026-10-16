package com.nutriagricola.app.ui.farmer.history

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.farm.Farm
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.ui.farmer.FarmerContextUiState
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomBar
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomItem
import com.nutriagricola.app.ui.farmer.record.components.CropSelectorBottomSheet
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
    var showCropSelector by remember(farm?.id) { mutableStateOf(false) }
    val error = contextState.error ?: state.error

    if (showCropSelector) {
        CropSelectorBottomSheet(
            crops = state.crops,
            selectedCropId = state.selectedCropId,
            onDismiss = { showCropSelector = false },
            onCropSelected = { crop ->
                viewModel.selectCrop(crop.id)
                showCropSelector = false
            },
            onAllCropsSelected = {
                viewModel.selectCrop(null)
                showCropSelector = false
            }
        )
    }

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
            modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { HistoryHeader(farm) }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val chipColors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.selectedType == null,
                            onClick = { viewModel.selectType(null) },
                            label = { Text("Todos", style = MaterialTheme.typography.bodyMedium) },
                            enabled = farm != null, colors = chipColors, shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                        RecordType.entries.forEach { type ->
                            FilterChip(
                                selected = state.selectedType == type,
                                onClick = { viewModel.selectType(type) },
                                label = { Text(type.historyLabel(), style = MaterialTheme.typography.bodyMedium) },
                                enabled = farm != null, colors = chipColors, shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }
                }
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Cultivo", style = MaterialTheme.typography.bodyLarge)
                    Card(
                        onClick = { showCropSelector = true },
                        enabled = farm != null && !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        val crop = state.crops.find { it.id == state.selectedCropId }
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            if (crop == null) {
                                Image(painterResource(R.drawable.analysis_foliar), null, modifier = Modifier.size(40.dp))
                            } else {
                                val fallback = painterResource(R.drawable.default_crop)
                                AsyncImage(
                                    model = crop.imagePath?.takeIf { it.isNotBlank() },
                                    contentDescription = "Imagen de ${crop.name}",
                                    placeholder = fallback, fallback = fallback, error = fallback,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Text(
                                text = if (crop == null) "Todos los cultivos"
                                    else if (crop.variety.isNullOrBlank()) crop.name
                                    else "${crop.name} - ${crop.variety}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(painterResource(R.drawable.ic_expand_more), "Seleccionar cultivo",
                                modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                }
            }

            when {
                error != null -> item { HistoryMessage(error, isError = true) }
                contextState.isLoading || (farm != null && state.isLoading) ->
                    item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                farm == null -> item { HistoryMessage("No hay un predio seleccionado") }
                state.records.isEmpty() -> item { HistoryMessage("No hay registros para esta selección") }
                else -> items(state.records, key = { it.id }) { record ->
                    val crop = state.crops.find { it.id == record.cropId }
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        RecordHistoryCard(
                            record = record,
                            cropName = crop?.name ?: "Cultivo",
                            onClick = { onRecordClick(record.id) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                            cropImagePath = crop?.imagePath,
                            showCropThumbnail = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(farm: Farm?) {
    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp)
        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))) {
        Image(painterResource(R.drawable.header_background), null,
            contentScale = ContentScale.Crop, alignment = Alignment.TopCenter,
            modifier = Modifier.matchParentSize())
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Historial", style = MaterialTheme.typography.headlineLarge)
            farm?.let {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)) {
                    Text(it.name, style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryMessage(message: String, isError: Boolean = false) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(14.dp),
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant) {
        Text(message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
