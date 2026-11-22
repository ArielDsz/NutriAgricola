package com.nutriagricola.app.data.local.entity

import androidx.room.*

@Entity(tableName = "remote_photos", foreignKeys = [ForeignKey(
    entity = RecordEntity::class, parentColumns = ["id"], childColumns = ["recordId"],
    onDelete = ForeignKey.CASCADE
)], indices = [Index("recordId")])
data class RemotePhotoEntity(@PrimaryKey val id: String, val recordId: String, val uploaded: Boolean)
