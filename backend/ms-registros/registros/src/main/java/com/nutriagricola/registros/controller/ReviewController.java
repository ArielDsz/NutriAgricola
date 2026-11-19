package com.nutriagricola.registros.controller;

import com.nutriagricola.registros.client.UserClientResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.nutriagricola.registros.dto.ReviewCreateRequest;
import com.nutriagricola.registros.dto.ReviewCreateResponse;
import com.nutriagricola.registros.service.RecordsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final RecordsService recordsService;

    public ReviewController(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewCreateResponse create(@Valid @RequestBody ReviewCreateRequest request,
                                        @AuthenticationPrincipal UserClientResponse user) {
        return recordsService.createReview(request, user);
    }
}
