package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.local.entity.RecordReviewEntity
import com.nutriagricola.app.domain.model.advisor.RecordReview
import com.nutriagricola.app.domain.model.advisor.ReviewResult
import java.time.LocalDateTime

fun RecordReviewEntity.toDomain(): RecordReview {
    return RecordReview(
        id = id,
        recordId = recordId,
        advisorId = advisorId,
        advisorName = advisorName,
        date = LocalDateTime.parse(date),
        result = ReviewResult.valueOf(result),
        comment = comment
    )
}

fun RecordReview.toEntity(): RecordReviewEntity {
    return RecordReviewEntity(
        id = id,
        recordId = recordId,
        advisorId = advisorId,
        advisorName = advisorName,
        date = date.toString(),
        result = result.name,
        comment = comment
    )
}
