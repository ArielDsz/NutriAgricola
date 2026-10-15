package com.nutriagricola.app.data.local

import android.content.Context
import androidx.room.withTransaction
import com.nutriagricola.app.R
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.entity.*
import com.nutriagricola.app.domain.model.user.UserRole
import java.time.LocalDate
import java.util.UUID

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
        ensureCrop(farm.id, "Tomate", "Roma", 0.8, "Fructificación", R.drawable.crop_tomato)
        ensureCrop(maiten.id, "Palto", "Hass", 3.0, "Desarrollo del fruto", R.drawable.crop_avocado)
        ensureCrop(maiten.id, "Tomate", "Cherry", 0.5, "Floración", R.drawable.crop_tomato)
        ensureCrop(santaElena.id, "Limonero", "Fino", 1.8, "Floración", R.drawable.crop_lemon)
        ensureCrop(santaElena.id, "Tomate", "Roma", 1.0, "Maduración", R.drawable.crop_tomato)

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
    }
}
