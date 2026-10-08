package com.nutriagricola.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = CropEntity::class,
            parentColumns = ["id"],
            childColumns = ["cropId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("cropId"),
        Index("userId")
    ]
)
data class RecordEntity(
    @PrimaryKey
    val id: String,
    val cropId: String,
    val userId: String,
    val date: String,
    val type: String,
    val status: String,
    val observations: String?
)