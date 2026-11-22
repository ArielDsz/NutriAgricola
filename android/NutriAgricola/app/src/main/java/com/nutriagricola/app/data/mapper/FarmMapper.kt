package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.local.entity.FarmEntity
import com.nutriagricola.app.domain.model.farm.Farm
import com.nutriagricola.app.data.remote.dto.FarmResponseDto

fun FarmResponseDto.toEntity() = FarmEntity(id, name, location, active, imagePath)

fun FarmEntity.toDomain(): Farm {
    return Farm(
        id = id,
        name = name,
        location = location,
        active = active,
        imagePath = imagePath
    )
}

fun Farm.toEntity(): FarmEntity {
    return FarmEntity(
        id = id,
        name = name,
        location = location,
        active = active,
        imagePath = imagePath
    )
}
