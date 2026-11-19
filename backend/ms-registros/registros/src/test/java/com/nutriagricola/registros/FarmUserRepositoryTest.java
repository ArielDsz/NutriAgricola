package com.nutriagricola.registros;

import com.nutriagricola.registros.model.Farm;
import com.nutriagricola.registros.model.FarmUser;
import com.nutriagricola.registros.repository.FarmRepository;
import com.nutriagricola.registros.repository.FarmUserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class FarmUserRepositoryTest {
    @Autowired
    private FarmRepository farms;

    @Autowired
    private FarmUserRepository assignments;

    @Autowired
    private EntityManager entityManager;

    @Test
    void allowsSeveralUsersPerFarmAndSeveralFarmsPerUser() {
        String farmerId = UUID.randomUUID().toString();
        String advisorId = UUID.randomUUID().toString();
        Farm firstFarm = saveFarm("Predio A");
        Farm secondFarm = saveFarm("Predio B");
        Farm otherFarm = saveFarm("Predio ajeno");

        assign(firstFarm, farmerId);
        assign(firstFarm, advisorId);
        assign(secondFarm, farmerId);
        assign(otherFarm, UUID.randomUUID().toString());
        assignments.flush();
        entityManager.clear();

        assertEquals(List.of(firstFarm.getId(), secondFarm.getId()),
                assignments.findFarmsByUserId(farmerId).stream().map(Farm::getId).toList());
        assertEquals(List.of(firstFarm.getId()),
                assignments.findFarmsByUserId(advisorId).stream().map(Farm::getId).toList());
        assertTrue(assignments.existsByFarm_IdAndUserId(firstFarm.getId(), advisorId));
        assertFalse(assignments.existsByFarm_IdAndUserId(otherFarm.getId(), farmerId));
    }

    @Test
    void returnsNoFarmsWhenUserHasNoAssignments() {
        saveFarm("Predio sin asignar");

        assertTrue(assignments.findFarmsByUserId(UUID.randomUUID().toString()).isEmpty());
    }

    @Test
    void rejectsDuplicateAssignmentForTheSameFarmAndUser() {
        Farm farm = saveFarm("Predio A");
        String userId = UUID.randomUUID().toString();
        assign(farm, userId);
        assignments.flush();

        FarmUser duplicate = new FarmUser();
        duplicate.setFarm(farm);
        duplicate.setUserId(userId);

        assertThrows(DataIntegrityViolationException.class,
                () -> assignments.saveAndFlush(duplicate));
    }

    private Farm saveFarm(String name) {
        Farm farm = new Farm();
        farm.setId(UUID.randomUUID().toString());
        farm.setName(name);
        farm.setLocation("Chile");
        return farms.save(farm);
    }

    private void assign(Farm farm, String userId) {
        FarmUser assignment = new FarmUser();
        assignment.setFarm(farm);
        assignment.setUserId(userId);
        assignments.save(assignment);
    }
}
