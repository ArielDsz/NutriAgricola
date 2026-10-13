package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.local.entity.CropEntity
import com.nutriagricola.app.domain.model.farm.Crop

fun CropEntity.toDomain(): Crop {
    return Crop(
        id = id,
        farmId = farmId,
        name = name,
        variety = variety,
        areaHectares = areaHectares,
        active = active,
        imagePath = imagePath,
        phenologicalStage = phenologicalStage
    )
}

fun Crop.toEntity(): CropEntity {
    return CropEntity(
        id = id,
        farmId = farmId,
        name = name,
        variety = variety,
        areaHectares = areaHectares,
        active = active,
        imagePath = imagePath,
        phenologicalStage = phenologicalStage
    )
}