package com.nutriagricola.app.data

import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.testing.SynchronousExecutor
import com.nutriagricola.app.data.sync.RecordUploadWorker
import com.nutriagricola.app.data.sync.RecordUploadScheduler
import kotlinx.coroutines.flow.filterNotNull
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.entity.CropEntity
import com.nutriagricola.app.data.local.entity.FarmEntity
import com.nutriagricola.app.data.remote.ApiClient
import com.nutriagricola.app.data.remote.PhotoFileTransfer
import com.nutriagricola.app.data.remote.dto.*
import com.nutriagricola.app.data.repository.*
import com.nutriagricola.app.domain.model.record.*
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.domain.model.user.UserRole
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import retrofit2.HttpException
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RecordUploadIntegrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: AppDatabase
    private lateinit var server: MockWebServer
    private lateinit var scope: CoroutineScope
    private lateinit var preferences: File
    private lateinit var folder: File
    private lateinit var records: RecordRepository
    private lateinit var sessions: SessionRepository
    private lateinit var api: ApiClient
    private lateinit var uploader: RecordUploadRepository
    private lateinit var files: PhotoFileTransfer
    private lateinit var record: Record
    private val farmerId = "11111111-1111-4111-8111-111111111111"
    private val cropId = "44444444-4444-4444-8444-444444444444"
    private lateinit var image: ByteArray
    private var workTestingInitialized = false

    private fun workerFactory() = object : WorkerFactory() {
        override fun createWorker(appContext: android.content.Context, workerClassName: String,
                                  workerParameters: WorkerParameters): ListenableWorker? =
            if (workerClassName == RecordUploadWorker::class.java.name)
                RecordUploadWorker(appContext, workerParameters, uploader) else null
    }

    @Before
    fun setUp(): Unit = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        server = MockWebServer().also { it.start() }
        api = ApiClient(server.url("/").toString())
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        preferences = File(context.cacheDir, "upload-test-${UUID.randomUUID()}.preferences_pb")
        val store = PreferenceDataStoreFactory.create(scope = scope) { preferences }
        val users = UserRepository(db.userDao())
        sessions = SessionRepository(store, users, api.authApi)
        sessions.saveSession(User(farmerId, "Ana", "Prueba", "farmer@example.test", UserRole.FARMER, true), "test-token")
        db.farmDao().insert(FarmEntity("farm", "Predio", null, true))
        db.farmDao().replaceAssignedFarms(farmerId, db.farmDao().getAll())
        db.cropDao().insert(CropEntity(cropId, "farm", "Palto", null, null, true))
        records = RecordRepository(db)
        val id = UUID.randomUUID().toString()
        folder = File(context.filesDir, "foliar_photos/$id").also { check(it.mkdirs()) }
        image = java.io.ByteArrayOutputStream().use { output ->
            val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            bitmap.recycle()
            output.toByteArray()
        }
        val file = File(folder, "test.png").also { it.writeBytes(image) }
        record = Record(id, cropId, farmerId, type = RecordType.FOLIAR, status = RecordStatus.PENDING,
            data = FoliarData(nitrogen = 0.0, photos = listOf(file.toUri().toString())))
        records.insert(record)
        files = PhotoFileTransfer(context)
        uploader = RecordUploadRepository(db, api, sessions, records, files)
    }

    @After
    fun tearDown(): Unit = runBlocking {
        if (workTestingInitialized) WorkManagerTestInitHelper.closeWorkDatabase()
        scope.cancel()
        scope.coroutineContext[Job]?.join()
        server.shutdown()
        db.close()
        preferences.delete()
        folder.listFiles()?.forEach { it.delete() }
        folder.delete()
    }

    private fun json(body: String, status: Int = 200) = MockResponse().setResponseCode(status)
        .setHeader("Content-Type", "application/json").setBody(body)

    private fun detail(photoId: String, uploaded: Boolean) = """
        {"id":"${record.id}","cropId":"$cropId","userId":"$farmerId","date":"${record.date}",
        "type":"FOLIAR","status":"PENDING","foliarData":{"nitrogen":0.0},
        "foliarPhotos":[{"id":"$photoId","path":"${(record.data as FoliarData).photos.single()}","uploaded":$uploaded}],"reviews":[]}
    """.trimIndent()

    @Test
    fun failedPhotoUploadRemainsPendingAndRetryReusesRecordAndPhotoIds(): Unit = runBlocking {
        val photo = records.getPhotos(record.id).single()
        server.enqueue(json("""{"id":"${record.id}","status":"PENDING"}""", 201))
        server.enqueue(json(detail(photo.id, false)))
        server.enqueue(MockResponse().setResponseCode(503))
        assertTrue(runCatching { uploader.uploadPending(farmerId) }.exceptionOrNull() is HttpException)
        val firstCreate = server.takeRequest().body.readUtf8()
        server.takeRequest()
        val firstPhoto = server.takeRequest()
        assertEquals(1, records.observePendingUploadCount(farmerId).first())
        assertNull(records.getSyncState(record.id))
        assertTrue(files.isAvailable(record.id, photo.path))

        server.enqueue(json("""{"id":"${record.id}","status":"PENDING"}"""))
        server.enqueue(json(detail(photo.id, false)))
        server.enqueue(json("""{"id":"${photo.id}","path":"unused","uploaded":true}"""))
        server.enqueue(json(detail(photo.id, true)))
        assertEquals(1, uploader.uploadPending(farmerId))
        assertEquals(firstCreate, server.takeRequest().body.readUtf8())
        server.takeRequest()
        val secondPhoto = server.takeRequest()
        assertEquals(firstPhoto.path, secondPhoto.path)
        assertTrue(secondPhoto.body.readUtf8().contains("name=\"file\""))
        server.takeRequest()
        assertEquals(0, records.observePendingUploadCount(farmerId).first())
        assertEquals(photo.id, records.getPhotos(record.id).single().id)
        assertTrue(files.isAvailable(record.id, photo.path))
    }

    @Test
    fun downloadsPhotoToOwnedStorageAndReusesCachedFile(): Unit = runBlocking {
        val id = UUID.randomUUID().toString()
        val photoId = UUID.randomUUID().toString()
        val dto = RecordDetailDto(id, cropId, farmerId, record.date.toString(), "FOLIAR", "PENDING",
            foliarData = FoliarDataDto(nitrogen = 0.0),
            foliarPhotos = listOf(PhotoResponseDto(photoId, "/another/device.jpg", true)))
        records.cacheRemoteRecord(dto)
        server.enqueue(MockResponse().setHeader("Content-Type", "image/png").setBody(Buffer().write(image)))
        val photos = RecordPhotoRepository(db, api, sessions, files, records)
        try {
            val downloaded = photos.download(id, farmerId)
            val path = (downloaded.data as FoliarData).photos.single()
            assertTrue(files.isAvailable(id, path))
            assertArrayEquals(image, File(path.toUri().path!!).readBytes())
            val photoRequest = server.takeRequest()
            assertEquals("Bearer test-token", photoRequest.getHeader("Authorization"))
            assertEquals("/api/v1/records/$id/photos/$photoId", photoRequest.path)
            photos.download(id, farmerId)
            assertEquals(1, server.requestCount)
        } finally {
            val downloadedFolder = File(context.filesDir, "foliar_photos/$id")
            downloadedFolder.listFiles()?.forEach { it.delete() }
            downloadedFolder.delete()
        }
    }

    @Test
    fun missingSessionDoesNotUploadAnotherAccountsPendingRecords(): Unit = runBlocking {
        sessions.clearSession()
        assertTrue(runCatching { uploader.uploadPending(farmerId) }.exceptionOrNull() is UploadSessionRequiredException)
        assertEquals(0, server.requestCount)
        assertEquals(1, records.observePendingUploadCount(farmerId).first())
    }

    @Test
    fun conflictingRecordDoesNotPreventAnotherPendingRecordFromBeingSent(): Unit = runBlocking {
        val other = record.copy(id = UUID.randomUUID().toString(), date = record.date.plusDays(1),
            type = RecordType.SOIL, data = SoilData(ph = 6.0))
        records.insert(other)
        server.enqueue(MockResponse().setResponseCode(409))
        server.enqueue(json("""{"id":"${other.id}","status":"PENDING"}""", 201))
        server.enqueue(json("""{"id":"${other.id}","cropId":"$cropId","userId":"$farmerId",
            "date":"${other.date}","type":"SOIL","status":"PENDING","soilData":{"ph":6.0},"foliarPhotos":[],"reviews":[]}"""))
        assertTrue(runCatching { uploader.uploadPending(farmerId) }.exceptionOrNull() is HttpException)
        assertEquals(1, records.observePendingUploadCount(farmerId).first())
        assertNull(records.getSyncState(record.id))
        assertFalse(records.getSyncState(other.id)!!.dirty)
    }

    @Test
    fun workerRetriesTemporaryServerFailureWithoutClearingPending(): Unit = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503))
        val worker = TestListenableWorkerBuilder<RecordUploadWorker>(context)
            .setWorkerFactory(workerFactory()).setInputData(workDataOf("userId" to farmerId)).build()
        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
        assertEquals(1, records.observePendingUploadCount(farmerId).first())
    }

    @Test
    fun workerStopsForMissingSessionAndRetainsPending(): Unit = runBlocking {
        sessions.clearSession()
        val worker = TestListenableWorkerBuilder<RecordUploadWorker>(context)
            .setWorkerFactory(workerFactory()).setInputData(workDataOf("userId" to farmerId)).build()
        val result = worker.doWork()
        assertTrue(result is ListenableWorker.Result.Failure)
        assertTrue((result as ListenableWorker.Result.Failure).outputData.getString("message")!!.contains("sesión"))
        assertEquals(1, records.observePendingUploadCount(farmerId).first())
        assertEquals(0, server.requestCount)
    }

    @Test
    fun queuedUploadWaitsForNetworkConstraintAndSendsWhenItIsMet(): Unit = runBlocking {
        val configuration = Configuration.Builder().setExecutor(SynchronousExecutor())
            .setWorkerFactory(workerFactory()).build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
        workTestingInitialized = true
        val manager = WorkManager.getInstance(context)
        val request = RecordUploadScheduler.request(farmerId)
        assertEquals(NetworkType.CONNECTED, request.workSpec.constraints.requiredNetworkType)
        assertEquals(setOf("userId"), request.workSpec.input.keyValueMap.keys)
        val photo = records.getPhotos(record.id).single()
        server.enqueue(json("""{"id":"${record.id}","status":"PENDING"}""", 201))
        server.enqueue(json(detail(photo.id, false)))
        server.enqueue(json("""{"id":"${photo.id}","path":"unused","uploaded":true}"""))
        server.enqueue(json(detail(photo.id, true)))
        manager.enqueue(request).result.get()
        assertEquals(0, server.requestCount)
        WorkManagerTestInitHelper.getTestDriver(context)!!.setAllConstraintsMet(request.id)
        val completed = withTimeout(10_000) {
            manager.getWorkInfoByIdFlow(request.id).filterNotNull().first { it.state.isFinished }
        }
        assertEquals(WorkInfo.State.SUCCEEDED, completed.state)
        assertEquals(1, completed.outputData.getInt("uploaded", -1))
        assertEquals(0, records.observePendingUploadCount(farmerId).first())
        assertTrue(files.isAvailable(record.id, photo.path))
    }
}
