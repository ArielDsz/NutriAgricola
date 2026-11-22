package com.nutriagricola.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "farm_users",
    primaryKeys = ["userId", "farmId"],
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FarmEntity::class, parentColumns = ["id"], childColumns = ["farmId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("farmId")]
)
data class FarmUserEntity(val userId: String, val farmId: String)
