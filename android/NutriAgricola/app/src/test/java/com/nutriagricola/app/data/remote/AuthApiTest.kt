package com.nutriagricola.app.data.remote

import com.nutriagricola.app.data.remote.dto.LoginRequestDto
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class AuthApiTest {
    private lateinit var server: MockWebServer
    private lateinit var client: ApiClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = ApiClient(server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun loginSendsJsonAndReadsTokenAndUser() = runBlocking {
        server.enqueue(jsonResponse("""{"accessToken":"test-token","user":$userJson}"""))

        val credentials = LoginRequestDto("farmer@test.cl", "farmer12")
        val response = client.authApi.login(credentials)
        val request = requireNotNull(server.takeRequest(5, TimeUnit.SECONDS))

        assertEquals("POST", request.method)
        assertEquals("/api/v1/auth/login", request.path)
        assertEquals(credentials, Json.decodeFromString<LoginRequestDto>(request.body.readUtf8()))
        assertEquals("test-token", response.accessToken)
        assertEquals("11111111-1111-4111-8111-111111111111", response.user.id)
        assertEquals("FARMER", response.user.role)
        assertEquals(null, response.user.phoneNumber)
    }

    @Test
    fun profileSendsBearerToken() = runBlocking {
        server.enqueue(jsonResponse(userJson))

        val user = client.authApi.getCurrentUser("Bearer test-token")
        val request = requireNotNull(server.takeRequest(5, TimeUnit.SECONDS))

        assertEquals("GET", request.method)
        assertEquals("/api/v1/users/me", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
        assertEquals("farmer@test.cl", user.email)
    }

    @Test
    fun rejectedLoginPropagatesHttp401() {
        server.enqueue(jsonResponse("""{"message":"Credenciales incorrectas"}""").setResponseCode(401))

        val error = assertThrows(HttpException::class.java) {
            runBlocking { client.authApi.login(LoginRequestDto("farmer@test.cl", "wrong")) }
        }
        assertEquals(401, error.code())
    }

    private fun jsonResponse(body: String) = MockResponse()
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private val userJson = """
        {"id":"11111111-1111-4111-8111-111111111111","run":"11111111-1",
         "firstName":"Agricultor","lastName":"Prueba","email":"farmer@test.cl",
         "role":"FARMER","active":true,"birthDate":"2000-01-01",
         "createdAt":"2026-11-19T12:00:00","extraField":"compatible"}
    """.trimIndent()
}
