package com.nutriagricola.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "record_reviews",
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["advisorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("recordId"),
        Index("advisorId")
    ]
)
data class RecordReviewEntity(
    @PrimaryKey
    val id: String,
    val recordId: String,
    val advisorId: String,
    val advisorName: String,
    val date: String,
    val result: String,
    val comment: String
)
