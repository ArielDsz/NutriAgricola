package com.nutriagricola.app.ui.programs

import com.nutriagricola.app.data.mapper.toValues
import com.nutriagricola.app.data.remote.dto.ProgramNutrientsDto
import com.nutriagricola.app.data.remote.dto.ProgramRequestDto
import com.nutriagricola.app.ui.advisor.programs.parseProgramNumber

/** Conserva el contenido original; editar dosis no sustituye productos ni composiciones. */
data class ProgramEditDraft(
    val original: ProgramRequestDto,
    val name: String = original.content.name,
    val species: String = original.species.orEmpty(),
    val plot: String = original.plot.orEmpty(),
    val requirements: List<String> = original.content.nutrients.toValues().map { it?.toString().orEmpty() },
    val doses: List<String> = original.content.fertilizers.map { it.doseKgHa.toString() }
) {
    fun buildRequest(): ProgramRequestDto {
        require(name.isNotBlank() && name.trim().length <= 150) { "El nombre es obligatorio y admite hasta 150 caracteres" }
        require(species.trim().length <= 150 && plot.trim().length <= 150) { "Especie y cuartel admiten hasta 150 caracteres" }
        val values = requirements.map { text ->
            if (text.isBlank()) null else requireNotNull(parseProgramNumber(text)) {
                "Los requerimientos deben ser números finitos mayores o iguales a cero"
            }
        }
        val fertilizers = original.content.fertilizers.mapIndexed { index, item ->
            item.copy(doseKgHa = requireNotNull(parseProgramNumber(doses[index])) {
                "Todas las dosis deben ser números finitos mayores o iguales a cero"
            })
        }
        return original.copy(species = species.trim().ifBlank { null }, plot = plot.trim().ifBlank { null },
            content = original.content.copy(name = name.trim(),
                nutrients = ProgramNutrientsDto(values[0], values[1], values[2], values[3], values[4], values[5]),
                fertilizers = fertilizers))
    }
}
