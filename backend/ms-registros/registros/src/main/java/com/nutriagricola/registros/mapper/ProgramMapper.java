package com.nutriagricola.registros.mapper;
import com.nutriagricola.registros.dto.*;
import com.nutriagricola.registros.model.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ProgramMapper {
    public void updateContent(ProgramContent content, Program program) {
        program.setName(content.name());
        program.setSeason(content.season());
        program.setStartDate(content.startDate());
        program.setEndDate(content.endDate());
        program.setTargetYieldTonHa(content.targetYieldTonHa());
        program.setIrrigationEfficiencyPercent(content.irrigationEfficiencyPercent());
        program.setNitrogen(content.nutrients().nitrogen());
        program.setPhosphorusOxide(content.nutrients().phosphorusOxide());
        program.setPotassiumOxide(content.nutrients().potassiumOxide());
        program.setCalciumOxide(content.nutrients().calciumOxide());
        program.setMagnesiumOxide(content.nutrients().magnesiumOxide());
        program.setSulfur(content.nutrients().sulfur());
        program.setIrrigationNotes(content.irrigationNotes());
    }

    public ProgramSummaryResponse toSummary(Program p) {
        return new ProgramSummaryResponse(p.getId(), p.getCrop() == null ? null : p.getCrop().getId(), p.getAdvisorId(),
                p.getName(), p.getSeason(), p.getStartDate(), p.getEndDate(), p.getActive(),
                p.getSpecies(), p.getPlot());
    }

    public ProgramResponse toResponse(Program p, List<ProgramFertilizer> fertilizers, List<ProgramStage> stages) {
        var nutrients = new ProgramContent.Nutrients(p.getNitrogen(), p.getPhosphorusOxide(), p.getPotassiumOxide(),
                p.getCalciumOxide(), p.getMagnesiumOxide(), p.getSulfur());
        var fertilizerItems = fertilizers.stream()
                .map(f -> new ProgramContent.Fertilizer(f.getName(), f.getDoseKgHa(), f.getNotes(),
                        f.getNitrogen() == null ? null : new ProgramContent.Nutrients(f.getNitrogen(),
                                f.getPhosphorusOxide(), f.getPotassiumOxide(), f.getCalciumOxide(),
                                f.getMagnesiumOxide(), f.getSulfur()))).toList();
        var stageItems = stages.stream()
                .map(s -> new ProgramContent.Stage(s.getName(), s.getWeeks(), s.getIrrigationsPerWeek(), s.getInstructions())).toList();
        var content = new ProgramContent(p.getName(), p.getSeason(), p.getStartDate(), p.getEndDate(),
                p.getTargetYieldTonHa(), p.getIrrigationEfficiencyPercent(), nutrients, p.getIrrigationNotes(),
                fertilizerItems, stageItems);
        return new ProgramResponse(p.getId(), p.getCrop() == null ? null : p.getCrop().getId(), p.getAdvisorId(), p.getAdvisorName(),
                content, p.getActive(), p.getCreatedAt(), p.getSpecies(), p.getPlot());
    }
}
