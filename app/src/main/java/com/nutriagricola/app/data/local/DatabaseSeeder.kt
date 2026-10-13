package com.nutriagricola.app.data.local

import androidx.room.withTransaction
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.entity.*
import java.time.LocalDate
import java.util.UUID

suspend fun seedDatabase(database: AppDatabase) {

    database.withTransaction {

        val userDao = database.userDao()
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
                role = "FARMER",
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
                role = "ADVISOR",
                active = true
            ).also {
                userDao.insert(it)
            }

        // PREDIO
        var farms = farmDao.getAll()

        val farm = farms.find { it.name == "Los Aromos" }
            ?: FarmEntity(
                id = UUID.randomUUID().toString(),
                name = "Los Aromos",
                location = "Quillota",
                imagePath = null,
                active = true
            ).also {
                farmDao.insert(it)
            }

        // CULTIVOS
        var crops = cropDao.getByFarmId(farm.id)

        val avocado = crops.find { it.name == "Palto" }
            ?: CropEntity(
                id = UUID.randomUUID().toString(),
                farmId = farm.id,
                name = "Palto",
                variety = "Hass",
                areaHectares = 2.5,
                phenologicalStage = "Floración",
                imagePath = null,
                active = true
            ).also {
                cropDao.insert(it)
            }

        val lemon = crops.find { it.name == "Limonero" }
            ?: CropEntity(
                id = UUID.randomUUID().toString(),
                farmId = farm.id,
                name = "Limonero",
                variety = "Eureka",
                areaHectares = 1.2,
                phenologicalStage = "Maduración",
                imagePath = null,
                active = true
            ).also {
                cropDao.insert(it)
            }

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