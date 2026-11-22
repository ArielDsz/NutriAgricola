package com.nutriagricola.app.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.remote.ApiClient
import com.nutriagricola.app.data.repository.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.util.UUID

// Opt-in check against the local backend. Ordinary tests do not require any services.
@RunWith(AndroidJUnit4::class)
class BackendRecordDownloadTest {
    @Test
    fun downloadsForBothRolesAndKeepsCachedRecordsWhenServiceIsUnavailable(): Unit = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("backendSmoke") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val file = File(context.cacheDir, "backend-test-${UUID.randomUUID()}.preferences_pb")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val store = PreferenceDataStoreFactory.create(scope = scope) { file }
        try {
            val api = ApiClient()
            val users = UserRepository(database.userDao())
            val sessions = SessionRepository(store, users, api.authApi)
            val auth = AuthRepository(api.authApi, sessions)
            val records = RecordRepository(database)
            val farms = FarmRepository(database.farmDao(), api.farmApi, sessions)
            val remote = RecordRemoteRepository(database, api, sessions, farms, records)
            val farmer = requireNotNull(auth.authenticate("farmer@test.cl", "farmer12"))
            val page = remote.downloadPage(farmer.id, 0)
            assertTrue(page.content.isNotEmpty())
            page.content.forEach { assertNotNull(records.getById(it.id)) }
            val ids = records.getByAssignedUser(farmer.id).map { it.id }.toSet()

            // Simulate an unreachable gateway without changing the emulator's connectivity.
            val offlineApi = ApiClient("http://127.0.0.1:1/")
            val offlineSessions = SessionRepository(store, users, offlineApi.authApi)
            assertEquals(farmer.id, offlineSessions.getSavedUser()!!.id)
            val offlineFarms = FarmRepository(database.farmDao(), offlineApi.farmApi, offlineSessions)
            val offlineRemote = RecordRemoteRepository(database, offlineApi, offlineSessions, offlineFarms, records)
            assertTrue(runCatching { offlineRemote.downloadPage(farmer.id, 0) }.exceptionOrNull() is IOException)
            assertEquals(ids, records.getByAssignedUser(farmer.id).map { it.id }.toSet())

            val advisor = requireNotNull(auth.authenticate("advisor@test.cl", "advisor1"))
            val advisorPage = remote.downloadPage(advisor.id, 0)
            assertTrue(advisorPage.content.isNotEmpty())
            assertTrue(records.getByAssignedUser(advisor.id).isNotEmpty())
            assertTrue(database.userDao().getById(advisor.id)!!.active)
        } finally {
            scope.cancel()
            scope.coroutineContext[Job]?.join()
            database.close()
            file.delete()
        }
    }
}
