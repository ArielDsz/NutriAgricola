package com.nutriagricola.app.data.remote

import com.nutriagricola.app.data.remote.dto.ReviewCreateRequestDto
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class ReviewApiTest {
    @Test
    fun submitsReviewWithoutClientSuppliedAdvisorAndSupportsSameIdRetry(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val api = ApiClient(server.url("/").toString()).reviewApi
            val request = ReviewCreateRequestDto("review", "record", "2026-11-22T12:00:00", "OBSERVED", "Revisar muestra")
            val body = """{"id":"review","recordId":"record","advisorId":"advisor","advisorName":"Asesor",
                "date":"2026-11-22T12:00:00","result":"OBSERVED","comment":"Revisar muestra","recordStatus":"PENDING"}"""
            for (status in listOf(201, 200)) {
                server.enqueue(MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body))
                val response = api.create("Bearer token", request)
                val sent = requireNotNull(server.takeRequest(5, TimeUnit.SECONDS))
                assertEquals("POST", sent.method)
                assertEquals("/api/v1/reviews", sent.path)
                assertEquals("Bearer token", sent.getHeader("Authorization"))
                val sentBody = sent.body.readUtf8()
                assertEquals(request, Json.decodeFromString<ReviewCreateRequestDto>(sentBody))
                assertFalse(sentBody.contains("advisorId"))
                assertEquals("PENDING", response.recordStatus)
                assertEquals("Asesor", response.advisorName)
            }
        } finally { server.shutdown() }
    }

    @Test
    fun rejectedReviewPropagatesServerError() {
        val server = MockWebServer()
        server.start()
        try {
            for (status in listOf(401, 403, 409, 503)) {
                server.enqueue(MockResponse().setResponseCode(status))
                val error = assertThrows(HttpException::class.java) {
                    runBlocking {
                        ApiClient(server.url("/").toString()).reviewApi.create("Bearer token",
                            ReviewCreateRequestDto("r", "record", "2026-11-22T12:00:00", "VALIDATED", ""))
                    }
                }
                assertEquals(status, error.code())
            }
        } finally { server.shutdown() }
    }
}
