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
import androidx.compose.ui.draw.rotate
import com.nutriagricola.app.domain.model.advisor.ReviewResult
import java.time.format.DateTimeFormatter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.ui.farmer.record.detail.RecordDetailHeader
import com.nutriagricola.app.ui.farmer.record.detail.RecordMeasurements
import com.nutriagricola.app.ui.farmer.record.detail.RecordTextBlock
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

    val record = state.record
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->
        Column(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()).verticalScroll(rememberScrollState())) {
            RecordDetailHeader(record, state.crop, onBackClick)
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when {
                    state.isLoading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    state.loadError != null -> {
                        Text(state.loadError ?: "Registro no disponible", color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::loadDetail) { Text("Reintentar") }
                    }
                    else -> if (record != null) {
                        val identity = listOfNotNull(state.farm?.name,
                            state.farmerUser?.let { "${it.firstName} ${it.lastName}".trim().takeIf(String::isNotBlank) }).joinToString("\n")
                        if (identity.isNotBlank()) RecordTextBlock("Predio y agricultor", identity, R.drawable.ic_farms)
                        Text("Datos del análisis", style = MaterialTheme.typography.bodyLarge)
                        RecordMeasurements(record.data)
                        (record.data as? WaterData)?.waterSource?.takeIf(String::isNotBlank)?.let {
                            RecordTextBlock("Fuente de agua", it, R.drawable.ic_water_source)
                        }
                        (record.data as? FoliarData)?.symptoms?.takeIf(String::isNotBlank)?.let {
                            RecordTextBlock("Síntomas", it, R.drawable.ic_symptoms)
                        }
                        record.observations?.takeIf(String::isNotBlank)?.let {
                            RecordTextBlock("Observaciones", it, R.drawable.ic_observations)
                        }
                        if (state.remotePhotoCount > 0) {
                            TextButton(onClick = viewModel::downloadPhotos, enabled = !state.isDownloadingPhotos) {
                                Text(if (state.isDownloadingPhotos) "Descargando fotos…" else "Descargar fotos")
                            }
                            state.photoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                        (record.data as? FoliarData)?.let { FoliarPhotoGallery(photos = it.photos) }
                        // Existing Review Section
                        var showPreviousReviews by remember(record.id) { mutableStateOf(false) }
                        if (state.reviews.size > 1) {
                            TextButton(onClick = { showPreviousReviews = !showPreviousReviews }) {
                                Text(if (showPreviousReviews) "Ocultar revisiones anteriores" else "Ver revisiones anteriores (${state.reviews.size - 1})")
                            }
                        }
                        val visibleReviews = if (showPreviousReviews) state.reviews else state.reviews.take(1)
                        visibleReviews.forEachIndexed { index, review ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (review.result == ReviewResult.VALIDATED) LeafGreen.copy(alpha = 0.1f) else OfflineBackground
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (index == 0) "Última revisión" else "Revisión anterior",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (review.result == ReviewResult.VALIDATED) ForestGreen else OfflineBackground
                                        ) {
                                            Text(
                                                text = if (review.result == ReviewResult.VALIDATED) "VALIDADO" else "OBSERVADO",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (review.result == ReviewResult.VALIDATED) MaterialTheme.colorScheme.onPrimary else ForestGreen,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Asesor: ${review.advisorName} · ${review.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}",
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
                                    text = "Revisión",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(if (state.canReview) "Requiere conexión"
                                    else "Este análisis no está disponible para enviar una revisión.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                OutlinedTextField(
                                    value = state.commentInput,
                                    onValueChange = { viewModel.onCommentChange(it) },
                                    label = { Text("Comentario del asesor") },
                                    placeholder = { Text("Comentario") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 96.dp),
                                    maxLines = 4,
                                    enabled = !state.isSaving && state.canReview
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.submitReview(ReviewResult.OBSERVED) },
                                        enabled = !state.isSaving && state.canReview,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Observar")
                                    }
                                    Button(
                                        onClick = { viewModel.submitReview(ReviewResult.VALIDATED) },
                                        enabled = !state.isSaving && state.canReview,
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
}
