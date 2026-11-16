package com.nutriagricola.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foliar_photos",
    foreignKeys = [
        ForeignKey(
            entity = FoliarDataEntity::class,
            parentColumns = ["recordId"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recordId")]
)
data class FoliarPhotoEntity(
    @PrimaryKey
    val id: String,
    val recordId: String,
    val path: String
)