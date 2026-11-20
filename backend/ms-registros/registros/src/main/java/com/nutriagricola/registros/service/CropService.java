package com.nutriagricola.registros.service;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.dto.CropCreateRequest;
import com.nutriagricola.registros.dto.CropCreationResult;
import jakarta.persistence.EntityManager;
import com.nutriagricola.registros.dto.CropResponse;
import com.nutriagricola.registros.dto.CropUpdateRequest;
import com.nutriagricola.registros.exception.ResourceConflictException;
import com.nutriagricola.registros.exception.ResourceNotFoundException;
import com.nutriagricola.registros.mapper.CropMapper;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.Farm;
import com.nutriagricola.registros.repository.CropRepository;
import com.nutriagricola.registros.repository.FarmRepository;
import com.nutriagricola.registros.repository.FarmUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class CropService {
    private final CropRepository crops;
    private final FarmRepository farms;
    private final FarmUserRepository farmUsers;
    private final CropMapper cropMapper;
    private final EntityManager entityManager;

    public CropService(CropRepository crops, FarmRepository farms,
                       FarmUserRepository farmUsers, CropMapper cropMapper, EntityManager entityManager) {
        this.crops = crops;
        this.farms = farms;
        this.farmUsers = farmUsers;
        this.cropMapper = cropMapper;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<CropResponse> findByFarm(String farmId, UserClientResponse user) {
        requireFarmAccess(farmId, user);
        return crops.findByFarm_IdOrderByNameAscIdAsc(farmId).stream().map(cropMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CropResponse findById(String id, UserClientResponse user) {
        return cropMapper.toResponse(findAssignedCrop(id, user));
    }

    @Transactional
    public CropCreationResult create(CropCreateRequest request, UserClientResponse user) {
        requireFarmer(user);
        validateArea(request.areaHectares());
        Crop existing = crops.findById(request.id()).orElse(null);
        if (existing != null) {
            requireFarmAccess(existing.getFarm().getId(), user);
            Crop candidate = cropMapper.toEntity(request, existing.getFarm());
            if (!request.farmId().equals(existing.getFarm().getId())
                    || !Objects.equals(candidate.getName(), existing.getName())
                    || !Objects.equals(candidate.getVariety(), existing.getVariety())
                    || !Objects.equals(candidate.getAreaHectares(), existing.getAreaHectares())
                    || !Objects.equals(candidate.getPhenologicalStage(), existing.getPhenologicalStage())
                    || !Objects.equals(candidate.getImagePath(), existing.getImagePath())) {
                throw new ResourceConflictException("El cultivo ya existe con datos diferentes");
            }
            return new CropCreationResult(cropMapper.toResponse(existing), false);
        }
        requireFarmAccess(request.farmId(), user);
        Farm farm = farms.findById(request.farmId()).orElseThrow(() ->
                new ResourceNotFoundException("Predio no encontrado"));
        if (!Boolean.TRUE.equals(farm.getActive())) {
            throw new ResourceConflictException("No se pueden crear cultivos en un predio inactivo");
        }
        Crop crop = cropMapper.toEntity(request, farm);
        entityManager.persist(crop);
        return new CropCreationResult(cropMapper.toResponse(crop), true);
    }

    @Transactional
    public CropResponse update(String id, CropUpdateRequest request, UserClientResponse user) {
        requireFarmer(user);
        Crop crop = findAssignedCrop(id, user);
        validateArea(request.areaHectares());
        cropMapper.updateEntity(request, crop);
        return cropMapper.toResponse(crops.save(crop));
    }

    @Transactional
    public void deactivate(String id, UserClientResponse user) {
        requireFarmer(user);
        Crop crop = findAssignedCrop(id, user);
        crop.setActive(false);
        crops.save(crop);
    }

    private Crop findAssignedCrop(String id, UserClientResponse user) {
        Crop crop = crops.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado"));
        requireFarmAccess(crop.getFarm().getId(), user);
        return crop;
    }

    private void requireFarmAccess(String farmId, UserClientResponse user) {
        if (!farmUsers.existsByFarm_IdAndUserId(farmId, user.id())) {
            throw new AccessDeniedException("No estás asignado a este predio");
        }
    }

    private void requireFarmer(UserClientResponse user) {
        if (!"FARMER".equals(user.role())) {
            throw new AccessDeniedException("Solo los responsables pueden gestionar cultivos");
        }
    }

    private void validateArea(Double area) {
        if (area != null && (!Double.isFinite(area) || area <= 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La superficie debe ser un número finito mayor que cero");
        }
    }
}
