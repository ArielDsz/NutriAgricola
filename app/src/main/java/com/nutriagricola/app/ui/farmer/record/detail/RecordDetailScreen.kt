package com.nutriagricola.app.ui.farmer.record.detail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.nutriagricola.app.domain.model.record.*
import com.nutriagricola.app.ui.farmer.FarmerContextUiState
import com.nutriagricola.app.ui.farmer.record.components.historyLabel
import com.nutriagricola.app.ui.farmer.record.components.FoliarPhotoGallery
import com.nutriagricola.app.ui.theme.OfflineBackground
import java.time.format.DateTimeFormatter

@Composable
fun RecordDetailScreen(
    viewModel: RecordDetailViewModel,
    contextState: FarmerContextUiState,
    onBackClick: () -> Unit
) {
    val loadedState by viewModel.uiState.collectAsState()
    // Do not display cached data while a different farm is being validated.
    val state = if (loadedState.farmId == contextState.selectedFarm?.id) loadedState
    else RecordDetailUiState()
    val error = contextState.error ?: state.error
    val ready = error == null && !contextState.isLoading && !state.isLoading
    val record = state.record.takeIf { ready }
    val crop = state.crop.takeIf { ready }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            RecordDetailHeader(record, crop, onBackClick)
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when {
                    error != null -> Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Text(error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    contextState.isLoading || state.isLoading -> Box(Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    else -> if (record != null && crop != null) {
                        Text("Datos del análisis", style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp))
                        RecordMeasurements(record.data)
                        (record.data as? WaterData)?.waterSource?.takeIf { it.isNotBlank() }?.let {
                            RecordTextBlock("Fuente de agua", it, R.drawable.ic_water_source)
                        }
                        val symptoms = (record.data as? FoliarData)?.symptoms?.takeIf { it.isNotBlank() }
                        val observations = record.observations?.takeIf { it.isNotBlank() }
                        if (symptoms != null || observations != null) {
                            Text("Información adicional", style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 4.dp))
                            symptoms?.let { RecordTextBlock("Síntomas", it, R.drawable.ic_symptoms) }
                            observations?.let { RecordTextBlock("Observaciones", it, R.drawable.ic_observations) }
                        }
                        if (observations == null) {
                            Text("Sin observaciones adicionales", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        (record.data as? FoliarData)?.let { FoliarPhotoGallery(photos = it.photos) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordDetailHeader(record: Record?, crop: Crop?, onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        val fallback = painterResource(R.drawable.default_crop)
        AsyncImage(
            model = crop?.imagePath?.takeIf { it.isNotBlank() },
            contentDescription = crop?.let { "Imagen de ${it.name}" },
            placeholder = fallback, fallback = fallback, error = fallback,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(192.dp)
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
        )
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBackClick, shape = CircleShape,
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                contentColor = MaterialTheme.colorScheme.primary) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_chevron_right), "Volver",
                        modifier = Modifier.size(28.dp).rotate(180f))
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                modifier = Modifier.weight(1f)) {
                Text("Detalle de registro", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
        }
        if (record != null && crop != null) {
            Card(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 152.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(when (record.type) {
                            RecordType.SOIL -> R.drawable.analysis_soil
                            RecordType.WATER -> R.drawable.analysis_water
                            RecordType.FOLIAR -> R.drawable.analysis_foliar
                        }), null, modifier = Modifier.size(52.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(when (record.type) {
                                RecordType.SOIL -> "Análisis de suelo"
                                RecordType.WATER -> "Análisis de agua"
                                RecordType.FOLIAR -> "Análisis foliar"
                            }, style = MaterialTheme.typography.bodyLarge)
                            Text(if (crop.variety.isNullOrBlank()) crop.name else "${crop.name} · ${crop.variety}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    RecordDateAndStatus(record)
                }
            }
        }
    }
}

@Composable
private fun RecordDateAndStatus(record: Record) {
    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_calendar), null, modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Text(record.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Surface(shape = RoundedCornerShape(12.dp),
            color = if (record.status == RecordStatus.PENDING) OfflineBackground else MaterialTheme.colorScheme.primaryContainer) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                if (record.status == RecordStatus.PENDING || record.status == RecordStatus.VALIDATED) {
                    Icon(painterResource(if (record.status == RecordStatus.PENDING) R.drawable.ic_history else R.drawable.ic_check),
                        null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Text(record.status.historyLabel(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun RecordMeasurements(data: RecordData) {
    val measurements = when (data) {
        is SoilData -> listOf(
            Triple("pH", data.ph, R.drawable.ic_ph),
            Triple("Conductividad eléctrica (CE)", data.electricalConductivity, R.drawable.ic_conductivity),
            Triple("Nitrógeno (N)", data.nitrogen, R.drawable.ic_nitrogen),
            Triple("Fósforo (P)", data.phosphorus, R.drawable.ic_phosphorus),
            Triple("Potasio (K)", data.potassium, R.drawable.ic_potassium),
            Triple("Calcio (Ca)", data.calcium, R.drawable.ic_calcium),
            Triple("Magnesio (Mg)", data.magnesium, R.drawable.ic_magnesium),
            Triple("Materia orgánica", data.organicMatter, R.drawable.ic_organic_matter)
        )
        is WaterData -> listOf(
            Triple("pH", data.ph, R.drawable.ic_ph),
            Triple("Conductividad eléctrica (CE)", data.electricalConductivity, R.drawable.ic_conductivity),
            Triple("Nitrógeno (N)", data.nitrogen, R.drawable.ic_nitrogen),
            Triple("Fósforo (P)", data.phosphorus, R.drawable.ic_phosphorus),
            Triple("Potasio (K)", data.potassium, R.drawable.ic_potassium),
            Triple("Calcio (Ca)", data.calcium, R.drawable.ic_calcium),
            Triple("Magnesio (Mg)", data.magnesium, R.drawable.ic_magnesium)
        )
        is FoliarData -> listOf(
            Triple("Nitrógeno (N)", data.nitrogen, R.drawable.ic_nitrogen),
            Triple("Fósforo (P)", data.phosphorus, R.drawable.ic_phosphorus),
            Triple("Potasio (K)", data.potassium, R.drawable.ic_potassium),
            Triple("Calcio (Ca)", data.calcium, R.drawable.ic_calcium),
            Triple("Magnesio (Mg)", data.magnesium, R.drawable.ic_magnesium)
        )
    }.filter { it.second != null }
    if (measurements.isEmpty()) {
        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()) {
            Text("Sin mediciones registradas", style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        val fontScale = LocalDensity.current.fontScale
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 340.dp && fontScale <= 1.2f) 2 else 1
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                measurements.chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (label, value, icon) ->
                            RecordMeasurementCard(label, value.toString(), icon, Modifier.weight(1f))
                        }
                        if (row.size < columns) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordMeasurementCard(label: String, value: String, @DrawableRes icon: Int, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                RecordDetailIcon(icon)
                Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun RecordTextBlock(label: String, value: String, @DrawableRes icon: Int) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                RecordDetailIcon(icon)
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RecordDetailIcon(@DrawableRes icon: Int) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
        Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            Image(painterResource(icon), null, modifier = Modifier.requiredSize(40.dp))
        }
    }
}
