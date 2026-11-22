package com.nutriagricola.app.data.remote

import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.remote.dto.RecordDetailDto
import com.nutriagricola.app.domain.model.record.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class RecordApiTest {
    @Test
    fun historyUsesPaginationAndBearerToken(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("""
                {"content":[{"id":"record","cropId":"crop","userId":"farmer","date":"2026-11-20",
                "type":"SOIL","status":"PENDING","observations":null}],
                "page":1,"size":20,"totalElements":25,"totalPages":2}
            """.trimIndent()))
            val page = ApiClient(server.url("/").toString()).recordApi.getHistory("Bearer token", 1)
            val request = requireNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("/api/v1/records?page=1&size=20", request.path)
            assertEquals("Bearer token", request.getHeader("Authorization"))
            assertEquals(2, page.totalPages)
            assertEquals(25L, page.totalElements)
            assertNull(page.content.single().observations)
        } finally { server.shutdown() }
    }

    @Test
    fun detailReadsMeasurementsReviewsAndRemotePhotoMetadata(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("""
                {"id":"record","cropId":"crop","userId":"farmer","date":"2026-11-20",
                 "type":"FOLIAR","status":"PENDING","foliarData":{"nitrogen":0.0,"symptoms":"Síntoma"},
                 "foliarPhotos":[{"id":"photo","path":"/another/device.jpg","uploaded":true,
                 "downloadUrl":"/api/v1/records/record/photos/photo"}],
                 "reviews":[{"id":"review","advisorId":"advisor","advisorName":"Asesor",
                 "date":"2026-11-20T12:00:00","result":"OBSERVED","comment":"Revisar"}]}
            """.trimIndent()))
            val dto = ApiClient(server.url("/").toString()).recordApi.getDetail("record", "Bearer token")
            val record = dto.toDomain()
            assertEquals("/api/v1/records/record", server.takeRequest(5, TimeUnit.SECONDS)!!.path)
            assertEquals(0.0, (record.data as FoliarData).nitrogen!!, 0.0)
            assertTrue(record.data.photos.isEmpty())
            assertEquals("OBSERVED", dto.reviews.single().result)
            assertTrue(dto.foliarPhotos.single().uploaded)
        } finally { server.shutdown() }
    }

    @Test
    fun mapsSoilAndWaterWithoutReplacingMissingValues() {
        val json = Json { ignoreUnknownKeys = true }
        val prefix = """"id":"r","cropId":"c","userId":"u","date":"2026-11-20","status":"PENDING","""
        val soil = json.decodeFromString<RecordDetailDto>("{" + prefix +
            """"type":"SOIL","soilData":{"ph":0.0,"organicMatter":null}}""").toDomain().data as SoilData
        assertEquals(0.0, soil.ph!!, 0.0)
        assertNull(soil.organicMatter)
        val water = json.decodeFromString<RecordDetailDto>("{" + prefix +
            """"type":"WATER","waterData":{"waterSource":"Pozo","ph":null}}""").toDomain().data as WaterData
        assertEquals("Pozo", water.waterSource)
        assertNull(water.ph)
    }

    @Test
    fun unauthorizedRequestPropagates401() {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(401))
            val error = assertThrows(HttpException::class.java) {
                runBlocking { ApiClient(server.url("/").toString()).recordApi.getHistory("Bearer expired", 0) }
            }
            assertEquals(401, error.code())
        } finally { server.shutdown() }
    }
}
