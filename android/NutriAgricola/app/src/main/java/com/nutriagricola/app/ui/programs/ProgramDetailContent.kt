package com.nutriagricola.app.ui.programs

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.program.NutritionalProgram
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProgramDetailContent(viewModel: ProgramsViewModel, onBack: () -> Unit, onArchive: () -> Unit,
                           modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    val program = state.programs.find { it.id == state.selectedId }
    Column(modifier.verticalScroll(rememberScrollState())) {
        ProgramDetailHeader(program, onBack)
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ProgramStatus(state)
            if (program == null) {
                if (!state.loading) Text("Programa no disponible", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }
            if (viewModel.canManage(program)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = viewModel::edit, enabled = !state.loading && !state.saving) { Text("Editar") }
                    if (program.active) OutlinedButton(onClick = onArchive, enabled = !state.loading && !state.saving) { Text("Archivar") }
                }
            }
            if (!program.detailAvailable) {
                if (!state.loading) Text("Detalle no disponible sin conexión", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }
            if (program.requirements.any { it != null }) {
                Text("Requerimientos", style = MaterialTheme.typography.bodyLarge)
                ProgramNutrientGrid(program.requirements)
            }
            if (program.fertilizers.isNotEmpty()) {
                Text("Fertilizantes", style = MaterialTheme.typography.bodyLarge)
                program.fertilizers.forEach { item ->
                    ProgramInfoCard(item.name, R.drawable.ic_program) {
                        Text("${programNumber(item.doseKgHa)} kg/ha", style = MaterialTheme.typography.titleMedium)
                        item.notes?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
            program.contributions()?.let { amounts ->
                Text("Aportes de la mezcla", style = MaterialTheme.typography.bodyLarge)
                ProgramNutrientGrid(amounts.values())
            }
            if (program.stages.isNotEmpty()) {
                Text("Etapas", style = MaterialTheme.typography.bodyLarge)
                program.stages.forEach { stage ->
                    ProgramInfoCard(stage.name, R.drawable.ic_calendar) {
                        stage.weeks?.let { Text("$it semanas", style = MaterialTheme.typography.bodyMedium) }
                        stage.irrigationsPerWeek?.let { Text("$it riegos por semana", style = MaterialTheme.typography.bodyMedium) }
                        stage.instructions?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
            val extra = listOfNotNull(
                program.endDate?.let { "Fin: ${programDate(it)}" },
                program.season?.takeIf { it.isNotBlank() }?.let { "Temporada: $it" },
                program.plot?.takeIf { it.isNotBlank() }?.let { "Cuartel: $it" },
                program.advisorName?.takeIf { it.isNotBlank() }?.let { "Asesor: $it" }
            )
            if (extra.isNotEmpty() || !program.notes.isNullOrBlank()) {
                Text("Información adicional", style = MaterialTheme.typography.bodyLarge)
                if (extra.isNotEmpty()) ProgramInfoCard("Información del programa", R.drawable.ic_program) {
                    extra.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
                program.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    ProgramInfoCard("Indicaciones", R.drawable.ic_program) { Text(notes, style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
    }
}

@Composable
private fun ProgramDetailHeader(program: NutritionalProgram?, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Image(painterResource(R.drawable.default_crop), null, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(192.dp).clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)))
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBack, shape = CircleShape, color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                contentColor = MaterialTheme.colorScheme.primary) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_chevron_right), "Volver", Modifier.size(28.dp).rotate(180f))
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                modifier = Modifier.weight(1f)) {
                Text("Detalle de programa", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
        }
        if (program != null) Card(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 152.dp),
            shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Icon(painterResource(R.drawable.ic_program), null, Modifier.padding(12.dp).size(28.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(program.name, style = MaterialTheme.typography.bodyLarge)
                        program.species?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_calendar), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(programDate(program.startDate), style = MaterialTheme.typography.bodyMedium)
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = if (program.active) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant) {
                        Text(if (program.active) "Activo" else "Archivado", style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramNutrientGrid(values: List<Double?>) {
    val labels = listOf("Nitrógeno (N)", "Fósforo (P₂O₅)", "Potasio (K₂O)", "Calcio (CaO)", "Magnesio (MgO)", "Azufre (S)")
    val icons = listOf(R.drawable.ic_nitrogen, R.drawable.ic_phosphorus, R.drawable.ic_potassium,
        R.drawable.ic_calcium, R.drawable.ic_magnesium, R.drawable.ic_program)
    val entries = values.mapIndexedNotNull { index, value -> value?.let { index to it } }
    val scale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 340.dp && scale <= 1.2f) 2 else 1
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            entries.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (index, value) ->
                        Card(Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    ProgramDetailIcon(icons[index])
                                    Text(labels[index], style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                }
                                Text("${programNumber(value)} kg/ha", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    if (row.size < columns) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ProgramInfoCard(title: String, @DrawableRes icon: Int, content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ProgramDetailIcon(icon)
                Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            }
            content()
        }
    }
}

@Composable
private fun ProgramDetailIcon(@DrawableRes icon: Int) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun programDate(date: String) = runCatching { LocalDate.parse(date).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrDefault(date)
private fun programNumber(value: Double) = String.format(Locale.forLanguageTag("es-CL"), "%.2f", value)
