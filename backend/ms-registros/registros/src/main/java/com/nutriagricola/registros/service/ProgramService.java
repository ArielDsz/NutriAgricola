package com.nutriagricola.registros.service;

import com.nutriagricola.registros.client.UserClientResponse;
import java.util.Objects;
import com.nutriagricola.registros.dto.*;
import com.nutriagricola.registros.exception.*;
import com.nutriagricola.registros.mapper.ProgramMapper;
import com.nutriagricola.registros.model.*;
import com.nutriagricola.registros.repository.*;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgramService {
    private final ProgramRepository programs;
    private final ProgramFertilizerRepository fertilizers;
    private final ProgramStageRepository stages;
    private final ProgramMapper mapper;
    private final EntityManager entityManager;

    public ProgramService(ProgramRepository programs, ProgramFertilizerRepository fertilizers,
                          ProgramStageRepository stages,
                          ProgramMapper mapper, EntityManager entityManager) {
        this.programs = programs;
        this.fertilizers = fertilizers;
        this.stages = stages;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public ProgramPageResponse findAll(String cropId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidRecordException("page debe ser mayor o igual a 0 y size debe estar entre 1 y 100");
        }
        var result = programs.findPrograms(cropId,
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("startDate"), Sort.Order.asc("id"))));
        return new ProgramPageResponse(result.getContent().stream().map(mapper::toSummary).toList(),
                page, size, result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProgramResponse findById(String id) {
        return response(findProgram(id));
    }

    @Transactional
    public ProgramCreationResult create(ProgramRequest request, UserClientResponse user) {
        requireAdvisor(user);
        validate(request.content());
        Program existing = programs.findById(request.id()).orElse(null);
        if (existing != null) {
            requireAuthor(existing, user);
            ProgramResponse saved = response(existing);
            if (!Objects.equals(request.species(), saved.species())
                    || !Objects.equals(request.plot(), saved.plot()) || !request.content().equals(saved.content())) {
                throw new ResourceConflictException("El programa ya existe con datos diferentes");
            }
            return new ProgramCreationResult(saved, false);
        }
        Program program = new Program();
        program.setId(request.id());
        program.setSpecies(request.species());
        program.setPlot(request.plot());
        program.setAdvisorId(user.id());
        program.setAdvisorName(user.firstName() + " " + user.lastName());
        mapper.updateContent(request.content(), program);
        entityManager.persist(program);
        saveItems(program, request.content());
        return new ProgramCreationResult(response(program), true);
    }

    @Transactional
    public ProgramResponse update(String id, ProgramRequest request, UserClientResponse user) {
        requireAdvisor(user);
        Program program = findProgram(id);
        requireAuthor(program, user);
        if (!id.equals(request.id())) {
            throw new ResourceConflictException("No puedes cambiar el identificador del programa");
        }
        validate(request.content());
        if (Objects.equals(request.species(), program.getSpecies())
                && Objects.equals(request.plot(), program.getPlot()) && request.content().equals(response(program).content())) {
            return response(program);
        }
        program.setSpecies(request.species());
        program.setPlot(request.plot());
        mapper.updateContent(request.content(), program);
        fertilizers.deleteItems(id);
        stages.deleteItems(id);
        saveItems(program, request.content());
        programs.save(program);
        return response(program);
    }

    @Transactional
    public void deactivate(String id, UserClientResponse user) {
        requireAdvisor(user);
        Program program = findProgram(id);
        requireAuthor(program, user);
        program.setActive(false);
        programs.save(program);
    }

    private Program findProgram(String id) {
        return programs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Programa no encontrado"));
    }

    private void requireAdvisor(UserClientResponse user) {
        if (!"ADVISOR".equals(user.role())) {
            throw new AccessDeniedException("Solo los asesores pueden gestionar programas");
        }
    }

    private void requireAuthor(Program program, UserClientResponse user) {
        if (!program.getAdvisorId().equals(user.id())) {
            throw new AccessDeniedException("Solo el asesor autor puede modificar o reenviar este programa");
        }
    }

    private ProgramResponse response(Program program) {
        return mapper.toResponse(program, fertilizers.findByProgram_IdOrderByItemOrderAsc(program.getId()),
                stages.findByProgram_IdOrderByItemOrderAsc(program.getId()));
    }

    private void saveItems(Program program, ProgramContent content) {
        for (int index = 0; index < content.fertilizers().size(); index++) {
            var item = content.fertilizers().get(index);
            ProgramFertilizer fertilizer = new ProgramFertilizer();
            fertilizer.setProgram(program);
            fertilizer.setItemOrder(index);
            fertilizer.setName(item.name());
            fertilizer.setDoseKgHa(item.doseKgHa());
            fertilizer.setNotes(item.notes());
            fertilizers.save(fertilizer);
        }
        for (int index = 0; index < content.stages().size(); index++) {
            var item = content.stages().get(index);
            ProgramStage stage = new ProgramStage();
            stage.setProgram(program);
            stage.setItemOrder(index);
            stage.setName(item.name());
            stage.setWeeks(item.weeks());
            stage.setIrrigationsPerWeek(item.irrigationsPerWeek());
            stage.setInstructions(item.instructions());
            stages.save(stage);
        }
    }

    private void validate(ProgramContent content) {
        if (content.endDate() != null && content.endDate().isBefore(content.startDate())) {
            throw new InvalidRecordException("La fecha final no puede ser anterior a la inicial");
        }
        var nutrients = content.nutrients();
        for (Double value : new Double[]{content.targetYieldTonHa(), content.irrigationEfficiencyPercent(),
                nutrients.nitrogen(), nutrients.phosphorusOxide(), nutrients.potassiumOxide(),
                nutrients.calciumOxide(), nutrients.magnesiumOxide(), nutrients.sulfur()}) {
            if (value != null && (!Double.isFinite(value) || value < 0)) {
                throw new InvalidRecordException("Los valores del programa deben ser finitos y no negativos");
            }
        }
        for (var fertilizer : content.fertilizers()) {
            if (!Double.isFinite(fertilizer.doseKgHa())) {
                throw new InvalidRecordException("Las dosis deben ser números finitos");
            }
        }
    }
}
