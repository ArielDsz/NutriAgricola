package com.nutriagricola.registros.service;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.dto.CropCreateRequest;
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

@Service
public class CropService {
    private final CropRepository crops;
    private final FarmRepository farms;
    private final FarmUserRepository farmUsers;
    private final CropMapper cropMapper;

    public CropService(CropRepository crops, FarmRepository farms,
                       FarmUserRepository farmUsers, CropMapper cropMapper) {
        this.crops = crops;
        this.farms = farms;
        this.farmUsers = farmUsers;
        this.cropMapper = cropMapper;
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
    public CropResponse create(CropCreateRequest request, UserClientResponse user) {
        requireFarmer(user);
        requireFarmAccess(request.farmId(), user);
        Farm farm = farms.findById(request.farmId()).orElseThrow(() ->
                new ResourceNotFoundException("Predio no encontrado"));
        if (!Boolean.TRUE.equals(farm.getActive())) {
            throw new ResourceConflictException("No se pueden crear cultivos en un predio inactivo");
        }
        if (crops.existsById(request.id())) {
            throw new ResourceConflictException("Ya existe un cultivo con el identificador indicado");
        }
        validateArea(request.areaHectares());
        return cropMapper.toResponse(crops.save(cropMapper.toEntity(request, farm)));
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
