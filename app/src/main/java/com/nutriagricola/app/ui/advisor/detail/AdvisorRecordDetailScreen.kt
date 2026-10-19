package com.nutriagricola.app.ui.advisor.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.record.*
import com.nutriagricola.app.ui.farmer.record.components.FoliarPhotoGallery
import com.nutriagricola.app.ui.theme.ForestGreen
import com.nutriagricola.app.ui.theme.LeafGreen
import com.nutriagricola.app.ui.theme.OfflineBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisorRecordDetailScreen(
    viewModel: AdvisorRecordDetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisión de Asesoría", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = "Volver",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null && state.record == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.error ?: "Error", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadDetail() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else {
            val record = state.record
            val crop = state.crop
            val farm = state.farm
            val farmer = state.farmerUser

            if (record != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(
                                    when (record.type) {
                                        RecordType.SOIL -> R.drawable.analysis_soil
                                        RecordType.WATER -> R.drawable.analysis_water
                                        RecordType.FOLIAR -> R.drawable.analysis_foliar
                                    }
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${farm?.name ?: "Predio"} — ${crop?.name ?: "Cultivo"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Agricultor: ${farmer?.firstName ?: ""} ${farmer?.lastName ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Fecha muestra: ${record.date}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Original Measurement Data
                    Text(
                        text = "Mediciones del Análisis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    RecordMeasurementsView(record.data)

                    // Farmer Observations
                    if (!record.observations.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Observaciones del Agricultor",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = record.observations,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Foliar Photos
                    (record.data as? FoliarData)?.let { foliarData ->
                        if (foliarData.photos.isNotEmpty()) {
                            Text(
                                text = "Fotografías del Cultivo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            FoliarPhotoGallery(photos = foliarData.photos)
                        }
                    }

                    // Existing Review Section
                    state.review?.let { review ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (review.result == RecordStatus.VALIDATED) LeafGreen.copy(alpha = 0.1f) else OfflineBackground
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Dictamen del Asesor",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (review.result == RecordStatus.VALIDATED) ForestGreen else OfflineBackground
                                    ) {
                                        Text(
                                            text = if (review.result == RecordStatus.VALIDATED) "VALIDADO" else "REVISADO / OBSERVADO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (review.result == RecordStatus.VALIDATED) MaterialTheme.colorScheme.onPrimary else ForestGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Asesor: ${review.advisorName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = review.comment,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Advisor Action / Form Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Emitir Dictamen de Asesoría",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.commentInput,
                                onValueChange = { viewModel.onCommentChange(it) },
                                label = { Text("Comentarios u observaciones del asesor") },
                                placeholder = { Text("Escribe aquí tus recomendaciones técnicas...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 96.dp),
                                maxLines = 4
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.submitReview(RecordStatus.VIEWED) },
                                    enabled = !state.isSaving,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Observar")
                                }
                                Button(
                                    onClick = { viewModel.submitReview(RecordStatus.VALIDATED) },
                                    enabled = !state.isSaving,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Validar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordMeasurementsView(data: RecordData) {
    val measurements = when (data) {
        is SoilData -> listOf(
            "pH" to data.ph,
            "Conductividad eléctrica (CE)" to data.electricalConductivity,
            "Nitrógeno (N)" to data.nitrogen,
            "Fósforo (P)" to data.phosphorus,
            "Potasio (K)" to data.potassium,
            "Calcio (Ca)" to data.calcium,
            "Magnesio (Mg)" to data.magnesium,
            "Materia orgánica" to data.organicMatter
        )
        is WaterData -> listOf(
            "pH" to data.ph,
            "Conductividad eléctrica (CE)" to data.electricalConductivity,
            "Nitrógeno (N)" to data.nitrogen,
            "Fósforo (P)" to data.phosphorus,
            "Potasio (K)" to data.potassium,
            "Calcio (Ca)" to data.calcium,
            "Magnesio (Mg)" to data.magnesium
        )
        is FoliarData -> listOf(
            "Nitrógeno (N)" to data.nitrogen,
            "Fósforo (P)" to data.phosphorus,
            "Potasio (K)" to data.potassium,
            "Calcio (Ca)" to data.calcium,
            "Magnesio (Mg)" to data.magnesium
        )
    }.filter { it.second != null }

    if (measurements.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sin mediciones registradas en esta muestra",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            measurements.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (label, value) ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = value.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (row.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
