package com.nutriagricola.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farms")
data class FarmEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val location: String?,
    val active: Boolean
)