package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.dao.ProgramDao
import com.nutriagricola.app.data.local.entity.ProgramEntity
import com.nutriagricola.app.data.remote.ApiClient
import com.nutriagricola.app.data.remote.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class ProgramRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var dao: MemoryProgramDao
    private lateinit var repository: ProgramRepository
    private val json = Json { ignoreUnknownKeys = true }
    private val composition = ProgramNutrientsDto(12.0, 60.0, 0.0, 0.0, 0.0, 0.0)
    private val content = ProgramContentDto("Prueba", "2026-11-22", ProgramNutrientsDto(phosphorusOxide = 60.0),
        listOf(ProgramFertilizerDto("MAP", 100.0, composition = composition)), emptyList())
    private val detail = ProgramResponseDto("program", "advisor", "Asesor Prueba", content, true, "2026-11-22T12:00:00")

    @Before fun setup() {
        server = MockWebServer(); server.start()
        dao = MemoryProgramDao()
        repository = ProgramRepository(dao, ApiClient(server.url("/").toString()).programApi) { "Bearer demo" }
    }
    @After fun close() { server.shutdown() }
    private fun respond(body: String, status: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body))
    }

    @Test fun `create sends composition and keeps confirmed detail available without more requests`() = runBlocking {
        respond(json.encodeToString(detail), 201)
        val request = ProgramRequestDto("program", "Uva", null, content)
        repository.create("advisor", request)
        val sent = server.takeRequest()
        assertEquals("POST", sent.method)
        assertEquals("Bearer demo", sent.getHeader("Authorization"))
        assertEquals(request, json.decodeFromString<ProgramRequestDto>(sent.body.readUtf8()))
        val cached = repository.observePrograms().first().single()
        assertTrue(cached.detailAvailable)
        assertEquals(12.0, cached.contributions()!!.nitrogen, 0.00001)
        assertEquals(60.0, cached.contributions()!!.phosphorusOxide, 0.00001)
        assertEquals(1, server.requestCount)
    }

    @Test fun `refresh keeps downloaded details and does not fetch them with the list`() = runBlocking {
        respond(json.encodeToString(detail))
        repository.downloadDetail("farmer", "program")
        val summary = ProgramSummaryDto("program", "advisor", "Prueba", "2026-11-22", true)
        respond(json.encodeToString(ProgramPageDto(listOf(summary), 0, 20, 21, 2)))
        assertTrue(repository.downloadPage("farmer", 0))
        val cached = repository.observePrograms().first().single()
        assertTrue(cached.detailAvailable)
        assertEquals("Asesor Prueba", cached.advisorName)
        assertNull(cached.requirements[0]) // Un dato ausente no se convierte en cero.
        assertEquals(2, server.requestCount)
        server.takeRequest()
        assertEquals("/api/v1/programs?page=0&size=20", server.takeRequest().path)
    }

    @Test fun `rejected writes keep existing cache and old manual programs do not invent composition`() = runBlocking {
        val manual = detail.copy(content = content.copy(fertilizers = listOf(ProgramFertilizerDto("Manual", 10.0))))
        respond(json.encodeToString(manual))
        repository.downloadDetail("farmer", "program")
        respond("{}", 403)
        try {
            repository.create("farmer", ProgramRequestDto("other", null, null, content))
            fail("El servidor debe rechazar la escritura")
        } catch (expected: HttpException) { assertEquals(403, expected.code()) }
        assertEquals(1, repository.observePrograms().first().size)
        assertNull(repository.observePrograms().first().single().contributions())
    }

    private class MemoryProgramDao : ProgramDao {
        private val rows = MutableStateFlow<List<ProgramEntity>>(emptyList())
        override fun observeAll() = rows
        override suspend fun insertSummary(row: ProgramEntity) { if (rows.value.none { it.id == row.id }) rows.value += row }
        override suspend fun updateSummary(id: String, startDate: String, json: String) {
            rows.value = rows.value.map { if (it.id == id) it.copy(startDate = startDate, summaryJson = json) else it }
        }
        override suspend fun cacheDetail(row: ProgramEntity) { rows.value = rows.value.filterNot { it.id == row.id } + row }
    }
}
