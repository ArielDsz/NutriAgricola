package com.nutriagricola.app.ui.programs

import com.nutriagricola.app.data.remote.dto.*
import org.junit.Assert.*
import org.junit.Test

class ProgramEditDraftTest {
    private val source = ProgramRequestDto("program", "Uva", "Cuartel 1", ProgramContentDto(
        name = "Original", startDate = "2026-11-01", endDate = "2027-01-01", season = "2026-2027",
        nutrients = ProgramNutrientsDto(nitrogen = 0.0), targetYieldTonHa = 30.0,
        irrigationEfficiencyPercent = 90.0, irrigationNotes = "Conservar indicaciones",
        fertilizers = listOf(ProgramFertilizerDto("Producto manual sin composición", 10.0, "Conservar nota")),
        stages = listOf(ProgramStageDto("Brotación", 4, 0, "Conservar etapa", 100.0))
    ))

    @Test fun `editing preserves legacy products missing composition and untouched program fields`() {
        val request = ProgramEditDraft(source).copy(name = " Editado ", doses = listOf("15,5")).buildRequest()
        assertEquals("Editado", request.content.name)
        assertEquals(15.5, request.content.fertilizers.single().doseKgHa, 0.0)
        assertEquals(source.content.fertilizers.single().copy(doseKgHa = 15.5), request.content.fertilizers.single())
        assertEquals(source.content, request.content.copy(name = source.content.name, fertilizers = source.content.fertilizers))
        assertNull(request.content.nutrients.phosphorusOxide)
        assertEquals(0.0, request.content.nutrients.nitrogen!!, 0.0)
    }

    @Test fun `editing accepts zero but rejects invalid doses and requirements`() {
        assertEquals(0.0, ProgramEditDraft(source).copy(doses = listOf("0")).buildRequest().content.fertilizers.single().doseKgHa, 0.0)
        listOf("", "-1", "NaN", "Infinity", "1e999").forEach { text ->
            assertThrows(IllegalArgumentException::class.java) { ProgramEditDraft(source).copy(doses = listOf(text)).buildRequest() }
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProgramEditDraft(source).copy(requirements = listOf("-1", "", "", "", "", "")).buildRequest()
        }
    }
}
