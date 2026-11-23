package com.nutriagricola.app.ui.programs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutriagricola.app.ui.advisor.programs.*
import java.time.LocalDate

@Composable
fun ProgramsContent(viewModel: ProgramsViewModel, modifier: Modifier = Modifier, onBack: () -> Unit = {},
                    headerActions: @Composable () -> Unit = {}) {
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
        ProgramMode.LIST -> ProgramListContent(viewModel, modifier, headerActions)
        ProgramMode.DETAIL -> ProgramDetailContent(viewModel, onBack, { showArchiveConfirmation = true }, modifier)
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
