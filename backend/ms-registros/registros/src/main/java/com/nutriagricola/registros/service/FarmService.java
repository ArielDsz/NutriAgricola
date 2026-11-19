package com.nutriagricola.registros.service;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.dto.FarmCreateRequest;
import com.nutriagricola.registros.dto.FarmResponse;
import com.nutriagricola.registros.dto.FarmUpdateRequest;
import com.nutriagricola.registros.exception.ResourceConflictException;
import com.nutriagricola.registros.exception.ResourceNotFoundException;
import com.nutriagricola.registros.mapper.FarmMapper;
import com.nutriagricola.registros.model.Farm;
import com.nutriagricola.registros.model.FarmUser;
import com.nutriagricola.registros.repository.FarmRepository;
import com.nutriagricola.registros.repository.FarmUserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FarmService {
    private final FarmRepository farms;
    private final FarmUserRepository farmUsers;
    private final FarmMapper farmMapper;

    public FarmService(FarmRepository farms, FarmUserRepository farmUsers, FarmMapper farmMapper) {
        this.farms = farms;
        this.farmUsers = farmUsers;
        this.farmMapper = farmMapper;
    }

    @Transactional(readOnly = true)
    public List<FarmResponse> findAll(UserClientResponse user) {
        return farmUsers.findFarmsByUserId(user.id()).stream().map(farmMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FarmResponse findById(String id, UserClientResponse user) {
        return farmMapper.toResponse(findAssignedFarm(id, user));
    }

    @Transactional
    public FarmResponse create(FarmCreateRequest request, UserClientResponse user) {
        requireFarmer(user);
        if (farms.existsById(request.id())) {
            throw new ResourceConflictException("Ya existe un predio con el identificador indicado");
        }
        Farm farm = farms.save(farmMapper.toEntity(request));
        FarmUser assignment = new FarmUser();
        assignment.setFarm(farm);
        assignment.setUserId(user.id());
        farmUsers.save(assignment);
        return farmMapper.toResponse(farm);
    }

    @Transactional
    public FarmResponse update(String id, FarmUpdateRequest request, UserClientResponse user) {
        requireFarmer(user);
        Farm farm = findAssignedFarm(id, user);
        farmMapper.updateEntity(request, farm);
        return farmMapper.toResponse(farms.save(farm));
    }

    @Transactional
    public void deactivate(String id, UserClientResponse user) {
        requireFarmer(user);
        Farm farm = findAssignedFarm(id, user);
        farm.setActive(false);
        farms.save(farm);
    }

    private Farm findAssignedFarm(String id, UserClientResponse user) {
        if (!farmUsers.existsByFarm_IdAndUserId(id, user.id())) {
            throw new AccessDeniedException("No estás asignado a este predio");
        }
        return farms.findById(id).orElseThrow(() -> new ResourceNotFoundException("Predio no encontrado"));
    }

    private void requireFarmer(UserClientResponse user) {
        if (!"FARMER".equals(user.role())) {
            throw new AccessDeniedException("Solo los responsables pueden gestionar predios");
        }
    }
}
