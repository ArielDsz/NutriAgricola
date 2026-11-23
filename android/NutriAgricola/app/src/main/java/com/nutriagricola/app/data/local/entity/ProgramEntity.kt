package com.nutriagricola.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Instantáneas JSON del contrato remoto: resumen liviano y detalle descargado opcional.
@Entity(tableName = "programs")
data class ProgramEntity(@PrimaryKey val id: String, val startDate: String,
                          val summaryJson: String, val detailJson: String?)
