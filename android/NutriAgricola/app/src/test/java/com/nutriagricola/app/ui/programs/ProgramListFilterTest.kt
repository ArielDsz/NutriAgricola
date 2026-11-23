package com.nutriagricola.app.ui.programs

import com.nutriagricola.app.domain.model.program.NutritionalProgram
import org.junit.Assert.*
import org.junit.Test

class ProgramListFilterTest {
    @Test fun `search combines names species advisors and archive status without requiring details`() {
        val first = NutritionalProgram("1", "Primavera", "advisor", "Ana Pérez", "Uva", null, null,
            "2026-11-01", null, true, false)
        val second = first.copy(id = "2", name = "Invierno", species = "Palto", advisorName = "Luis Díaz", active = false)
        val programs = listOf(first, second)
        assertEquals(listOf(first), filterPrograms(programs, "  UVA ", ProgramStatusFilter.ALL))
        assertEquals(listOf(second), filterPrograms(programs, "luis", ProgramStatusFilter.ARCHIVED))
        assertEquals(listOf(first), filterPrograms(programs, "primavera", ProgramStatusFilter.ACTIVE))
        assertTrue(filterPrograms(programs, "palto", ProgramStatusFilter.ACTIVE).isEmpty())
        assertEquals(programs, filterPrograms(programs, " ", ProgramStatusFilter.ALL))
    }
}
