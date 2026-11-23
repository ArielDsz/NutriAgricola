package com.nutriagricola.app.ui.programs

import org.junit.Assert.*
import org.junit.Test

class ProgramWizardViewModelTest {
    private fun readyForMaterials(): ProgramWizardViewModel {
        val vm = ProgramWizardViewModel()
        vm.change { it.copy(name = "Programa de temporada") }
        assertTrue(vm.next())
        assertTrue(vm.next())
        return vm
    }

    @Test fun `five steps validate name and save actual inputs compositions and irrigation stages`() {
        val vm = ProgramWizardViewModel()
        assertFalse(vm.next())
        assertEquals(0, vm.state.value.step)
        vm.change { it.copy(name = " Programa ", plot = "Cuartel 2") }
        repeat(4) { assertTrue(vm.next()) }
        assertEquals(4, vm.state.value.step)
        val request = vm.buildRequest("program")!!
        assertEquals("Programa", request.content.name)
        assertEquals("Uva de mesa", request.species)
        assertEquals("Cuartel 2", request.plot)
        assertEquals(5, request.content.fertilizers.size)
        assertNotNull(request.content.fertilizers.first().composition)
        assertEquals(listOf(35.0, 25.0, 30.0, 10.0), request.content.stages.map { it.allocationPercent })
        assertEquals(22, vm.state.value.totalWeeks)
        assertEquals(82L, vm.state.value.totalIrrigations)
    }

    @Test fun `inverse transfer uses the same integer rounding as the website`() {
        val vm = ProgramWizardViewModel()
        vm.change { it.copy(requirements = listOf("225", "68", "225", "231", "95")) }
        assertTrue(vm.suggest())
        assertEquals(listOf("113", "856", "594", "500", "21"), vm.state.value.fertilizers.map { it.dose })
        assertEquals(225.18, vm.state.value.contributions()!!.nitrogen, 0.00001)
    }

    @Test fun `advisor can replace suggested products with any catalog material and a decimal dose`() {
        val vm = readyForMaterials()
        vm.state.value.fertilizers.forEach { vm.removeMaterial(it.key) }
        assertFalse(vm.next())
        vm.pickMaterial(null, "potassium_sulfate")
        vm.setDose(vm.state.value.fertilizers.single().key, "100,5")
        assertEquals(51.255, vm.state.value.contributions()!!.potassiumOxide, 0.00001)
        assertTrue(vm.next())
        assertTrue(vm.next())
        val saved = vm.buildRequest("program")!!.content.fertilizers.single()
        assertEquals("Sulfato de potasio", saved.name)
        assertEquals(100.5, saved.doseKgHa, 0.0)
        assertEquals(51.0, saved.composition!!.potassiumOxide!!, 0.0)
    }

    @Test fun `going back preserves custom mixture and invalid stage totals cannot be saved`() {
        val vm = readyForMaterials()
        val row = vm.state.value.fertilizers.first()
        vm.setDose(row.key, "12,5")
        assertTrue(vm.previous())
        assertTrue(vm.next())
        assertEquals("12,5", vm.state.value.fertilizers.first().dose)
        assertTrue(vm.next())
        vm.stage(0) { it.copy(percent = "34") }
        assertFalse(vm.next())
        assertEquals(3, vm.state.value.step)
        assertNull(vm.buildRequest("program"))
        vm.stage(0) { it.copy(percent = "35", irrigations = "0", instructions = "Sin riego en esta etapa") }
        assertTrue(vm.next())
        assertEquals(0, vm.buildRequest("program")!!.content.stages.first().irrigationsPerWeek)
    }

    @Test fun `invalid dates efficiencies and fertilizer quantities are rejected without inventing defaults`() {
        val vm = ProgramWizardViewModel()
        vm.change { it.copy(name = "Plan", startDate = "2026-12-01", endDate = "2026-11-01") }
        assertFalse(vm.next())
        vm.change { it.copy(endDate = "") }
        assertTrue(vm.next())
        vm.change { it.copy(efficiency = "0") }
        assertFalse(vm.calculateRequirements())
        assertFalse(vm.next())
        vm.change { it.copy(efficiency = "90") }
        assertTrue(vm.next())
        val row = vm.state.value.fertilizers.first()
        vm.setDose(row.key, "NaN")
        assertFalse(vm.next())
        vm.setDose(row.key, "0")
        assertTrue(vm.next())
    }
}
