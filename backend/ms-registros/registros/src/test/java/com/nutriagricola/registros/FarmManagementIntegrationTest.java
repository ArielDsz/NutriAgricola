package com.nutriagricola.registros;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.client.UsersClient;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.Farm;
import com.nutriagricola.registros.model.FarmUser;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.repository.CropRepository;
import com.nutriagricola.registros.repository.FarmRepository;
import com.nutriagricola.registros.repository.FarmUserRepository;
import com.nutriagricola.registros.repository.RecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FarmManagementIntegrationTest {
    private static final String AUTHORIZATION = "Bearer token-de-prueba";

    @Autowired private MockMvc mvc;
    @Autowired private FarmRepository farms;
    @Autowired private FarmUserRepository assignments;
    @Autowired private CropRepository crops;
    @Autowired private RecordRepository records;

    @MockitoBean
    private UsersClient usersClient;

    @Test
    void createsFarmAssignsCreatorAndAllowsEditingWithoutDuplicateCreation() throws Exception {
        UserClientResponse farmer = authenticate("FARMER");
        String id = UUID.randomUUID().toString();

        mvc.perform(post("/api/v1/farms").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(createBody(id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Predio de prueba"))
                .andExpect(jsonPath("$.active").value(true));
        assignments.flush();
        assertTrue(assignments.existsByFarm_IdAndUserId(id, farmer.id()));

        mvc.perform(get("/api/v1/farms").header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(put("/api/v1/farms/" + id).header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nombre actualizado\",\"location\":\"Maule\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nombre actualizado"));
        mvc.perform(post("/api/v1/farms").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(createBody(id)))
                .andExpect(status().isConflict());
    }

    @Test
    void deactivatesWithoutDeletingHistoryAndPreventsNewRecords() throws Exception {
        UserClientResponse farmer = authenticate("FARMER");
        Farm farm = assignedFarm(farmer.id());
        Crop crop = new Crop();
        crop.setId(UUID.randomUUID().toString());
        crop.setFarm(farm);
        crop.setName("Uva de mesa");
        crop = crops.save(crop);
        Record record = new Record();
        record.setId(UUID.randomUUID().toString());
        record.setCrop(crop);
        record.setUserId(farmer.id());
        record.setDate("2026-11-19");
        record.setType("SOIL");
        record.setStatus("PENDING");
        record = records.saveAndFlush(record);

        mvc.perform(delete("/api/v1/farms/" + farm.getId()).header("Authorization", AUTHORIZATION))
                .andExpect(status().isNoContent());
        farms.flush();
        assertFalse(farms.findById(farm.getId()).orElseThrow().getActive());
        assertTrue(crops.existsById(crop.getId()));
        assertTrue(records.existsById(record.getId()));
        mvc.perform(get("/api/v1/farms/" + farm.getId()).header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mvc.perform(get("/api/v1/records/status/PENDING").header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(record.getId()));

        String rejectedId = UUID.randomUUID().toString();
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"id":"%s","cropId":"%s","date":"2026-11-19","type":"SOIL",
                                 "soilData":{"ph":6.5}}
                                """.formatted(rejectedId, crop.getId())))
                .andExpect(status().isConflict());
        assertFalse(records.existsById(rejectedId));
    }

    @Test
    void allowsAssignedAdvisorToReadButRestrictsWritesAndUnassignedUsers() throws Exception {
        UserClientResponse advisor = authenticate("ADVISOR");
        Farm farm = assignedFarm(advisor.id());
        String path = "/api/v1/farms/" + farm.getId();

        mvc.perform(get(path).header("Authorization", AUTHORIZATION)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/farms").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(createBody(UUID.randomUUID().toString())))
                .andExpect(status().isForbidden());
        mvc.perform(put(path).header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cambio\",\"location\":\"Chile\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete(path).header("Authorization", AUTHORIZATION)).andExpect(status().isForbidden());

        authenticate("FARMER");
        mvc.perform(get(path).header("Authorization", AUTHORIZATION)).andExpect(status().isForbidden());
        mvc.perform(put(path).header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cambio\",\"location\":\"Chile\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete(path).header("Authorization", AUTHORIZATION)).andExpect(status().isForbidden());
        assertTrue(farms.findById(farm.getId()).orElseThrow().getActive());
    }

    @Test
    void rejectsInvalidUuidAndBlankName() throws Exception {
        authenticate("FARMER");
        mvc.perform(post("/api/v1/farms").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"no-es-un-uuid\",\"name\":\" \",\"location\":\"Chile\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").exists())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    private UserClientResponse authenticate(String role) {
        UserClientResponse user = new UserClientResponse(UUID.randomUUID().toString(), role,
                true, "Usuario", "Prueba");
        when(usersClient.getCurrentUser(AUTHORIZATION)).thenReturn(user);
        return user;
    }

    private Farm assignedFarm(String userId) {
        Farm farm = new Farm();
        farm.setId(UUID.randomUUID().toString());
        farm.setName("Predio de prueba");
        farm.setLocation("Chile");
        farm = farms.save(farm);
        FarmUser assignment = new FarmUser();
        assignment.setFarm(farm);
        assignment.setUserId(userId);
        assignments.save(assignment);
        return farm;
    }

    private String createBody(String id) {
        return """
                {"id":"%s","name":"  Predio de prueba  ","location":"Chile"}
                """.formatted(id);
    }
}
