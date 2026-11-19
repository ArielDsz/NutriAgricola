package com.nutriagricola.registros.mapper;

import com.nutriagricola.registros.dto.ReviewCreateRequest;
import com.nutriagricola.registros.dto.ReviewCreateResponse;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.model.RecordReview;
import org.springframework.stereotype.Component;

@Component
public class RecordReviewMapper {
    public RecordReview toEntity(ReviewCreateRequest request, Record record, String result,
                                 String advisorId, String advisorName) {
        RecordReview review = new RecordReview();
        review.setId(request.id());
        review.setRecord(record);
        review.setAdvisorId(advisorId);
        review.setAdvisorName(advisorName);
        review.setDate(request.date());
        review.setResult(result);
        review.setComment(request.comment());
        return review;
    }

    public ReviewCreateResponse toResponse(RecordReview review, String recordStatus) {
        return new ReviewCreateResponse(review.getId(), review.getRecord().getId(), review.getAdvisorId(),
                review.getAdvisorName(), review.getDate(), review.getResult(), review.getComment(),
                recordStatus);
    }
}
