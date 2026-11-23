package com.nutriagricola.app.ui.programs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutriagricola.app.domain.model.program.*
import com.nutriagricola.app.domain.model.user.UserRole
import com.nutriagricola.app.ui.advisor.programs.*
import java.time.LocalDate
import java.util.Locale

@Composable
fun ProgramsContent(viewModel: ProgramsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    var showArchiveConfirmation by remember(state.selectedId, state.mode) { mutableStateOf(false) }
    if (showArchiveConfirmation) AlertDialog(
        onDismissRequest = { showArchiveConfirmation = false },
        title = { Text("¿Archivar programa?") },
        text = { Text("El programa seguirá disponible para consulta y aparecerá como archivado.") },
        confirmButton = { TextButton(onClick = { showArchiveConfirmation = false; viewModel.archive() }) { Text("Archivar") } },
        dismissButton = { TextButton(onClick = { showArchiveConfirmation = false }) { Text("Cancelar") } }
    )
    BackHandler(enabled = state.mode != ProgramMode.LIST) { viewModel.back() }
    when (state.mode) {
        ProgramMode.LIST -> ProgramListContent(viewModel, modifier)
        ProgramMode.DETAIL -> Column(modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = viewModel::refreshDetail, enabled = !state.loading && !state.saving) { Text("Actualizar detalle") }
            ProgramStatus(state)
            val program = state.programs.find { it.id == state.selectedId }
            if (program != null) {
                if (viewModel.canManage(program)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = viewModel::edit, enabled = !state.loading && !state.saving) { Text("Editar") }
                        if (program.active) OutlinedButton(onClick = { showArchiveConfirmation = true },
                            enabled = !state.loading && !state.saving) { Text("Archivar") }
                    }
                }
                ProgramDetail(program)
            }
            else if (!state.loading) Text("El programa todavía no está disponible en este dispositivo.")
        }
        ProgramMode.EDIT -> ProgramEditForm(viewModel, modifier)
        ProgramMode.CREATE -> {
            val draftId = state.draftId ?: return
            val calculator: ProgramCalculatorViewModel = viewModel(key = "calculator-$draftId")
            var name by rememberSaveable(draftId) { mutableStateOf("") }
            var species by rememberSaveable(draftId) { mutableStateOf("") }
            var plot by rememberSaveable(draftId) { mutableStateOf("") }
            val date = remember(draftId) { LocalDate.now().toString() }
            val locked = state.saving || viewModel.hasPendingSave()
            ProgramCalculatorForm(modifier, calculator, enabled = !locked, header = {
                OutlinedTextField(name, { name = it.take(150) }, label = { Text("Nombre del programa *") }, enabled = !locked, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(species, { species = it.take(150) }, label = { Text("Especie (opcional)") }, enabled = !locked, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(plot, { plot = it.take(150) }, label = { Text("Cuartel (opcional)") }, enabled = !locked, modifier = Modifier.fillMaxWidth())
                Text("Fecha de inicio: $date", style = MaterialTheme.typography.bodySmall)
            }, footer = { calculation ->
                ProgramStatus(state)
                if (viewModel.hasPendingSave() && !state.saving) {
                    Text("No se confirmó la respuesta. Reintenta el mismo guardado para evitar duplicados.")
                }
                Button(enabled = !state.saving && name.isNotBlank() && calculation.contributions() != null,
                    modifier = Modifier.fillMaxWidth(), onClick = {
                        calculator.buildRequest(draftId, name, species, plot, date)?.let(viewModel::save)
                    }) { Text(if (state.saving) "Guardando…" else if (viewModel.hasPendingSave()) "Reintentar guardado" else "Guardar programa") }
                Text("Necesitas conexión para guardar. Será visible para todos los usuarios.", style = MaterialTheme.typography.bodySmall)
            })
        }
    }
}

@Composable
internal fun ProgramStatus(state: ProgramsState) {
    if (state.loading || state.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}

@Composable
private fun ProgramDetail(program: NutritionalProgram) {
    Text(program.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    if (!program.active) Text("Programa archivado")
    program.species?.takeIf { it.isNotBlank() }?.let { Text("Especie: $it") }
    program.plot?.takeIf { it.isNotBlank() }?.let { Text("Cuartel: $it") }
    Text("Inicio: ${program.startDate}")
    program.endDate?.let { Text("Fin: $it") }
    program.season?.let { Text("Temporada: $it") }
    program.advisorName?.let { Text("Asesor: $it") }
    if (!program.detailAvailable) {
        Text("Conecta y pulsa Actualizar detalle para descargar el contenido.")
        return
    }
    val labels = listOf("N", "P₂O₅", "K₂O", "CaO", "MgO", "S")
    Text("Requerimientos (kg/ha)", style = MaterialTheme.typography.titleMedium)
    program.requirements.forEachIndexed { index, value -> Text("${labels[index]}: ${value?.let(::displayNumber) ?: "Sin dato"}") }
    Text("Fertilizantes", style = MaterialTheme.typography.titleMedium)
    program.fertilizers.forEach {
        Text("${it.name}: ${displayNumber(it.doseKgHa)} kg/ha")
        it.notes?.takeIf(String::isNotBlank)?.let { notes -> Text(notes, style = MaterialTheme.typography.bodySmall) }
    }
    val totals = program.contributions()
    if (totals == null) {
        Text("Este programa no incluye composiciones suficientes para mostrar los aportes.", style = MaterialTheme.typography.bodySmall)
    } else {
        Text("Aportes de la mezcla (kg/ha)", style = MaterialTheme.typography.titleMedium)
        totals.values().forEachIndexed { index, amount -> Text("${labels[index]}: ${displayNumber(amount)}") }
    }
    if (program.stages.isNotEmpty()) Text("Etapas", style = MaterialTheme.typography.titleMedium)
    program.stages.forEach { stage ->
        Text(stage.name, fontWeight = FontWeight.Bold)
        stage.weeks?.let { Text("$it semanas") }
        stage.irrigationsPerWeek?.let { Text("$it riegos por semana") }
        stage.instructions?.takeIf(String::isNotBlank)?.let { Text(it) }
    }
    program.notes?.takeIf(String::isNotBlank)?.let { Text(it) }
}

private fun displayNumber(value: Double) = String.format(Locale.forLanguageTag("es-CL"), "%.2f", value)
