package com.nutriagricola.app.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class FarmApiTest {
    @Test
    fun readsAssignedFarmsAndSendsBearerToken(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("""
                [{"id":"33333333-3333-4333-8333-333333333333","name":"Predio prueba","location":null,"imagePath":null,"active":true}]
            """.trimIndent()))
            val farms = ApiClient(server.url("/").toString()).farmApi.getFarms("Bearer test-token")
            val request = requireNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("GET", request.method)
            assertEquals("/api/v1/farms", request.path)
            assertEquals("Bearer test-token", request.getHeader("Authorization"))
            assertEquals("Predio prueba", farms.single().name)
            assertNull(farms.single().location)
            assertTrue(farms.single().active)

            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("[]"))
            assertTrue(ApiClient(server.url("/").toString()).farmApi.getFarms("Bearer test-token").isEmpty())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun expiredSessionPropagates401() {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(401))
            val error = assertThrows(HttpException::class.java) {
                runBlocking { ApiClient(server.url("/").toString()).farmApi.getFarms("Bearer expired") }
            }
            assertEquals(401, error.code())
        } finally {
            server.shutdown()
        }
    }
}
