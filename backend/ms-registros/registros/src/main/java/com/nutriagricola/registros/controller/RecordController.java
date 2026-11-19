package com.nutriagricola.registros.controller;

import com.nutriagricola.registros.client.UserClientResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.nutriagricola.registros.dto.RecordCreateRequest;
import com.nutriagricola.registros.dto.RecordCreateResponse;
import com.nutriagricola.registros.dto.RecordSummaryResponse;
import com.nutriagricola.registros.service.RecordsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/records")
public class RecordController {
    private final RecordsService recordsService;

    public RecordController(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    @GetMapping("/status/{status}")
    public List<RecordSummaryResponse> getByStatus(@PathVariable String status,
                                                  @AuthenticationPrincipal UserClientResponse user) {
        return recordsService.getRecordsByStatus(status, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecordCreateResponse create(@Valid @RequestBody RecordCreateRequest request,
                                        @AuthenticationPrincipal UserClientResponse user) {
        return recordsService.createRecord(request, user);
    }
}
