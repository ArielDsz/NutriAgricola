package com.nutriagricola.app.ui.programs

import androidx.lifecycle.ViewModel
import com.nutriagricola.app.data.mapper.toDto
import com.nutriagricola.app.data.remote.dto.*
import com.nutriagricola.app.domain.model.program.*
import com.nutriagricola.app.ui.advisor.programs.parseProgramNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlin.math.abs
import kotlin.math.floor

internal fun programInput(value: Double): String = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()

data class WizardFertilizer(val materialId: String, val dose: String, val key: String = UUID.randomUUID().toString()) {
    val material get() = FertilizerCatalog.materials.first { it.id == materialId }
}
data class WizardStage(val name: String, val weeks: String, val irrigations: String, val percent: String,
                        val instructions: String = "")

private fun defaultRequirements() = CropNutritionCatalog.requirements(CropNutritionCatalog.profiles.first(), 50.0, 90.0)
    .values().take(5).map(::programInput)

data class ProgramWizardState(
    val step: Int = 0, val name: String = "", val cropIndex: Int = 0, val plot: String = "",
    val season: String = "${LocalDate.now().year}-${LocalDate.now().year + 1}",
    val startDate: String = LocalDate.now().toString(), val endDate: String = "",
    val yieldTonHa: String = "50", val efficiency: String = "90",
    val requirements: List<String> = defaultRequirements(),
    val fertilizers: List<WizardFertilizer> = emptyList(),
    val stages: List<WizardStage> = listOf(
        WizardStage("Brotación / Crecimiento vegetativo", "6", "3", "35"),
        WizardStage("Floración / Cuaja", "4", "4", "25"),
        WizardStage("Desarrollo de fruta / Pre-cosecha", "8", "5", "30"),
        WizardStage("Post-cosecha", "4", "2", "10")
    ), val notes: String = "", val error: String? = null
) {
    val crop get() = CropNutritionCatalog.profiles[cropIndex]
    fun target(): NutrientAmounts? {
        val values = requirements.map { parseProgramNumber(it) ?: return null }
        return NutrientAmounts(values[0], values[1], values[2], values[3], values[4], values[4] * 0.8)
    }
    fun doses(): List<FertilizerDose>? = fertilizers.map { row ->
        FertilizerDose(row.material, parseProgramNumber(row.dose) ?: return null)
    }
    fun contributions(): NutrientAmounts? = runCatching { ProgramCalculator.contributions(doses() ?: return null) }.getOrNull()
    val allocationTotal get() = stages.sumOf { parseProgramNumber(it.percent) ?: 0.0 }
    val totalWeeks get() = stages.sumOf { it.weeks.toIntOrNull() ?: 0 }
    val totalIrrigations get() = stages.sumOf { (it.weeks.toLongOrNull() ?: 0) * (it.irrigations.toLongOrNull() ?: 0) }

    fun validate(step: Int) {
        when (step) {
            0 -> {
                require(name.isNotBlank() && name.trim().length <= 150) { "Escribe un nombre de hasta 150 caracteres" }
                require(plot.length <= 150 && season.length <= 30) { "Revisa el cuartel y la temporada" }
                val start = runCatching { LocalDate.parse(startDate) }.getOrNull()
                require(start != null) { "Selecciona la fecha de inicio" }
                if (endDate.isNotBlank()) {
                    val end = runCatching { LocalDate.parse(endDate) }.getOrNull()
                    require(end != null && !end.isBefore(start)) { "La fecha final no puede ser anterior al inicio" }
                }
            }
            1 -> {
                require(parseProgramNumber(yieldTonHa) != null) { "Revisa el rendimiento" }
                val percent = parseProgramNumber(efficiency)
                require(percent != null && percent > 0 && percent <= 100) { "La eficiencia debe ser mayor que 0 y hasta 100%" }
                require(target() != null) { "Completa los requerimientos con valores mayores o iguales a cero" }
            }
            2 -> {
                require(fertilizers.isNotEmpty()) { "Agrega al menos un fertilizante" }
                require(fertilizers.size <= 100 && doses() != null && contributions() != null) { "Revisa las dosis de los fertilizantes" }
            }
            3 -> {
                require(stages.isNotEmpty()) { "Agrega las etapas del programa" }
                stages.forEach {
                    require((it.weeks.toIntOrNull() ?: 0) > 0 && (it.irrigations.toIntOrNull() ?: -1) >= 0) { "Revisa las semanas y los riegos de cada etapa" }
                    val percent = parseProgramNumber(it.percent)
                    require(percent != null && percent <= 100) { "Revisa los porcentajes de las etapas" }
                }
                require(abs(allocationTotal - 100) < 0.01) { "La distribución debe sumar 100%" }
            }
        }
    }

    fun request(id: String): ProgramRequestDto {
        (0..3).forEach(::validate)
        return ProgramRequestDto(id, crop.name, plot.trim().ifBlank { null }, ProgramContentDto(
            name = name.trim(), startDate = startDate, endDate = endDate.ifBlank { null }, season = season.trim().ifBlank { null },
            targetYieldTonHa = parseProgramNumber(yieldTonHa), irrigationEfficiencyPercent = parseProgramNumber(efficiency),
            nutrients = target()!!.toDto(), fertilizers = doses()!!.map {
                ProgramFertilizerDto(it.material.name, it.kgHa, composition = it.material.percentages.toDto())
            }, stages = stages.map { ProgramStageDto(it.name, it.weeks.toInt(), it.irrigations.toInt(),
                it.instructions.trim().ifBlank { null }, parseProgramNumber(it.percent)) },
            irrigationNotes = notes.trim().ifBlank { null }
        ))
    }
}

class ProgramWizardViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(ProgramWizardState())
    val state = mutableState.asStateFlow()

    fun change(update: (ProgramWizardState) -> ProgramWizardState) {
        mutableState.update { update(it).copy(error = null) }
    }
    fun selectCrop(index: Int) {
        val profile = CropNutritionCatalog.profiles[index]
        change { it.copy(cropIndex = index, yieldTonHa = programInput(profile.baseYieldTonHa)) }
        calculateRequirements()
    }
    fun calculateRequirements(): Boolean = attempt {
        val current = state.value
        val yield = requireNotNull(parseProgramNumber(current.yieldTonHa)) { "Revisa el rendimiento" }
        val efficiency = requireNotNull(parseProgramNumber(current.efficiency)) { "Revisa la eficiencia" }
        val target = CropNutritionCatalog.requirements(current.crop, yield, efficiency)
        change { it.copy(requirements = target.values().take(5).map(::programInput)) }
    }
    fun suggest(): Boolean = attempt {
        val target = requireNotNull(state.value.target()) { "Revisa los requerimientos" }
        val doses = ProgramCalculator.suggest(target)
        // La página redondea a kg enteros al transferir la receta a su calculadora.
        change { it.copy(fertilizers = doses.map { dose -> WizardFertilizer(dose.material.id, programInput(floor(dose.kgHa + 0.5))) }) }
    }
    fun next(): Boolean = attempt {
        val current = state.value
        current.validate(current.step)
        if (current.step == 1 && current.fertilizers.isEmpty()) check(suggest()) { state.value.error ?: "Revisa la mezcla" }
        if (current.step < 4) change { it.copy(step = it.step + 1) }
    }
    fun previous(): Boolean {
        if (state.value.step == 0) return false
        change { it.copy(step = it.step - 1) }
        return true
    }
    fun pickMaterial(key: String?, materialId: String) {
        require(FertilizerCatalog.materials.any { it.id == materialId })
        change { current -> current.copy(fertilizers = if (key == null) current.fertilizers + WizardFertilizer(materialId, "0")
            else current.fertilizers.map { if (it.key == key) it.copy(materialId = materialId) else it }) }
    }
    fun setDose(key: String, value: String) { change { current -> current.copy(fertilizers = current.fertilizers.map { if (it.key == key) it.copy(dose = value) else it }) } }
    fun removeMaterial(key: String) { change { it.copy(fertilizers = it.fertilizers.filterNot { row -> row.key == key }) } }
    fun stage(index: Int, update: (WizardStage) -> WizardStage) { change { it.copy(stages = it.stages.mapIndexed { i, stage -> if (index == i) update(stage) else stage }) } }
    fun buildRequest(id: String): ProgramRequestDto? {
        var request: ProgramRequestDto? = null
        attempt { request = state.value.request(id) }
        return request
    }
    private fun attempt(action: () -> Unit): Boolean = try { action(); true } catch (error: IllegalArgumentException) {
        mutableState.update { it.copy(error = error.message ?: "Revisa los datos") }; false
    } catch (error: IllegalStateException) {
        mutableState.update { it.copy(error = error.message ?: "Revisa los datos") }; false
    }
}
