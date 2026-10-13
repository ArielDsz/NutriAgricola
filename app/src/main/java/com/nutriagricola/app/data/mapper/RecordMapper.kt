package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.local.entity.RecordEntity
import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.record.RecordData
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.record.RecordType
import java.time.LocalDate

fun RecordEntity.toDomain(data: RecordData): Record {
    return Record(
        id = id,
        cropId = cropId,
        userId = userId,
        date = LocalDate.parse(date),
        type = RecordType.valueOf(type),
        status = RecordStatus.valueOf(status),
        observations = observations,
        data = data
    )
}

fun Record.toEntity(): RecordEntity {
    return RecordEntity(
        id = id,
        cropId = cropId,
        userId = userId,
        date = date.toString(),
        type = type.name,
        status = status.name,
        observations = observations
    )
}