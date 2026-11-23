package com.nutriagricola.app.ui.programs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

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
    BackHandler(enabled = state.mode != ProgramMode.LIST && state.mode != ProgramMode.CREATE) { viewModel.back() }
    when (state.mode) {
        ProgramMode.LIST -> ProgramListContent(viewModel, modifier, headerActions)
        ProgramMode.DETAIL -> ProgramDetailContent(viewModel, onBack, { showArchiveConfirmation = true }, modifier)
        ProgramMode.EDIT -> ProgramEditForm(viewModel, modifier)
        ProgramMode.CREATE -> {
            val draftId = state.draftId ?: return
            ProgramWizardScreen(draftId, viewModel, onExit = { viewModel.back() }, modifier = modifier)
        }
    }
}

@Composable
internal fun ProgramStatus(state: ProgramsState) {
    if (state.loading || state.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}
