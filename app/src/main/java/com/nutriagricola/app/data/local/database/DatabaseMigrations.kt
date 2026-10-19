package com.nutriagricola.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
