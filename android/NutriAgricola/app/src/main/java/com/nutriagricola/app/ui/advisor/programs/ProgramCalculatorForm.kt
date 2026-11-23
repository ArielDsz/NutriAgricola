package com.nutriagricola.app.ui.advisor.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutriagricola.app.domain.model.program.FertilizerCatalog
import java.util.Locale
import kotlin.math.abs

@Composable
fun ProgramCalculatorForm(modifier: Modifier = Modifier, calculator: ProgramCalculatorViewModel = viewModel(),
                          enabled: Boolean = true, header: @Composable () -> Unit = {},
                          footer: @Composable (ProgramCalculatorState) -> Unit = {}) {
    val state by calculator.state.collectAsState()
    val labels = listOf("N", "P₂O₅", "K₂O", "CaO", "MgO", "S")
    Column(modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Armar programa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        header()
        Text("1. Requerimientos nutricionales", style = MaterialTheme.typography.titleMedium)
        state.requirements.forEachIndexed { index, value ->
            ProgramNumberField(value, "${labels[index]} (kg/ha)", { calculator.setRequirement(index, it) }, state.error != null, enabled)
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = calculator::calculate, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text("Calcular dosis sugeridas") }
        if (state.doses.isNotEmpty()) {
            Text("2. Ajustar dosis de fertilizantes", style = MaterialTheme.typography.titleMedium)
            state.doses.forEachIndexed { index, value ->
                ProgramNumberField(value, "${FertilizerCatalog.materials[index].name} (kg/ha)", { calculator.setDose(index, it) }, enabled = enabled)
            }
            val totals = state.contributions()
            val target = state.target()
            if (totals != null && target != null) {
                Text("3. Aportes y balance (kg/ha)", style = MaterialTheme.typography.titleMedium)
                totals.values().forEachIndexed { index, amount ->
                    val difference = amount - target.values()[index]
                    val balance = if (index == 5) "Aporte adicional" else when {
                        abs(difference) < 0.005 -> "Cubre el requerimiento"
                        difference > 0 -> "Excedente: ${programNumber(difference)}"
                        else -> "Faltante: ${programNumber(-difference)}"
                    }
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${labels[index]}: ${programNumber(amount)}", fontWeight = FontWeight.Bold)
                            Text(balance, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                Text("Revisa las dosis: usa números finitos mayores o iguales a cero.", color = MaterialTheme.colorScheme.error)
            }
        }
        footer(state)
    }
}

@Composable
private fun ProgramNumberField(value: String, label: String, onChange: (String) -> Unit, requiredError: Boolean = false, enabled: Boolean = true) {
    val invalid = (value.isNotBlank() || requiredError) && parseProgramNumber(value) == null
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, enabled = enabled,
        modifier = Modifier.fillMaxWidth(), singleLine = true, isError = invalid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = if (invalid) ({ Text("Ingresa un número mayor o igual a cero") }) else null
    )
}

private fun programNumber(value: Double) = String.format(Locale.forLanguageTag("es-CL"), "%.2f", value)
