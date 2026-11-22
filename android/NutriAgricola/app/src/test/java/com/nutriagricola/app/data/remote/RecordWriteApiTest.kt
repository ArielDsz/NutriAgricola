package com.nutriagricola.app.data.remote

import com.nutriagricola.app.data.local.entity.FoliarPhotoEntity
import com.nutriagricola.app.data.mapper.toCreateRequest
import com.nutriagricola.app.data.mapper.toUpdateRequest
import com.nutriagricola.app.domain.model.record.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class RecordWriteApiTest {
    @Test
    fun createPreservesIdentifiersWithoutSendingAuthorOrStatus(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val api = ApiClient(server.url("/").toString()).recordApi
            val record = Record("record", "crop", "farmer", LocalDate.parse("2026-11-22"),
                RecordType.FOLIAR, RecordStatus.PENDING, "Nota", FoliarData(nitrogen = 0.0))
            val request = record.toCreateRequest(listOf(FoliarPhotoEntity("photo", "record", "file:///photo.png")))
            for (status in listOf(201, 200)) {
                server.enqueue(MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json")
                    .setBody("""{"id":"record","status":"PENDING"}"""))
                assertEquals("record", api.create("Bearer token", request).id)
                val sent = server.takeRequest(5, TimeUnit.SECONDS)!!
                val body = sent.body.readUtf8()
                assertEquals("POST", sent.method)
                assertEquals("Bearer token", sent.getHeader("Authorization"))
                assertEquals(request, Json.decodeFromString<com.nutriagricola.app.data.remote.dto.RecordCreateDto>(body))
                assertFalse(body.contains("userId"))
                assertFalse(body.contains("status"))
                assertEquals("photo", request.foliarPhotos.single().id)
            }
        } finally { server.shutdown() }
    }

    @Test
    fun updateDoesNotSendImmutableRelations(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("""
                {"id":"r","cropId":"c","userId":"u","date":"2026-11-22","type":"SOIL","status":"PENDING"}
            """.trimIndent()))
            val record = Record("r", "c", "u", LocalDate.parse("2026-11-22"), RecordType.SOIL,
                RecordStatus.PENDING, data = SoilData(ph = 0.0))
            ApiClient(server.url("/").toString()).recordApi.update("r", "Bearer token",
                record.toCreateRequest(emptyList()).toUpdateRequest())
            val request = server.takeRequest(5, TimeUnit.SECONDS)!!
            val body = request.body.readUtf8()
            assertEquals("PUT", request.method)
            assertEquals("/api/v1/records/r", request.path)
            assertFalse(body.contains("cropId"))
            assertFalse(body.contains("userId"))
            assertFalse(body.contains("type"))
            assertTrue(body.contains("0.0"))
        } finally { server.shutdown() }
    }

    @Test
    fun uploadsMultipartFileAndDownloadsBinaryWithAuthorization(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val api = ApiClient(server.url("/").toString()).recordApi
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("""
                {"id":"p","path":"file:///photo.png","uploaded":true}
            """.trimIndent()))
            val file = MultipartBody.Part.createFormData("file", "photo.png", "image-bytes".toRequestBody("image/png".toMediaType()))
            assertTrue(api.uploadPhoto("r", "p", "Bearer token", file).uploaded)
            val sent = server.takeRequest(5, TimeUnit.SECONDS)!!
            assertEquals("PUT", sent.method)
            assertEquals("/api/v1/records/r/photos/p", sent.path)
            assertEquals("Bearer token", sent.getHeader("Authorization"))
            assertTrue(sent.getHeader("Content-Type")!!.startsWith("multipart/form-data; boundary="))
            assertTrue(sent.body.readUtf8().contains("name=\"file\""))
            server.enqueue(MockResponse().setHeader("Content-Type", "image/png").setBody("image-bytes"))
            assertEquals("image-bytes", api.downloadPhoto("r", "p", "Bearer token").use { it.string() })
            assertEquals("Bearer token", server.takeRequest(5, TimeUnit.SECONDS)!!.getHeader("Authorization"))
        } finally { server.shutdown() }
    }
}
