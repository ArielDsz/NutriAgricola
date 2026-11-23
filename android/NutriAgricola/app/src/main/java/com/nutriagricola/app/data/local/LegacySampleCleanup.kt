package com.nutriagricola.app.data.local

import android.content.Context
import androidx.room.withTransaction
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.entity.*
import com.nutriagricola.app.domain.model.record.RecordType
import java.util.UUID
import kotlin.math.round

/** Solo retira ejemplos deterministas intactos; nunca vacía Room ni elimina fotos. */
suspend fun cleanupLegacySamples(database: AppDatabase, context: Context) {
    val preferences = context.getSharedPreferences("sample_cleanup", Context.MODE_PRIVATE)
    if (preferences.getBoolean("backend_dataset_v7", false)) return
    database.withTransaction {
        val layouts = listOf(
            "Los Aromos" to listOf("Palto", "Limonero", "Tomate", "Naranjo", "Mandarino"),
            "El Maitén" to listOf("Palto", "Tomate", "Limonero", "Vid"),
            "Santa Elena" to listOf("Limonero", "Tomate", "Palto")
        )
        var cropIndex = 0
        val farms = database.farmDao().getAll()
        val crops = database.cropDao().getAll()
        for ((farmName, cropNames) in layouts) {
            for (cropName in cropNames) {
                val matchingCrops = crops.filter { crop ->
                    crop.name == cropName && farms.any {
                        it.id == crop.farmId && it.name == farmName &&
                            it.imagePath?.startsWith("android.resource://") == true
                    }
                }
                for (crop in matchingCrops) repeat(4) { sampleIndex ->
                    val recordId = UUID.nameUUIDFromBytes(
                        "expanded_demo:${crop.id}:$sampleIndex".toByteArray(Charsets.UTF_8)
                    ).toString()
                    val record = database.recordDao().getById(recordId) ?: return@repeat
                    val expectedNotes = if (sampleIndex == 1) null else
                        "Datos ficticios de demostración. Seguimiento de ${crop.name}, " +
                            "variedad ${crop.variety}, muestra ${sampleIndex + 1} del sector de riego."
                    val type = RecordType.entries[(cropIndex + sampleIndex) % RecordType.entries.size]
                    val expectedStatus = if (sampleIndex % 2 == 0) "PENDING" else "VALIDATED"
                    if (record.cropId == crop.id && record.type == type.name &&
                        record.status == expectedStatus && record.observations == expectedNotes &&
                        database.recordSyncDao().getById(recordId) == null &&
                        database.recordReviewDao().getByRecordId(recordId).isEmpty() &&
                        database.foliarPhotoDao().getByRecordId(recordId).isEmpty() &&
                        matchesLegacyMeasurements(database, recordId, type, cropIndex, sampleIndex)) {
                        database.recordDao().delete(record)
                    }
                }
                cropIndex++
            }
        }
    }
    preferences.edit().putBoolean("backend_dataset_v7", true).apply()
}

private suspend fun matchesLegacyMeasurements(
    database: AppDatabase, recordId: String, type: RecordType, cropIndex: Int, sampleIndex: Int
): Boolean {
    val partial = sampleIndex == 3
    val variation = (cropIndex % 4) * 0.1 + sampleIndex * 0.05
    fun variedValue(base: Double) = round((base + variation) * 100) / 100
    return when (type) {
        RecordType.SOIL -> database.soilDataDao().getByRecordId(recordId) == (
            SoilDataEntity(
                recordId = recordId,
                ph = variedValue(6.2),
                electricalConductivity = variedValue(0.9),
                nitrogen = if (partial) null else 28.0 + cropIndex + sampleIndex,
                phosphorus = if (partial) null else 14.0 + cropIndex,
                potassium = if (partial) null else 145.0 + cropIndex * 4,
                calcium = if (partial) null else 920.0 + cropIndex * 25,
                magnesium = if (partial) null else 110.0 + cropIndex * 3,
                organicMatter = if (partial) null else variedValue(2.4)
            )
        )
        RecordType.WATER -> database.waterDataDao().getByRecordId(recordId) == (
            WaterDataEntity(
                recordId = recordId,
                ph = if (partial) null else variedValue(7.0),
                electricalConductivity = if (partial) null else variedValue(0.6),
                nitrogen = if (partial) null else if (cropIndex % 3 == 0) 0.0 else 4.0 + cropIndex,
                phosphorus = if (partial) null else variedValue(1.0),
                potassium = if (partial) null else 8.0 + cropIndex,
                calcium = if (partial) null else 55.0 + cropIndex * 2,
                magnesium = if (partial) null else 15.0 + cropIndex,
                waterSource = listOf("Pozo profundo", "Canal de riego", "Estanque de acumulación")[cropIndex % 3]
            )
        )
        RecordType.FOLIAR -> database.foliarDataDao().getByRecordId(recordId) == (
            FoliarDataEntity(
                recordId = recordId,
                nitrogen = if (partial) null else variedValue(1.8),
                phosphorus = if (partial) null else variedValue(0.18),
                potassium = if (partial) null else variedValue(1.2),
                calcium = if (partial) null else variedValue(1.4),
                magnesium = if (partial) null else variedValue(0.30),
                symptoms = if (cropIndex % 2 == 0)
                    "Amarillamiento leve en hojas del sector norte; muestra de demostración."
                else "Hojas con bordes secos en una parte del sector; muestra de demostración."
            )
        )
    }
}
