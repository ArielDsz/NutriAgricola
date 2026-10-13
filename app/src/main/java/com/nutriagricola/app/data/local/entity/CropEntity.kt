package com.nutriagricola.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "crops",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("farmId")]
)
data class CropEntity(
    @PrimaryKey
    val id: String,
    val farmId: String,
    val name: String,
    val variety: String?,
    val areaHectares: Double?,
    val active: Boolean,
    val imagePath: String? = null,
    val phenologicalStage: String? = null
)