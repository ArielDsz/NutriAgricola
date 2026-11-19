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
import com.nutriagricola.registros.repository.RecordReviewRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecordsSecurityIntegrationTest {
    private static final String AUTHORIZATION = "Bearer token-de-prueba";

    @Autowired private MockMvc mvc;
    @Autowired private FarmRepository farms;
    @Autowired private FarmUserRepository assignments;
    @Autowired private CropRepository crops;
    @Autowired private RecordRepository records;
    @Autowired private RecordReviewRepository reviews;

    @MockitoBean
    private UsersClient usersClient;

    @Test
    void rejectsRequestWithoutTokenWithoutCallingUsers() throws Exception {
        mvc.perform(get("/api/v1/farms")).andExpect(status().isUnauthorized());
        verifyNoInteractions(usersClient);
    }

    @Test
    void rejectsTokenRejectedByUsers() throws Exception {
        FeignException rejected = mock(FeignException.class);
        when(rejected.status()).thenReturn(401);
        when(usersClient.getCurrentUser(AUTHORIZATION)).thenThrow(rejected);

        mvc.perform(get("/api/v1/farms").header("Authorization", AUTHORIZATION))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInactiveUser() throws Exception {
        when(usersClient.getCurrentUser(AUTHORIZATION)).thenReturn(
                new UserClientResponse(UUID.randomUUID().toString(), "FARMER", false, "Usuario", "Prueba"));

        mvc.perform(get("/api/v1/farms").header("Authorization", AUTHORIZATION))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reportsUnavailableUsersServiceWithoutAuthorizingRequest() throws Exception {
        FeignException unavailable = mock(FeignException.class);
        when(unavailable.status()).thenReturn(503);
        when(usersClient.getCurrentUser(AUTHORIZATION)).thenThrow(unavailable);

        mvc.perform(get("/api/v1/farms").header("Authorization", AUTHORIZATION))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void filtersFarmsAndRecordsAndRejectsCropsFromAnotherFarm() throws Exception {
        UserClientResponse farmer = authenticate("FARMER");
        Crop assignedCrop = saveCrop("Predio propio");
        Crop otherCrop = saveCrop("Predio ajeno");
        assign(assignedCrop.getFarm(), farmer.id());
        Record visible = saveRecord(assignedCrop);
        saveRecord(otherCrop);

        mvc.perform(get("/api/v1/farms").header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(assignedCrop.getFarm().getId()));
        mvc.perform(get("/api/v1/records/status/PENDING").header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(visible.getId()));
        mvc.perform(get("/api/v1/crops/farm/" + otherCrop.getFarm().getId())
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isForbidden());
    }

    @Test
    void createsPendingRecordWithAuthenticatedAuthorAndRejectsAnotherFarm() throws Exception {
        UserClientResponse farmer = authenticate("FARMER");
        Crop crop = saveCrop("Predio propio");
        assign(crop.getFarm(), farmer.id());
        String recordId = UUID.randomUUID().toString();

        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(recordId, crop.getId())))
                .andExpect(status().isCreated());
        records.flush();
        Record saved = records.findById(recordId).orElseThrow();
        assertEquals(farmer.id(), saved.getUserId());
        assertEquals("PENDING", saved.getStatus());
        verify(usersClient).getCurrentUser(AUTHORIZATION);

        Crop other = saveCrop("Predio ajeno");
        String rejectedId = UUID.randomUUID().toString();
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(rejectedId, other.getId())))
                .andExpect(status().isForbidden());
        assertFalse(records.existsById(rejectedId));
    }

    @Test
    void reviewsAsAssignedAdvisorAndEnforcesFarmAndRolePermissions() throws Exception {
        UserClientResponse advisor = authenticate("ADVISOR");
        Crop crop = saveCrop("Predio asignado");
        assign(crop.getFarm(), advisor.id());
        Record record = saveRecord(crop);
        String reviewId = UUID.randomUUID().toString();

        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(reviewBody(reviewId, record.getId())))
                .andExpect(status().isCreated());
        reviews.flush();
        var saved = reviews.findById(reviewId).orElseThrow();
        assertEquals(advisor.id(), saved.getAdvisorId());
        assertEquals("Usuario Prueba", saved.getAdvisorName());
        assertEquals("VALIDATED", records.findById(record.getId()).orElseThrow().getStatus());

        Record other = saveRecord(saveCrop("Predio ajeno"));
        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewBody(UUID.randomUUID().toString(), other.getId())))
                .andExpect(status().isForbidden());
        assertEquals("PENDING", records.findById(other.getId()).orElseThrow().getStatus());

        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordBody(UUID.randomUUID().toString(), crop.getId())))
                .andExpect(status().isForbidden());
        authenticate("FARMER");
        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewBody(UUID.randomUUID().toString(), record.getId())))
                .andExpect(status().isForbidden());
    }

    private UserClientResponse authenticate(String role) {
        UserClientResponse user = new UserClientResponse(UUID.randomUUID().toString(), role,
                true, "Usuario", "Prueba");
        when(usersClient.getCurrentUser(AUTHORIZATION)).thenReturn(user);
        return user;
    }

    private Crop saveCrop(String farmName) {
        Farm farm = new Farm();
        farm.setId(UUID.randomUUID().toString());
        farm.setName(farmName);
        farm.setLocation("Chile");
        farms.save(farm);
        Crop crop = new Crop();
        crop.setId(UUID.randomUUID().toString());
        crop.setFarm(farm);
        crop.setName("Uva de mesa");
        return crops.save(crop);
    }

    private void assign(Farm farm, String userId) {
        FarmUser assignment = new FarmUser();
        assignment.setFarm(farm);
        assignment.setUserId(userId);
        assignments.save(assignment);
    }

    private Record saveRecord(Crop crop) {
        Record record = new Record();
        record.setId(UUID.randomUUID().toString());
        record.setCrop(crop);
        record.setUserId(UUID.randomUUID().toString());
        record.setDate("2026-11-19");
        record.setType("SOIL");
        record.setStatus("PENDING");
        return records.save(record);
    }

    private String recordBody(String id, String cropId) {
        return """
                {"id":"%s", "cropId":"%s", "date":"2026-11-19", "type":"SOIL",
                 "soilData":{"ph":6.5}}
                """.formatted(id, cropId);
    }

    private String reviewBody(String id, String recordId) {
        return """
                {"id":"%s", "recordId":"%s", "date":"2026-11-19T12:00:00",
                 "result":"VALIDATED", "comment":"Revisión de prueba"}
                """.formatted(id, recordId);
    }
}
