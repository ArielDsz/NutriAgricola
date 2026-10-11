package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.local.entity.FarmEntity
import com.nutriagricola.app.domain.model.farm.Farm

fun FarmEntity.toDomain(): Farm {
    return Farm(
        id = id,
        name = name,
        location = location,
        active = active
    )
}

fun Farm.toEntity(): FarmEntity {
    return FarmEntity(
        id = id,
        name = name,
        location = location,
        active = active
    )
}