package com.nutriagricola.app.ui.programs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.program.*
import com.nutriagricola.app.ui.advisor.programs.parseProgramNumber
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramWizardScreen(id: String, programs: ProgramsViewModel, onExit: () -> Unit, modifier: Modifier = Modifier) {
    val wizard: ProgramWizardViewModel = viewModel(key = "program-wizard-$id")
    val state by wizard.state.collectAsState()
    val remote by programs.state.collectAsState()
    val locked = remote.saving || programs.hasPendingSave()
    var pickerOpen by remember { mutableStateOf(false) }
    var pickerKey by remember { mutableStateOf<String?>(null) }
    var materialInfo by remember { mutableStateOf<FertilizerMaterial?>(null) }
    val back = { if (!remote.saving) { if (locked || !wizard.previous()) onExit() }; Unit }
    BackHandler(onBack = back)
    Column(modifier.fillMaxSize().imePadding()) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))) {
            Image(painterResource(R.drawable.header_background), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(onClick = back, shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) {
                    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_chevron_right), "Volver", Modifier.size(26.dp).rotate(180f))
                    }
                }
                Text("Nuevo programa", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
                    Text("${state.step + 1}/5", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        LinearProgressIndicator(progress = { (state.step + 1) / 5f }, modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primaryContainer)
        key(state.step) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(listOf("Datos del programa", "Requerimientos", "Fertilizantes", "Etapas y riego", "Resumen")[state.step],
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                when (state.step) {
                    0 -> WizardData(state, wizard, !locked)
                    1 -> WizardRequirements(state, wizard, !locked)
                    2 -> {
                        var expanded by remember { mutableStateOf<String?>(null) }
                        state.fertilizers.forEach { row ->
                            WizardCard {
                                Row(Modifier.fillMaxWidth().clickable(enabled = !locked) { expanded = if (expanded == row.key) null else row.key },
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(painterResource(R.drawable.ic_program), null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(row.material.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    Text("${parseProgramNumber(row.dose)?.let(::wizardNumber) ?: "—"} kg/ha", style = MaterialTheme.typography.bodySmall)
                                    Icon(painterResource(R.drawable.ic_expand_more), "Editar fertilizante", Modifier.size(22.dp))
                                }
                                if (expanded == row.key) {
                                    WizardSelect("Fertilizante", row.material.name, !locked) { pickerKey = row.key; pickerOpen = true }
                                    WizardNumber(row.dose, "Dosis (kg/ha)", !locked) { wizard.setDose(row.key, it) }
                                    Text(materialComposition(row.material), style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        TextButton(onClick = { materialInfo = row.material }) { Text("Ficha") }
                                        TextButton(onClick = { wizard.removeMaterial(row.key) }, enabled = !locked) { Text("Quitar") }
                                    }
                                }
                            }
                        }
                        OutlinedButton(onClick = { pickerKey = null; pickerOpen = true }, enabled = !locked,
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Icon(painterResource(R.drawable.ic_add_circle), null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Agregar fertilizante")
                        }
                        TextButton(onClick = { wizard.suggest() }, enabled = !locked) { Text("Sugerir dosis") }
                        WizardBalance(state)
                    }
                    3 -> WizardStages(state, wizard, !locked)
                    4 -> WizardSummary(state)
                }
            }
        }
        Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                remote.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                if (remote.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.step > 0) OutlinedButton(onClick = { wizard.previous() }, enabled = !locked,
                        shape = RoundedCornerShape(14.dp), modifier = Modifier.heightIn(min = 50.dp)) { Text("Atrás") }
                    Button(onClick = {
                        if (state.step < 4) wizard.next()
                        else wizard.buildRequest(id)?.let(programs::save)
                    }, enabled = !remote.saving, shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 50.dp)) {
                        Text(when { remote.saving -> "Guardando…"; programs.hasPendingSave() -> "Reintentar guardado"
                            state.step == 4 -> "Guardar programa"; else -> "Continuar" })
                    }
                }
            }
        }
    }
    if (pickerOpen) MaterialPicker(onDismiss = { pickerOpen = false }, onPick = {
        wizard.pickMaterial(pickerKey, it.id); pickerOpen = false
    }, onInfo = { materialInfo = it })
    materialInfo?.let { material -> MaterialInformation(material) { materialInfo = null } }
}

