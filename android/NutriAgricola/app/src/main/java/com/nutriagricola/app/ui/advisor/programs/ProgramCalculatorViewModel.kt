package com.nutriagricola.app.ui.advisor.programs

import androidx.lifecycle.ViewModel
import com.nutriagricola.app.domain.model.program.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProgramCalculatorState(
    val requirements: List<String> = List(5) { "" },
    val doses: List<String> = emptyList(),
    val error: String? = null
) {
    fun target(): NutrientAmounts? {
        val numbers = requirements.map { parseProgramNumber(it) ?: return null }
        return NutrientAmounts(numbers[0], numbers[1], numbers[2], numbers[3], numbers[4])
    }

    fun contributions(): NutrientAmounts? {
        if (doses.isEmpty()) return null
        val entries = doses.mapIndexed { index, text ->
            FertilizerDose(FertilizerCatalog.materials[index], parseProgramNumber(text) ?: return null)
        }
        return runCatching { ProgramCalculator.contributions(entries) }.getOrNull()
    }
}

internal fun parseProgramNumber(text: String): Double? = text.trim().replace(',', '.')
    .toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }

class ProgramCalculatorViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(ProgramCalculatorState())
    val state = mutableState.asStateFlow()

    fun setRequirement(index: Int, value: String) {
        mutableState.update { current -> current.copy(
            requirements = current.requirements.toMutableList().also { it[index] = value },
            doses = emptyList(), error = null
        ) }
    }

    fun setDose(index: Int, value: String) {
        mutableState.update { current -> current.copy(
            doses = current.doses.toMutableList().also { it[index] = value }, error = null
        ) }
    }

    fun calculate() {
        val current = mutableState.value
        val target = current.target()
        if (target == null) {
            mutableState.update { it.copy(error = "Completa los cinco requerimientos con números mayores o iguales a cero.") }
            return
        }
        runCatching { ProgramCalculator.suggest(target) }.fold(
            onSuccess = { doses -> mutableState.update { it.copy(doses = doses.map { dose -> dose.kgHa.toString() }, error = null) } },
            onFailure = { mutableState.update { it.copy(error = "Los valores ingresados son demasiado grandes.") } }
        )
    }
}
