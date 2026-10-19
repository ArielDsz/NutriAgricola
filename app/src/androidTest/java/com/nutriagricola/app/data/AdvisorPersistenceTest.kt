package com.nutriagricola.app.data

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.database.MIGRATION_1_2
import com.nutriagricola.app.data.local.database.MIGRATION_2_3
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.data.repository.RecordReviewRepository
import com.nutriagricola.app.domain.model.advisor.RecordReview
import com.nutriagricola.app.domain.model.advisor.ReviewResult
import com.nutriagricola.app.domain.model.record.RecordStatus
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdvisorPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "advisor-test-${UUID.randomUUID()}.db"
    private var database: AppDatabase? = null

    @After
    fun closeTestDatabase() {
        database?.close()
        context.deleteDatabase(databaseName)
    }

    private fun createFarmerVersion1() {
        val path = context.getDatabasePath(databaseName)
        path.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { db ->
            farmerVersion1Schema.forEach { db.execSQL(it) }
            db.execSQL("INSERT INTO users VALUES ('farmer', 'Ana', 'Pérez', 'farmer@example.test', 'FARMER', 1)")
            db.execSQL("INSERT INTO users VALUES ('advisor', 'Luis', 'Díaz', 'advisor@example.test', 'ADVISOR', 1)")
            db.execSQL("INSERT INTO farms (id, name, active) VALUES ('farm', 'Predio de prueba', 1)")
            db.execSQL("INSERT INTO crops (id, farmId, name, active) VALUES ('crop', 'farm', 'Palto', 1)")
            db.execSQL("INSERT INTO records VALUES ('record', 'crop', 'farmer', '2026-10-01', 'FOLIAR', 'PENDING', 'Nota original')")
            db.execSQL("INSERT INTO foliar_data (recordId, nitrogen, symptoms) VALUES ('record', 0.0, 'Síntoma original')")
            db.execSQL("INSERT INTO foliar_photos VALUES ('photo', 'record', '/private/test-photo.jpg')")
            db.version = 1
        }
    }

    private fun openMigratedDatabase(): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build().also { database = it }
    }

    @Test
    fun version1MigrationPreservesFarmerDataAndPhotos() = runBlocking {
        createFarmerVersion1()
        val db = openMigratedDatabase()
        val record = RecordRepository(db).getById("record")!!
        assertEquals("farmer", record.userId)
        assertEquals("Nota original", record.observations)
        val data = record.data as com.nutriagricola.app.domain.model.record.FoliarData
        assertEquals(0.0, data.nitrogen!!, 0.0)
        assertEquals("Síntoma original", data.symptoms)
        assertEquals(listOf("/private/test-photo.jpg"), data.photos)
        assertEquals(2, db.userDao().getAll().size)
        assertTrue(db.recordReviewDao().getAll().isEmpty())
    }

    @Test
    fun version2MigrationRestoresObservedPendingAndPreservesValidatedAndUnreviewed() = runBlocking {
        createFarmerVersion1()
        val path = context.getDatabasePath(databaseName)
        SQLiteDatabase.openDatabase(path.path, null, SQLiteDatabase.OPEN_READWRITE).use { raw ->
            // Version 2 had the same reviews schema, but used VIEWED as an outcome.
            raw.execSQL("CREATE TABLE record_reviews (id TEXT NOT NULL PRIMARY KEY, recordId TEXT NOT NULL, advisorId TEXT NOT NULL, advisorName TEXT NOT NULL, date TEXT NOT NULL, result TEXT NOT NULL, comment TEXT NOT NULL, FOREIGN KEY(recordId) REFERENCES records(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(advisorId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            raw.execSQL("CREATE INDEX index_record_reviews_recordId ON record_reviews(recordId)")
            raw.execSQL("CREATE INDEX index_record_reviews_advisorId ON record_reviews(advisorId)")
            raw.execSQL("UPDATE records SET status = 'VIEWED' WHERE id = 'record'")
            raw.execSQL("INSERT INTO records VALUES ('validated', 'crop', 'farmer', '2026-10-01', 'SOIL', 'VALIDATED', NULL)")
            raw.execSQL("INSERT INTO records VALUES ('viewed-only', 'crop', 'farmer', '2026-10-01', 'SOIL', 'VIEWED', NULL)")
            raw.execSQL("INSERT INTO record_reviews VALUES ('r1','record','advisor','Luis','2026-10-01T10:00','VIEWED','Revisar muestra')")
            raw.execSQL("INSERT INTO record_reviews VALUES ('r2','validated','advisor','Luis','2026-10-01T10:00','VIEWED','Primera revisión')")
            raw.execSQL("INSERT INTO record_reviews VALUES ('r3','validated','advisor','Luis','2026-10-02T10:00','VALIDATED','Correcto')")
            raw.version = 2
        }
        val db = openMigratedDatabase()
        assertEquals("PENDING", db.recordDao().getById("record")!!.status)
        assertEquals("VALIDATED", db.recordDao().getById("validated")!!.status)
        assertEquals("VIEWED", db.recordDao().getById("viewed-only")!!.status)
        assertEquals(3, db.recordReviewDao().getAll().size)
        assertEquals("OBSERVED", db.recordReviewDao().getByRecordId("record").first().result)
        assertEquals(1, db.foliarPhotoDao().getByRecordId("record").size)
    }

    @Test
    fun reviewingPreservesOriginalDataAndRejectsInvalidWrites() = runBlocking {
        createFarmerVersion1()
        val db = openMigratedDatabase()
        val records = RecordRepository(db)
        val reviews = RecordReviewRepository(db)
        val original = records.getById("record")!!
        val observed = RecordReview("review1", "record", "advisor", "Luis", LocalDateTime.of(2026, 10, 1, 10, 0), ReviewResult.OBSERVED, "Revisar muestra")
        reviews.saveReviewAndUpdateStatus(observed)
        assertEquals(original, records.getById("record"))
        val validated = observed.copy(id = "review2", date = observed.date.plusHours(1), result = ReviewResult.VALIDATED)
        reviews.saveReviewAndUpdateStatus(validated)
        assertEquals(original.copy(status = RecordStatus.VALIDATED), records.getById("record"))
        assertEquals(listOf("review2", "review1"), reviews.getReviewsForRecord("record").map { it.id })

        // A duplicate ID must fail atomically: neither a new review nor a pending status.
        assertTrue(runCatching { reviews.saveReviewAndUpdateStatus(observed) }.isFailure)
        assertEquals(RecordStatus.VALIDATED, records.getById("record")!!.status)
        assertEquals(2, reviews.getReviewsForRecord("record").size)
        // Force a failure after the review insert to verify the whole transaction rolls back.
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fail_status_update BEFORE UPDATE ON records BEGIN SELECT RAISE(ABORT, 'Test failure'); END")
        try {
            assertTrue(runCatching { reviews.saveReviewAndUpdateStatus(observed.copy(id = "rolled-back")) }.isFailure)
        } finally {
            db.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_status_update")
        }
        assertEquals(RecordStatus.VALIDATED, records.getById("record")!!.status)
        assertEquals(2, reviews.getReviewsForRecord("record").size)
        assertTrue(runCatching { reviews.saveReviewAndUpdateStatus(observed.copy(id = "empty", comment = " ")) }.isFailure)
        assertTrue(runCatching { reviews.saveReviewAndUpdateStatus(observed.copy(id = "farmer-review", advisorId = "farmer")) }.isFailure)
        val advisor = db.userDao().getById("advisor")!!
        db.userDao().update(advisor.copy(active = false))
        assertTrue(runCatching { reviews.saveReviewAndUpdateStatus(observed.copy(id = "inactive")) }.isFailure)
        assertEquals(2, reviews.getReviewsForRecord("record").size)
    }
}
