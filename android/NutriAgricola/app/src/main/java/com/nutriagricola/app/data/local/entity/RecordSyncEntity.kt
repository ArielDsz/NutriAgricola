package com.nutriagricola.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// A missing row means the record was created locally and has not been downloaded.
@Entity(tableName = "record_sync", foreignKeys = [ForeignKey(
    entity = RecordEntity::class, parentColumns = ["id"], childColumns = ["recordId"],
    onDelete = ForeignKey.CASCADE
)])
data class RecordSyncEntity(@PrimaryKey val recordId: String, val dirty: Boolean,
                            val remotePhotoCount: Int)
