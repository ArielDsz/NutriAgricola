package com.nutriagricola.registros.controller;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.dto.CropCreateRequest;
import com.nutriagricola.registros.dto.CropResponse;
import com.nutriagricola.registros.dto.CropUpdateRequest;
import com.nutriagricola.registros.service.CropService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crops")
public class CropController {
    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @GetMapping("/farm/{farmId}")
    public List<CropResponse> getByFarm(@PathVariable String farmId,
                                       @AuthenticationPrincipal UserClientResponse user) {
        return cropService.findByFarm(farmId, user);
    }

    @GetMapping("/{id}")
    public CropResponse getById(@PathVariable String id, @AuthenticationPrincipal UserClientResponse user) {
        return cropService.findById(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CropResponse create(@Valid @RequestBody CropCreateRequest request,
                               @AuthenticationPrincipal UserClientResponse user) {
        return cropService.create(request, user);
    }

    @PutMapping("/{id}")
    public CropResponse update(@PathVariable String id, @Valid @RequestBody CropUpdateRequest request,
                               @AuthenticationPrincipal UserClientResponse user) {
        return cropService.update(id, request, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable String id, @AuthenticationPrincipal UserClientResponse user) {
        cropService.deactivate(id, user);
    }
}
