package com.nutriagricola.app.ui.programs

import com.nutriagricola.app.domain.model.program.NutritionalProgram

enum class ProgramStatusFilter(val label: String) { ALL("Todos"), ACTIVE("Activos"), ARCHIVED("Archivados") }

fun filterPrograms(programs: List<NutritionalProgram>, query: String, status: ProgramStatusFilter): List<NutritionalProgram> {
    val search = query.trim()
    return programs.filter { program ->
        val matchesStatus = when (status) {
            ProgramStatusFilter.ALL -> true
            ProgramStatusFilter.ACTIVE -> program.active
            ProgramStatusFilter.ARCHIVED -> !program.active
        }
        matchesStatus && (search.isEmpty() || listOfNotNull(program.name, program.species, program.advisorName)
            .any { it.contains(search, ignoreCase = true) })
    }
}
