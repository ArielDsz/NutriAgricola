package com.nutriagricola.app.data.local

import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.entity.CropEntity
import com.nutriagricola.app.data.local.entity.FarmEntity
import java.util.UUID

suspend fun seedDatabase(database: AppDatabase) {

    val farmDao = database.farmDao()
    val cropDao = database.cropDao()

    var farms = farmDao.getAll()

    if (farms.isEmpty()) {
        val farm = FarmEntity(
            id = UUID.randomUUID().toString(),
            name = "Los Aromos",
            location = null,
            imagePath = null,
            active = true
        )

        farmDao.insert(farm)
        farms = listOf(farm)
    }

    val farm = farms.first()

    if (cropDao.getByFarmId(farm.id).isEmpty()) {

        cropDao.insert(
            CropEntity(
                id = UUID.randomUUID().toString(),
                farmId = farm.id,
                name = "Palto",
                variety = "Hass",
                areaHectares = 2.5,
                phenologicalStage = "Floración",
                imagePath = null,
                active = true
            )
        )

        cropDao.insert(
            CropEntity(
                id = UUID.randomUUID().toString(),
                farmId = farm.id,
                name = "Limonero",
                variety = "Eureka",
                areaHectares = 1.2,
                phenologicalStage = "Maduración",
                imagePath = null,
                active = true
            )
        )
    }
}