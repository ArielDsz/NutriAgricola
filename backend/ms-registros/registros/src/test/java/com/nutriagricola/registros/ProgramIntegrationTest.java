package com.nutriagricola.registros;

import com.nutriagricola.registros.client.*;
import com.nutriagricola.registros.model.*;
import com.nutriagricola.registros.repository.*;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class ProgramIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired FarmRepository farms;
    @Autowired FarmUserRepository assignments;
    @Autowired CropRepository crops;
    @Autowired ProgramFertilizerRepository fertilizers;
    @Autowired ProgramStageRepository stages;
    @MockitoBean UsersClient usersClient;

    @Test
    void createsRetriesEditsAndDeactivatesManualProgram() throws Exception {
        String advisor = UUID.randomUUID().toString(); auth(advisor, "ADVISOR");
        Crop crop = crop(advisor);
        String id = UUID.randomUUID().toString(); String body = body(id, crop.getId());
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.advisorId").value(advisor))
                .andExpect(jsonPath("$.content.nutrients.phosphorusOxide").value(5.0));
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("Manual de prueba", "Diferente"))).andExpect(status().isConflict());
        mvc.perform(get("/api/v1/programs").param("cropId", crop.getId()).param("size", "1").header("Authorization", "Bearer demo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Manual de prueba"))
                .andExpect(jsonPath("$.content[0].fertilizers").doesNotExist());
        mvc.perform(put("/api/v1/programs/" + id).header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("Manual de prueba", "Actualizado").replace("\"doseKgHa\":10", "\"doseKgHa\":15")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.fertilizers[0].doseKgHa").value(15.0));
        assertEquals(1, fertilizers.findByProgram_IdOrderByItemOrderAsc(id).size());
        assertEquals(1, stages.findByProgram_IdOrderByItemOrderAsc(id).size());
        mvc.perform(delete("/api/v1/programs/" + id).header("Authorization", "Bearer demo")).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/programs/" + id).header("Authorization", "Bearer demo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.content.fertilizers.length()").value(1));
    }

    @Test
    void enforcesAssignmentAndAuthorWithoutHidingSharedReadAccess() throws Exception {
        String author = UUID.randomUUID().toString(); auth(author, "ADVISOR"); Crop crop = crop(author);
        String id = UUID.randomUUID().toString(); String body = body(id, crop.getId());
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        String other = UUID.randomUUID().toString(); assign(crop.getFarm(), other);
        auth(other, "FARMER");
        mvc.perform(get("/api/v1/programs/" + id).header("Authorization", "Bearer demo")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        auth(other, "ADVISOR");
        mvc.perform(put("/api/v1/programs/" + id).header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/programs/" + id).header("Authorization", "Bearer demo")).andExpect(status().isForbidden());
        auth(UUID.randomUUID().toString(), "ADVISOR");
        mvc.perform(get("/api/v1/programs/" + id).header("Authorization", "Bearer demo")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/programs").header("Authorization", "Bearer demo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void validatesManualInputsAndRejectsInactiveCrop() throws Exception {
        String author = UUID.randomUUID().toString(); auth(author, "ADVISOR"); Crop crop = crop(author);
        String body = body(UUID.randomUUID().toString(), crop.getId());
        for (String invalid : java.util.List.of(body.replace("2026-12-01", "2026-01-01"),
                body.replace("\"doseKgHa\":10", "\"doseKgHa\":-1"),
                body.replace("\"nitrogen\":20", "\"nitrogen\":1e999"))) {
            mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest());
        }
        crop.setActive(false); crops.saveAndFlush(crop);
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    private void auth(String id, String role) {
        when(usersClient.getCurrentUser("Bearer demo")).thenReturn(new UserClientResponse(id, role, true, "Usuario", "Prueba"));
    }
    private Crop crop(String user) {
        Farm farm = new Farm(); farm.setId(UUID.randomUUID().toString()); farm.setName("Predio"); farm.setLocation("Chile");
        farm = farms.save(farm); assign(farm, user);
        Crop crop = new Crop(); crop.setId(UUID.randomUUID().toString()); crop.setFarm(farm); crop.setName("Uva");
        return crops.save(crop);
    }
    private void assign(Farm farm, String user) {
        FarmUser a = new FarmUser(); a.setFarm(farm); a.setUserId(user); assignments.save(a);
    }
    private String body(String id, String crop) {
        return """
                {"id":"%s","cropId":"%s","content":{"name":"Manual de prueba","season":"2026",
                 "startDate":"2026-11-01","endDate":"2026-12-01","targetYieldTonHa":10,
                 "irrigationEfficiencyPercent":90,"nutrients":{"nitrogen":20,"phosphorusOxide":5},
                 "irrigationNotes":"Valores ficticios, sin cálculo automático",
                 "fertilizers":[{"name":"Producto de prueba","doseKgHa":10,"notes":"Dosis manual"}],
                 "stages":[{"name":"Brotación","weeks":4,"irrigationsPerWeek":3,"instructions":"Indicación manual"}]}}
                """.formatted(id, crop);
    }
}
