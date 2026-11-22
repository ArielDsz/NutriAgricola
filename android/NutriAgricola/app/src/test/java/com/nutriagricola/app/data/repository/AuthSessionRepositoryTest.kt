package com.nutriagricola.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nutriagricola.app.data.local.dao.UserDao
import com.nutriagricola.app.data.local.entity.UserEntity
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.remote.InvalidUserProfileException
import com.nutriagricola.app.data.remote.api.AuthApi
import com.nutriagricola.app.data.remote.dto.AuthResponseDto
import com.nutriagricola.app.data.remote.dto.LoginRequestDto
import com.nutriagricola.app.data.remote.dto.UserResponseDto
import com.nutriagricola.app.domain.model.user.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AuthSessionRepositoryTest {
    private lateinit var store: DataStore<Preferences>
    private lateinit var users: UserRepository
    private lateinit var api: FakeAuthApi
    private lateinit var sessions: SessionRepository
    private lateinit var auth: AuthRepository

    @Before
    fun setUp() {
        store = InMemoryPreferencesStore()
        users = UserRepository(FakeUserDao())
        api = FakeAuthApi()
        sessions = SessionRepository(store, users, api)
        auth = AuthRepository(api, sessions)
    }

    @Test
    fun onlineLoginNormalizesEmailCachesBackendUserAndSavesToken() = runBlocking {
        val user = auth.authenticate(" FARMER@TEST.CL ", " secret ")

        assertEquals(LoginRequestDto("farmer@test.cl", " secret "), api.lastLogin)
        assertEquals(profile.id, user?.id)
        assertEquals(user, users.getById(profile.id))
        assertEquals("server-token", sessions.getAccessToken())
        assertTrue(sessions.hasSession(profile.id))
    }

    @Test
    fun rejectedCredentialsNeverAuthenticateFromLocalProfile() = runBlocking {
        users.upsert(profile.toDomain())
        api.loginFailure = httpError(401)

        assertNull(auth.authenticate("farmer@test.cl", "wrong"))
        assertNull(sessions.getAccessToken())
        assertFalse(sessions.hasSession(profile.id))
    }

    @Test
    fun savedSessionWorksOfflineButLogoutRequiresOnlineLoginAgain(): Unit = runBlocking {
        val user = auth.authenticate("farmer@test.cl", "secret")
        api.profileFailure = IOException("Sin conexión")
        assertEquals(user, sessions.getSavedUser())

        sessions.clearSession()
        assertNull(sessions.getAccessToken())
        assertNull(sessions.getSavedUser())
        assertEquals(user, users.getById(profile.id))
        api.loginFailure = IOException("Sin conexión")
        assertThrows(IOException::class.java) {
            runBlocking { auth.authenticate("farmer@test.cl", "secret") }
        }
    }

    @Test
    fun restoringOnlineRefreshesProfileAndUsesBearerToken() = runBlocking {
        auth.authenticate("farmer@test.cl", "secret")
        api.remoteProfile = profile.copy(firstName = "Actualizado", role = "ADVISOR")

        val restored = sessions.getSavedUser()

        assertEquals("Bearer server-token", api.lastAuthorization)
        assertEquals("Actualizado", restored?.firstName)
        assertEquals(UserRole.ADVISOR, restored?.role)
        assertEquals(restored, users.getById(profile.id))
    }

    @Test
    fun invalidOrExpiredSessionIsClearedWithoutDeletingCachedUser() = runBlocking {
        for (code in listOf(401, 403)) {
            auth.authenticate("farmer@test.cl", "secret")
            api.profileFailure = httpError(code)
            assertNull(sessions.getSavedUser())
            assertNull(sessions.getAccessToken())
            assertNotNull(users.getById(profile.id))
        }
    }

    @Test
    fun rejectsUnsupportedInactiveOrMalformedProfilesAndBlankTokens() = runBlocking {
        for (invalid in listOf(profile.copy(role = "ADMIN"), profile.copy(active = false), profile.copy(id = "incorrecto"))) {
            api.loginResponse = AuthResponseDto("server-token", invalid)
            assertThrows(InvalidUserProfileException::class.java) {
                runBlocking { auth.authenticate("farmer@test.cl", "secret") }
            }
            assertNull(sessions.getAccessToken())
        }
        api.loginResponse = AuthResponseDto("", profile)
        assertThrows(InvalidUserProfileException::class.java) {
            runBlocking { auth.authenticate("farmer@test.cl", "secret") }
        }
        assertNull(sessions.getAccessToken())
    }

    @Test
    fun temporaryServerFailureKeepsSessionAndCancellationPropagates() = runBlocking {
        val user = auth.authenticate("farmer@test.cl", "secret")
        api.profileFailure = httpError(503)
        assertEquals(user, sessions.getSavedUser())
        api.profileFailure = CancellationException("Cancelado")
        assertThrows(CancellationException::class.java) {
            runBlocking { sessions.getSavedUser() }
        }
        assertEquals("server-token", sessions.getAccessToken())
    }

    @Test
    fun legacySessionWithoutTokenAndMismatchedRemoteUserRequireNewLogin() = runBlocking {
        users.upsert(profile.toDomain())
        store.edit { it[stringPreferencesKey("userId")] = profile.id }
        assertNull(sessions.getSavedUser())
        assertEquals(0, api.profileCalls)

        auth.authenticate("farmer@test.cl", "secret")
        api.remoteProfile = profile.copy(id = "22222222-2222-4222-8222-222222222222")
        assertNull(sessions.getSavedUser())
        assertNull(sessions.getAccessToken())
    }

    private inner class FakeAuthApi : AuthApi {
        var loginResponse = AuthResponseDto("server-token", profile)
        var remoteProfile = profile
        var loginFailure: Exception? = null
        var profileFailure: Exception? = null
        var lastLogin: LoginRequestDto? = null
        var lastAuthorization: String? = null
        var profileCalls = 0

        override suspend fun login(request: LoginRequestDto): AuthResponseDto {
            lastLogin = request
            loginFailure?.let { throw it }
            return loginResponse
        }
        override suspend fun getCurrentUser(authorization: String): UserResponseDto {
            profileCalls++
            lastAuthorization = authorization
            profileFailure?.let { throw it }
            return remoteProfile
        }
    }

    private class InMemoryPreferencesStore : DataStore<Preferences> {
        override val data = MutableStateFlow(emptyPreferences())
        private val mutex = Mutex()

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            mutex.withLock {
                transform(data.value).also { data.value = it }
            }
    }

    private class FakeUserDao : UserDao {
        private val rows = mutableMapOf<String, UserEntity>()
        override suspend fun insert(user: UserEntity) { check(user.id !in rows); rows[user.id] = user }
        override suspend fun upsert(user: UserEntity) { rows[user.id] = user }
        override suspend fun getAll() = rows.values.toList()
        override suspend fun getById(id: String) = rows[id]
        override suspend fun getByEmail(email: String) = rows.values.firstOrNull { it.email == email }
        override suspend fun update(user: UserEntity) { if (user.id in rows) rows[user.id] = user }
        override suspend fun delete(user: UserEntity) { rows.remove(user.id) }
    }

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "{}".toResponseBody()))

    private val profile = UserResponseDto(
        id = "11111111-1111-4111-8111-111111111111", run = "11111111-1", firstName = "Agricultor",
        lastName = "Prueba", email = "farmer@test.cl", role = "FARMER", active = true,
        birthDate = "2000-01-01", createdAt = "2026-11-19T12:00:00"
    )
}
