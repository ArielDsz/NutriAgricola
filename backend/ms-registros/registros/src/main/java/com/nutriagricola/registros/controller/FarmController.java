package com.nutriagricola.registros.controller;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.dto.FarmCreateRequest;
import com.nutriagricola.registros.dto.FarmUpdateRequest;
import com.nutriagricola.registros.service.FarmService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.nutriagricola.registros.dto.CropResponse;
import com.nutriagricola.registros.dto.FarmResponse;
import com.nutriagricola.registros.service.RecordsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FarmController {
    private final RecordsService recordsService;
    private final FarmService farmService;

    public FarmController(RecordsService recordsService, FarmService farmService) {
        this.recordsService = recordsService;
        this.farmService = farmService;
    }

    @GetMapping("/farms")
    public List<FarmResponse> getFarms(@AuthenticationPrincipal UserClientResponse user) {
        return farmService.findAll(user);
    }

    @GetMapping("/farms/{id}")
    public FarmResponse getById(@PathVariable String id, @AuthenticationPrincipal UserClientResponse user) {
        return farmService.findById(id, user);
    }

    @PostMapping("/farms")
    @ResponseStatus(HttpStatus.CREATED)
    public FarmResponse create(@Valid @RequestBody FarmCreateRequest request,
                               @AuthenticationPrincipal UserClientResponse user) {
        return farmService.create(request, user);
    }

    @PutMapping("/farms/{id}")
    public FarmResponse update(@PathVariable String id, @Valid @RequestBody FarmUpdateRequest request,
                               @AuthenticationPrincipal UserClientResponse user) {
        return farmService.update(id, request, user);
    }

    @DeleteMapping("/farms/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable String id, @AuthenticationPrincipal UserClientResponse user) {
        farmService.deactivate(id, user);
    }

    @GetMapping("/crops/farm/{farmId}")
    public List<CropResponse> getCropsByFarm(@PathVariable String farmId,
                                           @AuthenticationPrincipal UserClientResponse user) {
        return recordsService.getCropsByFarm(farmId, user);
    }
}
