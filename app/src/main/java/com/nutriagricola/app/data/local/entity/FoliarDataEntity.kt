package com.nutriagricola.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "foliar_data",
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FoliarDataEntity(
    @PrimaryKey
    val recordId: String,
    val nitrogen: Double?,
    val phosphorus: Double?,
    val potassium: Double?,
    val calcium: Double?,
    val magnesium: Double?,
    val symptoms: String?
)