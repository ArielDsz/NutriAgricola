package com.nutriagricola.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS remote_photos (
                id TEXT NOT NULL PRIMARY KEY, recordId TEXT NOT NULL, uploaded INTEGER NOT NULL,
                FOREIGN KEY(recordId) REFERENCES records(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_remote_photos_recordId ON remote_photos(recordId)")
    }
}

// Existing records remain local. Only a successful download creates a sync marker.
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS record_sync (
                recordId TEXT NOT NULL PRIMARY KEY,
                dirty INTEGER NOT NULL,
                remotePhotoCount INTEGER NOT NULL,
                FOREIGN KEY(recordId) REFERENCES records(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
    }
}

// Keep existing farms and records. Remote assignments are downloaded per account.
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS farm_users (
                userId TEXT NOT NULL,
                farmId TEXT NOT NULL,
                PRIMARY KEY(userId, farmId),
                FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(farmId) REFERENCES farms(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_farm_users_farmId ON farm_users(farmId)")
    }
}

// Add Advisor reviews without rebuilding any Farmer tables.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS record_reviews (
                id TEXT NOT NULL PRIMARY KEY,
                recordId TEXT NOT NULL,
                advisorId TEXT NOT NULL,
                advisorName TEXT NOT NULL,
                date TEXT NOT NULL,
                result TEXT NOT NULL,
                comment TEXT NOT NULL,
                FOREIGN KEY(recordId) REFERENCES records(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(advisorId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_record_reviews_recordId ON record_reviews(recordId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_record_reviews_advisorId ON record_reviews(advisorId)")
    }
}

// Version 2 used VIEWED for observations. Preserve those reviews and restore
// pending only where the latest review actually requested an observation.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE record_reviews SET result = 'OBSERVED' WHERE result = 'VIEWED'")
        db.execSQL("""
            UPDATE records SET status = 'PENDING'
            WHERE status = 'VIEWED' AND (
                SELECT result FROM record_reviews
                WHERE recordId = records.id
                ORDER BY date DESC, rowid DESC LIMIT 1
            ) = 'OBSERVED'
        """.trimIndent())
    }
}
