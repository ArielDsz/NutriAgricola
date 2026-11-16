package com.nutriagricola.app.data.local

import android.content.Context
import androidx.room.withTransaction
import com.nutriagricola.app.R
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.entity.*
import com.nutriagricola.app.domain.model.user.UserRole
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.record.RecordType
import java.time.LocalDate
import java.util.UUID
import kotlin.math.round

suspend fun seedDatabase(database: AppDatabase, context: Context) {

    database.withTransaction {

        val userDao = database.userDao()
        val userCredentialDao = database.userCredentialDao()
        val farmDao = database.farmDao()
        val cropDao = database.cropDao()
        val recordDao = database.recordDao()
        val soilDataDao = database.soilDataDao()
        val waterDataDao = database.waterDataDao()
        val foliarDataDao = database.foliarDataDao()

        // USUARIOS
        var users = userDao.getAll()

        val farmer = users.find { it.email == "farmer@test.cl" }
            ?: UserEntity(
                id = UUID.randomUUID().toString(),
                firstName = "Angel",
                lastName = "Muñoz",
                email = "farmer@test.cl",
                role = UserRole.FARMER.name,
                active = true
            ).also {
                userDao.insert(it)
            }

        val advisor = users.find { it.email == "advisor@test.cl" }
            ?: UserEntity(
                id = UUID.randomUUID().toString(),
                firstName = "María",
                lastName = "González",
                email = "advisor@test.cl",
                role = UserRole.ADVISOR.name,
                active = true
            ).also {
                userDao.insert(it)
            }

        // Precomputed PBKDF2-HMAC-SHA256 credentials for the demo accounts.
        if (userCredentialDao.getByUserId(farmer.id) == null) {
            userCredentialDao.insert(
                UserCredentialEntity(
                    userId = farmer.id,
                    passwordHash = "LDNO9DFzuciuvjJ6St1t/LF8Z2F264KO8iH4qWW128s=",
                    salt = "QnTS/ORJYI4do4xhYwiClQ==",
                    iterations = 600_000
                )
            )
        }

        if (userCredentialDao.getByUserId(advisor.id) == null) {
            userCredentialDao.insert(
                UserCredentialEntity(
                    userId = advisor.id,
                    passwordHash = "LBpwUbrB77Hwfqz4EXiz2xa/rpDo4crcTZTrdv8J7a4=",
                    salt = "8T5woAkl+JZGcwCnkASY5A==",
                    iterations = 600_000
                )
            )
        }

        fun resourceUri(resourceId: Int): String =
            "android.resource://${context.packageName}/$resourceId"

        // Refresh demo image URIs on startup: resource IDs can change between builds.
        // Existing entities retain their IDs and other stored information.
        suspend fun ensureFarm(name: String, location: String, imageId: Int): FarmEntity {
            val existing = farmDao.getAll().find { it.name == name }
            val imagePath = resourceUri(imageId)
            if (existing != null) {
                val updated = existing.copy(imagePath = imagePath)
                if (updated != existing) farmDao.update(updated)
                return updated
            }
            return FarmEntity(
                id = UUID.randomUUID().toString(), name = name, location = location,
                active = true, imagePath = imagePath
            ).also { farmDao.insert(it) }
        }

        suspend fun ensureCrop(
            farmId: String, name: String, variety: String,
            areaHectares: Double, stage: String, imageId: Int
        ): CropEntity {
            val existing = cropDao.getByFarmId(farmId).find { it.name == name }
            val imagePath = resourceUri(imageId)
            if (existing != null) {
                val updated = existing.copy(imagePath = imagePath)
                if (updated != existing) cropDao.update(updated)
                return updated
            }
            return CropEntity(
                id = UUID.randomUUID().toString(), farmId = farmId, name = name,
                variety = variety, areaHectares = areaHectares, phenologicalStage = stage,
                active = true, imagePath = imagePath
            ).also { cropDao.insert(it) }
        }

        val farm = ensureFarm("Los Aromos", "Quillota", R.drawable.farm_los_aromos)
        val maiten = ensureFarm("El Maitén", "Limache", R.drawable.farm_el_maiten)
        val santaElena = ensureFarm("Santa Elena", "La Cruz", R.drawable.farm_santa_elena)

        val avocado = ensureCrop(
            farm.id, "Palto", "Hass", 2.5, "Floración", R.drawable.crop_avocado
        )
        val lemon = ensureCrop(
            farm.id, "Limonero", "Eureka", 1.2, "Maduración", R.drawable.crop_lemon
        )
        val tomato = ensureCrop(farm.id, "Tomate", "Roma", 0.8, "Fructificación", R.drawable.crop_tomato)
        val orange = ensureCrop(
            farm.id, "Naranjo", "Navel", 1.6, "Desarrollo del fruto", R.drawable.crop_orange
        )
        val mandarin = ensureCrop(
            farm.id, "Mandarino", "Clemenules", 1.1, "Floración", R.drawable.crop_mandarin
        )
        val maitenAvocado = ensureCrop(
            maiten.id, "Palto", "Hass", 3.0, "Desarrollo del fruto", R.drawable.crop_avocado
        )
        val maitenTomato = ensureCrop(maiten.id, "Tomate", "Cherry", 0.5, "Floración", R.drawable.crop_tomato)
        val maitenLemon = ensureCrop(maiten.id, "Limonero", "Eureka", 1.4, "Maduración", R.drawable.crop_lemon)
        val grape = ensureCrop(
            maiten.id, "Vid", "Thompson Seedless", 2.2, "Brotación", R.drawable.crop_grape
        )
        val santaElenaLemon = ensureCrop(
            santaElena.id, "Limonero", "Fino", 1.8, "Floración", R.drawable.crop_lemon
        )
        val santaElenaTomato = ensureCrop(
            santaElena.id, "Tomate", "Roma", 1.0, "Maduración", R.drawable.crop_tomato
        )
        val santaElenaAvocado = ensureCrop(
            santaElena.id, "Palto", "Hass", 2.0, "Floración", R.drawable.crop_avocado
        )

        // REGISTROS PALTO
        if (recordDao.getByCropId(avocado.id).isEmpty()) {

            val soilRecordId = UUID.randomUUID().toString()

            recordDao.insert(
                RecordEntity(
                    id = soilRecordId,
                    cropId = avocado.id,
                    userId = farmer.id,
                    date = LocalDate.now().minusDays(3).toString(),
                    type = "SOIL",
                    status = "PENDING",
                    observations = "Muestra tomada en sector principal"
                )
            )

            soilDataDao.insert(
                SoilDataEntity(
                    recordId = soilRecordId,
                    ph = 6.4,
                    electricalConductivity = 1.2,
                    nitrogen = 42.0,
                    phosphorus = 18.0,
                    potassium = 160.0,
                    calcium = 1200.0,
                    magnesium = 180.0,
                    organicMatter = 3.1
                )
            )

            val foliarRecordId = UUID.randomUUID().toString()

            recordDao.insert(
                RecordEntity(
                    id = foliarRecordId,
                    cropId = avocado.id,
                    userId = farmer.id,
                    date = LocalDate.now().minusDays(1).toString(),
                    type = "FOLIAR",
                    status = "PENDING",
                    observations = "Se observan algunas hojas amarillentas"
                )
            )

            foliarDataDao.insert(
                FoliarDataEntity(
                    recordId = foliarRecordId,
                    nitrogen = 2.1,
                    phosphorus = 0.25,
                    potassium = 1.4,
                    calcium = 1.8,
                    magnesium = 0.45,
                    symptoms = "Amarillamiento leve en hojas"
                )
            )
        }

        // REGISTRO LIMONERO
        if (recordDao.getByCropId(lemon.id).isEmpty()) {

            val waterRecordId = UUID.randomUUID().toString()

            recordDao.insert(
                RecordEntity(
                    id = waterRecordId,
                    cropId = lemon.id,
                    userId = farmer.id,
                    date = LocalDate.now().minusDays(5).toString(),
                    type = "WATER",
                    status = "VALIDATED",
                    observations = "Agua proveniente de pozo"
                )
            )

            waterDataDao.insert(
                WaterDataEntity(
                    recordId = waterRecordId,
                    ph = 7.1,
                    electricalConductivity = 0.8,
                    nitrogen = 8.0,
                    phosphorus = 2.0,
                    potassium = 12.0,
                    calcium = 85.0,
                    magnesium = 24.0,
                    waterSource = "Pozo"
                )
            )
        }

        // Synthetic examples for UI testing, not agronomic recommendations or real reviews.
        val demoCrops = listOf(
            avocado, lemon, tomato, orange, mandarin,
            maitenAvocado, maitenTomato, maitenLemon, grape,
            santaElenaLemon, santaElenaTomato, santaElenaAvocado
        )
        val today = LocalDate.now()
        demoCrops.forEachIndexed { cropIndex, crop ->
            repeat(4) { sampleIndex ->
                val recordId = UUID.nameUUIDFromBytes(
                    "expanded_demo:${crop.id}:$sampleIndex".toByteArray(Charsets.UTF_8)
                ).toString()
                // Existing demo rows and user-created records are never overwritten or re-dated.
                if (recordDao.getById(recordId) == null) {
                    val type = RecordType.entries[(cropIndex + sampleIndex) % RecordType.entries.size]
                    val status = if (sampleIndex % 2 == 0) RecordStatus.PENDING else RecordStatus.VALIDATED
                    val daysAgo = sampleIndex * 10L + (cropIndex % 5) * 2L + cropIndex / 5 + 1L
                    val partial = sampleIndex == 3
                    val variation = (cropIndex % 4) * 0.1 + sampleIndex * 0.05
                    fun variedValue(base: Double) = round((base + variation) * 100) / 100
                    recordDao.insert(
                        RecordEntity(
                            id = recordId,
                            cropId = crop.id,
                            userId = farmer.id,
                            date = today.minusDays(daysAgo).toString(),
                            type = type.name,
                            status = status.name,
                            observations = if (sampleIndex == 1) null else
                                "Datos ficticios de demostración. Seguimiento de ${crop.name}, " +
                                    "variedad ${crop.variety}, muestra ${sampleIndex + 1} del sector de riego."
                        )
                    )
                    when (type) {
                        RecordType.SOIL -> soilDataDao.insert(
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
                        RecordType.WATER -> waterDataDao.insert(
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
                        RecordType.FOLIAR -> foliarDataDao.insert(
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
            }
        }
    }
}
