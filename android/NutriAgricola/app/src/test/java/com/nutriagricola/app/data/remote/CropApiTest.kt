package com.nutriagricola.app.data.remote

import com.nutriagricola.app.data.mapper.toEntity
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class CropApiTest {
    @Test
    fun readsCropsForSelectedFarmAndPreservesOptionalFields(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val api = ApiClient(server.url("/").toString()).cropApi
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("""
                [{"id":"crop","farmId":"farm","name":"Palto","variety":null,
                  "areaHectares":0.0,"phenologicalStage":"Floración","imagePath":null,"active":true}]
            """.trimIndent()))
            val crop = api.getByFarm("farm", "Bearer test-token").single().toEntity()
            val request = requireNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("GET", request.method)
            assertEquals("/api/v1/crops/farm/farm", request.path)
            assertEquals("Bearer test-token", request.getHeader("Authorization"))
            assertEquals("farm", crop.farmId)
            assertEquals("Floración", crop.phenologicalStage)
            assertEquals(0.0, crop.areaHectares!!, 0.0)
            assertNull(crop.variety)
            assertNull(crop.imagePath)

            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("[]"))
            assertTrue(api.getByFarm("farm", "Bearer test-token").isEmpty())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun accessDeniedDoesNotReturnAnEmptySuccessfulResponse() {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(403))
            val error = assertThrows(HttpException::class.java) {
                runBlocking { ApiClient(server.url("/").toString()).cropApi.getByFarm("farm", "Bearer token") }
            }
            assertEquals(403, error.code())
        } finally {
            server.shutdown()
        }
    }
}
