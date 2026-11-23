package com.nutriagricola.app.ui.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.ui.advisor.programs.parseProgramNumber

@Composable
fun ProgramEditForm(viewModel: ProgramsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    val draft = state.editDraft ?: return
    val locked = state.saving || viewModel.hasPendingSave()
    Column(modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Editar programa", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(draft.name, { text -> viewModel.changeDraft { it.copy(name = text.take(150)) } },
            label = { Text("Nombre del programa *") }, enabled = !locked, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(draft.species, { text -> viewModel.changeDraft { it.copy(species = text.take(150)) } },
            label = { Text("Especie") }, enabled = !locked, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(draft.plot, { text -> viewModel.changeDraft { it.copy(plot = text.take(150)) } },
            label = { Text("Cuartel") }, enabled = !locked, modifier = Modifier.fillMaxWidth())
        Text("Requerimientos (kg/ha)", style = MaterialTheme.typography.titleMedium)
        Text("Un campo vacío conserva el requerimiento como no informado. Las dosis se ajustan manualmente.", style = MaterialTheme.typography.bodySmall)
        val labels = listOf("N", "P₂O₅", "K₂O", "CaO", "MgO", "S")
        draft.requirements.forEachIndexed { index, value ->
            EditNumber(value, labels[index], !locked, optional = true) { text -> viewModel.changeDraft {
                it.copy(requirements = it.requirements.toMutableList().also { values -> values[index] = text })
            } }
        }
        Text("Dosis de productos (kg/ha)", style = MaterialTheme.typography.titleMedium)
        draft.doses.forEachIndexed { index, value ->
            EditNumber(value, draft.original.content.fertilizers[index].name, !locked, optional = false) { text -> viewModel.changeDraft {
                it.copy(doses = it.doses.toMutableList().also { values -> values[index] = text })
            } }
        }
        Text("Se conservan las composiciones, fechas, etapas e indicaciones guardadas.", style = MaterialTheme.typography.bodySmall)
        ProgramStatus(state)
        if (viewModel.hasPendingSave() && !state.saving) Text("No se confirmó la respuesta. Reintenta para confirmar los mismos cambios.")
        Button(onClick = viewModel::saveEdit, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.saving) "Guardando…" else if (viewModel.hasPendingSave()) "Reintentar cambios" else "Guardar cambios")
        }
        Text("Necesitas conexión para cargar y guardar la edición.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EditNumber(value: String, label: String, enabled: Boolean, optional: Boolean, onChange: (String) -> Unit) {
    val invalid = !(optional && value.isBlank()) && parseProgramNumber(value) == null
    OutlinedTextField(value, onChange, label = { Text(label) }, enabled = enabled,
        modifier = Modifier.fillMaxWidth(), singleLine = true, isError = invalid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = if (invalid) ({ Text("Ingresa un número mayor o igual a cero") }) else null)
}
