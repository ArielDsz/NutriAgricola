package com.nutriagricola.app.ui.farmer.crop.detail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.ui.farmer.FarmerContextUiState
import com.nutriagricola.app.ui.farmer.record.components.RecordHistoryCard
import com.nutriagricola.app.ui.farmer.record.components.historyLabel
import com.nutriagricola.app.ui.theme.OfflineBackground
import java.time.format.DateTimeFormatter

@Composable
fun CropDetailScreen(
    viewModel: CropDetailViewModel,
    contextState: FarmerContextUiState,
    onBackClick: () -> Unit,
    onRecordClick: (String) -> Unit
) {
    val loadedState by viewModel.uiState.collectAsState()
    // Hide cached data immediately when it no longer belongs to the current farm.
    val state = if (loadedState.farmId == contextState.selectedFarm?.id) loadedState
    else CropDetailUiState()
    val error = contextState.error ?: state.error
    val crop = state.crop

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (contextState.isLoading || error != null || state.isLoading || crop == null) {
                item {
                    Column(modifier = Modifier.statusBarsPadding().padding(16.dp)) {
                        CropDetailBackButton(onBackClick)
                        Text("Detalle del cultivo", style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
            when {
                contextState.isLoading -> item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> item {
                    Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
                }
                state.isLoading -> item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                crop != null -> {
                    item { CropDetailHeader(crop, onBackClick) }
                    item {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Últimos análisis", style = MaterialTheme.typography.bodyLarge)
                            Text("El registro más reciente de cada tipo", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    item {
                        LatestCropRecords(state.latestRecords, onRecordClick)
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(painterResource(R.drawable.ic_history), null,
                                modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Historial del cultivo", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    item {
                        val chipColors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.selectedType == null,
                                onClick = { viewModel.selectType(null) },
                                label = { Text("Todos", style = MaterialTheme.typography.bodySmall) },
                                colors = chipColors,
                                shape = RoundedCornerShape(12.dp)
                            )
                            RecordType.entries.forEach { type ->
                                FilterChip(
                                    selected = state.selectedType == type,
                                    onClick = { viewModel.selectType(type) },
                                    label = { Text(type.historyLabel(), style = MaterialTheme.typography.bodySmall) },
                                    colors = chipColors,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                    if (state.records.isEmpty()) {
                        item { CropDetailEmptyMessage("No hay registros para esta selección") }
                    } else {
                        items(state.records, key = { "history_${it.id}" }) { record ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                RecordHistoryCard(
                                    record = record,
                                    cropName = crop.name,
                                    onClick = { onRecordClick(record.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CropDetailHeader(crop: Crop, onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painterResource(R.drawable.header_background), contentDescription = null,
            contentScale = ContentScale.Crop, alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxWidth().height(160.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        )
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CropDetailBackButton(onBackClick)
            Text("Detalle del cultivo", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f))
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 120.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val fallback = painterResource(R.drawable.default_crop)
                AsyncImage(
                    model = crop.imagePath?.takeIf { it.isNotBlank() },
                    contentDescription = "Imagen de ${crop.name}",
                    placeholder = fallback, fallback = fallback, error = fallback,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(144.dp).clip(RoundedCornerShape(12.dp))
                )
                Text(crop.name, style = MaterialTheme.typography.titleLarge)
                if (LocalDensity.current.fontScale > 1.2f) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        crop.variety?.takeIf { it.isNotBlank() }?.let {
                            CropDetailAttribute("Variedad", it, R.drawable.ic_nitrogen, Modifier.fillMaxWidth())
                        }
                        crop.areaHectares?.let {
                            CropDetailAttribute("Superficie", "$it ha", R.drawable.ic_farms, Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        crop.variety?.takeIf { it.isNotBlank() }?.let {
                            CropDetailAttribute("Variedad", it, R.drawable.ic_nitrogen, Modifier.weight(1f))
                        }
                        crop.areaHectares?.let {
                            CropDetailAttribute("Superficie", "$it ha", R.drawable.ic_farms, Modifier.weight(1f))
                        }
                    }
                }
                crop.phenologicalStage?.takeIf { it.isNotBlank() }?.let {
                    CropDetailAttribute("Etapa fenológica", it, R.drawable.ic_potassium, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun CropDetailAttribute(label: String, value: String, @DrawableRes iconRes: Int, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer) {
        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(iconRes), null, modifier = Modifier.size(32.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(value, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun LatestCropRecords(records: List<Record>, onRecordClick: (String) -> Unit) {
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth - 32.dp >= 336.dp && fontScale <= 1.2f) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecordType.entries.forEach { type ->
                    LatestCropRecordCard(type, records.firstOrNull { it.type == type }, onRecordClick,
                        Modifier.weight(1f))
                }
            }
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(RecordType.entries, key = { it.name }) { type ->
                    LatestCropRecordCard(type, records.firstOrNull { it.type == type }, onRecordClick,
                        Modifier.width(168.dp))
                }
            }
        }
    }
}

@Composable
private fun LatestCropRecordCard(
    type: RecordType,
    record: Record?,
    onRecordClick: (String) -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier.then(if (record != null) Modifier.clickable { onRecordClick(record.id) } else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp).padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(when (type) {
                RecordType.SOIL -> R.drawable.analysis_soil
                RecordType.WATER -> R.drawable.analysis_water
                RecordType.FOLIAR -> R.drawable.analysis_foliar
            }), null, modifier = Modifier.size(40.dp))
            Text(type.historyLabel(), style = MaterialTheme.typography.bodyLarge)
            if (record != null) {
                Text(record.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (record.status == RecordStatus.PENDING) OfflineBackground
                        else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(record.status.historyLabel(), style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            } else {
                Text("Sin registros", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CropDetailBackButton(onBackClick: () -> Unit) {
    Surface(
        onClick = onBackClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.primary,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.ic_chevron_right), "Volver", modifier = Modifier.size(24.dp).rotate(180f))

        }
    }
}

@Composable
private fun CropDetailEmptyMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
    }
}
