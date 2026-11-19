package com.nutriagricola.registros.controller;

import com.nutriagricola.registros.client.UserClientResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.nutriagricola.registros.dto.RecordCreateRequest;
import com.nutriagricola.registros.dto.RecordCreateResponse;
import com.nutriagricola.registros.dto.RecordSummaryResponse;
import com.nutriagricola.registros.dto.RecordPageResponse;
import com.nutriagricola.registros.dto.RecordDetailResponse;
import com.nutriagricola.registros.service.RecordsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @AuthenticationPrincipal UserClientResponse user) {
        return recordsService.getHistory(null, null, status, page, size, user).content();
    }

    @GetMapping
    public RecordPageResponse getHistory(@RequestParam(required = false) String cropId,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @AuthenticationPrincipal UserClientResponse user) {
        return recordsService.getHistory(cropId, type, status, page, size, user);
    }

    @GetMapping("/{id}")
    public RecordDetailResponse getDetail(@PathVariable String id,
                                          @AuthenticationPrincipal UserClientResponse user) {
        return recordsService.getDetail(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, @AuthenticationPrincipal UserClientResponse user) {
        recordsService.deleteRecord(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecordCreateResponse create(@Valid @RequestBody RecordCreateRequest request,
                                        @AuthenticationPrincipal UserClientResponse user) {
        return recordsService.createRecord(request, user);
    }
}