@Composable
private fun WizardData(state: ProgramWizardState, wizard: ProgramWizardViewModel, enabled: Boolean) {
    var showCrops by remember { mutableStateOf(false) }
    WizardCard {
        OutlinedTextField(state.name, { text -> wizard.change { it.copy(name = text.take(150)) } }, label = { Text("Nombre del programa") },
            singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Box {
            WizardSelect("Especie", state.crop.name, enabled) { showCrops = true }
            DropdownMenu(showCrops, { showCrops = false }) {
                CropNutritionCatalog.profiles.forEachIndexed { index, crop ->
                    DropdownMenuItem(text = { Text(crop.name) }, onClick = { wizard.selectCrop(index); showCrops = false })
                }
            }
        }
        OutlinedTextField(state.plot, { text -> wizard.change { it.copy(plot = text.take(150)) } }, label = { Text("Cuartel") },
            singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(state.season, { text -> wizard.change { it.copy(season = text.take(30)) } }, label = { Text("Temporada") },
            singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        WizardDate("Inicio", state.startDate, enabled, false) { value -> wizard.change { it.copy(startDate = value) } }
        WizardDate("Fin", state.endDate, enabled, true) { value -> wizard.change { it.copy(endDate = value) } }
    }
}

@Composable
private fun WizardRequirements(state: ProgramWizardState, wizard: ProgramWizardViewModel, enabled: Boolean) {
    WizardCard {
        WizardNumber(state.yieldTonHa, "Rendimiento (t/ha)", enabled) { value -> wizard.change { it.copy(yieldTonHa = value) } }
        WizardNumber(state.efficiency, "Eficiencia de riego (%)", enabled) { value -> wizard.change { it.copy(efficiency = value) } }
        OutlinedButton(onClick = { wizard.calculateRequirements() }, enabled = enabled, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)) { Text("Calcular requerimientos") }
    }
    Text("Requerimientos (kg/ha)", style = MaterialTheme.typography.bodyLarge)
    WizardCard {
        nutrientLabels.take(5).forEachIndexed { index, label ->
            WizardNumber(state.requirements[index], label, enabled) { value -> wizard.change {
                it.copy(requirements = it.requirements.toMutableList().also { values -> values[index] = value })
            } }
        }
    }
}

@Composable
private fun WizardStages(state: ProgramWizardState, wizard: ProgramWizardViewModel, enabled: Boolean) {
    var expanded by remember { mutableStateOf<Int?>(0) }
    state.stages.forEachIndexed { index, stage ->
        WizardCard {
            Row(Modifier.fillMaxWidth().clickable(enabled) { expanded = if (expanded == index) null else index },
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stage.name, style = MaterialTheme.typography.bodyMedium)
                    Text("${stage.weeks} semanas · ${stage.irrigations} riegos/semana · ${stage.percent}%",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(painterResource(R.drawable.ic_expand_more), "Editar etapa", Modifier.size(24.dp))
            }
            if (expanded == index) {
                WizardNumber(stage.weeks, "Semanas", enabled, true) { value -> wizard.stage(index) { it.copy(weeks = value) } }
                WizardNumber(stage.irrigations, "Riegos por semana", enabled, true) { value -> wizard.stage(index) { it.copy(irrigations = value) } }
                WizardNumber(stage.percent, "Distribución (%)", enabled) { value -> wizard.stage(index) { it.copy(percent = value) } }
                OutlinedTextField(stage.instructions, { value -> wizard.stage(index) { it.copy(instructions = value) } }, label = { Text("Indicaciones") },
                    enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2)
            }
        }
    }
    WizardCard {
        Text("Distribución: ${wizardNumber(state.allocationTotal)}%", style = MaterialTheme.typography.bodyLarge,
            color = if (abs(state.allocationTotal - 100) < 0.01) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        Text("${state.totalWeeks} semanas · ${state.totalIrrigations} riegos", style = MaterialTheme.typography.bodyMedium)
    }
    OutlinedTextField(state.notes, { value -> wizard.change { it.copy(notes = value) } }, label = { Text("Indicaciones de riego") },
        enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), minLines = 2)
}

@Composable
private fun WizardSummary(state: ProgramWizardState) {
    WizardCard {
        Text(state.name, style = MaterialTheme.typography.titleMedium)
        Text(state.crop.name, style = MaterialTheme.typography.bodyMedium)
        Text("${state.yieldTonHa} t/ha · ${state.efficiency}% de eficiencia", style = MaterialTheme.typography.bodySmall)
        if (state.plot.isNotBlank()) Text(state.plot, style = MaterialTheme.typography.bodySmall)
        Text(displayDate(state.startDate) + if (state.endDate.isBlank()) "" else " — ${displayDate(state.endDate)}", style = MaterialTheme.typography.bodySmall)
    }
    WizardBalance(state)
    Text("Fertilizantes", style = MaterialTheme.typography.bodyLarge)
    WizardCard {
        state.fertilizers.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(row.material.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text("${parseProgramNumber(row.dose)?.let(::wizardNumber) ?: "—"} kg/ha", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    Text("Etapas", style = MaterialTheme.typography.bodyLarge)
    state.stages.forEach { stage ->
        WizardCard {
            Text(stage.name, style = MaterialTheme.typography.bodyMedium)
            Text("${stage.percent}% · ${stage.weeks} semanas · ${stage.irrigations} riegos/semana", style = MaterialTheme.typography.bodySmall)
            if (stage.instructions.isNotBlank()) Text(stage.instructions, style = MaterialTheme.typography.bodySmall)
        }
    }
    if (state.notes.isNotBlank()) WizardCard {
        Text("Indicaciones", style = MaterialTheme.typography.bodyLarge)
        Text(state.notes, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun WizardBalance(state: ProgramWizardState) {
    val total = state.contributions()?.values() ?: return
    val target = state.target()?.values() ?: return
    Text("Aportes (kg/ha)", style = MaterialTheme.typography.bodyLarge)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        total.indices.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { index ->
                    Surface(Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(nutrientLabels[index], style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(wizardNumber(total[index]), style = MaterialTheme.typography.titleMedium)
                            Text("Objetivo: ${wizardNumber(target[index])}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun WizardNumber(value: String, label: String, enabled: Boolean, integer: Boolean = false, onChange: (String) -> Unit) {
    val invalid = value.isNotBlank() && (parseProgramNumber(value) == null || (integer && value.toIntOrNull() == null))
    OutlinedTextField(value, onChange, label = { Text(label) }, enabled = enabled, singleLine = true,
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), isError = invalid,
        keyboardOptions = KeyboardOptions(keyboardType = if (integer) KeyboardType.Number else KeyboardType.Decimal))
}

@Composable
private fun WizardSelect(label: String, value: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(painterResource(R.drawable.ic_expand_more), null, Modifier.size(22.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardDate(label: String, value: String, enabled: Boolean, optional: Boolean, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    WizardSelect(label, if (value.isBlank()) "Seleccionar" else displayDate(value), enabled) { open = true }
    if (open) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = value.takeIf { it.isNotBlank() }?.let {
            LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        })
        DatePickerDialog(onDismissRequest = { open = false }, confirmButton = {
            TextButton(enabled = picker.selectedDateMillis != null, onClick = {
                picker.selectedDateMillis?.let { onSelect(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()) }; open = false
            }) { Text("Aceptar") }
        }, dismissButton = {
            Row {
                if (optional) TextButton(onClick = { onSelect(""); open = false }) { Text("Quitar") }
                TextButton(onClick = { open = false }) { Text("Cancelar") }
            }
        }) { DatePicker(picker) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialPicker(onDismiss: () -> Unit, onPick: (FertilizerMaterial) -> Unit, onInfo: (FertilizerMaterial) -> Unit) {
    var query by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Fertilizantes", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(query, { query = it }, placeholder = { Text("Buscar") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 440.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(FertilizerCatalog.materials.filter { it.name.contains(query, true) }, key = { it.id }) { material ->
                    ListItem(headlineContent = { Text(material.name, style = MaterialTheme.typography.bodyMedium) },
                        supportingContent = { Text(materialComposition(material), style = MaterialTheme.typography.bodySmall) },
                        trailingContent = { IconButton(onClick = { onInfo(material) }) { Icon(painterResource(R.drawable.ic_visibility), "Ficha", Modifier.size(22.dp)) } },
                        modifier = Modifier.clickable { onPick(material) }, colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background))
                }
            }
        }
    }
}

@Composable
private fun MaterialInformation(material: FertilizerMaterial, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(material.name) }, confirmButton = {
        TextButton(onClick = onDismiss) { Text("Cerrar") }
    }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(materialComposition(material))
            material.solubilityGramsL?.let { Text("Solubilidad (20 °C): ${wizardNumber(it)} g/L") }
            material.solutionPh?.let { Text("pH (solución 1%): ${wizardNumber(it)}") }
            material.conductivityDsM?.let { Text("CE (1 g/L): ${wizardNumber(it)} dS/m") }
            material.tankGroup?.let { Text("Grupo: $it") }
        }
    })
}

private val nutrientLabels = listOf("N", "P₂O₅", "K₂O", "CaO", "MgO", "S")
private fun materialComposition(material: FertilizerMaterial) = material.percentages.values().mapIndexedNotNull { index, value ->
    if (value == 0.0) null else "${nutrientLabels[index]} ${programInput(value)}%"
}.joinToString(" · ")
private fun displayDate(value: String) = runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrDefault(value)
private fun wizardNumber(value: Double) = String.format(Locale.forLanguageTag("es-CL"), "%.2f", value)
