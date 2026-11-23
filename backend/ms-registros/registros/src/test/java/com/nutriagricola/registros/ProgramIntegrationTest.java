package com.nutriagricola.registros;

import com.nutriagricola.registros.client.*;
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
    @Autowired ProgramFertilizerRepository fertilizers;
    @Autowired ProgramStageRepository stages;
    @MockitoBean UsersClient usersClient;

    @Test
    void createsRetriesEditsAndDeactivatesManualProgram() throws Exception {
        String advisor = UUID.randomUUID().toString(); auth(advisor, "ADVISOR");
        String id = UUID.randomUUID().toString(); String body = body(id);
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.advisorId").value(advisor))
                .andExpect(jsonPath("$.cropId").isEmpty()).andExpect(jsonPath("$.farmerId").doesNotExist())
                .andExpect(jsonPath("$.species").value("Uva de mesa"))
                .andExpect(jsonPath("$.content.nutrients.phosphorusOxide").value(5.0));
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("Manual de prueba", "Diferente"))).andExpect(status().isConflict());
        mvc.perform(get("/api/v1/programs").param("size", "1").header("Authorization", "Bearer demo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Manual de prueba"))
                .andExpect(jsonPath("$.content[0].advisorName").value("Usuario Prueba"))
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
    void allAuthenticatedUsersCanReadButOnlyAdvisorAuthorCanWrite() throws Exception {
        String author = UUID.randomUUID().toString(); auth(author, "ADVISOR");
        String id = UUID.randomUUID().toString(); String body = body(id);
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        auth(UUID.randomUUID().toString(), "FARMER");
        mvc.perform(get("/api/v1/programs/" + id).header("Authorization", "Bearer demo")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/programs").header("Authorization", "Bearer demo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/programs/" + id).header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/programs/" + id).header("Authorization", "Bearer demo")).andExpect(status().isForbidden());
        auth(UUID.randomUUID().toString(), "ADVISOR");
        mvc.perform(get("/api/v1/programs/" + id).header("Authorization", "Bearer demo")).andExpect(status().isOk());
        mvc.perform(put("/api/v1/programs/" + id).header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/programs/" + id).header("Authorization", "Bearer demo")).andExpect(status().isForbidden());
        auth(UUID.randomUUID().toString(), "FARMER");
        mvc.perform(get("/api/v1/programs/" + id).header("Authorization", "Bearer demo")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/programs").header("Authorization", "Bearer demo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void validatesProgramInputsWithoutRequiringFarmsOrCrops() throws Exception {
        String author = UUID.randomUUID().toString(); auth(author, "ADVISOR");
        mvc.perform(get("/api/v1/programs")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/programs").param("size", "101").header("Authorization", "Bearer demo"))
                .andExpect(status().isBadRequest());
        String body = body(UUID.randomUUID().toString());
        for (String invalid : java.util.List.of(body.replace("2026-12-01", "2026-01-01"),
                body.replace("\"doseKgHa\":10", "\"doseKgHa\":-1"),
                body.replace("\"nitrogen\":20", "\"nitrogen\":1e999"))) {
            mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    private void auth(String id, String role) {
        when(usersClient.getCurrentUser("Bearer demo")).thenReturn(new UserClientResponse(id, role, true, "Usuario", "Prueba"));
    }

    @Test
    void savesCompositionSnapshotAndRejectsInvalidPercentages() throws Exception {
        auth(UUID.randomUUID().toString(), "ADVISOR");
        String id = UUID.randomUUID().toString();
        String body = body(id).replace("\"notes\":\"Dosis manual\"", """
                "notes":"Dosis manual","composition":{"nitrogen":12,"phosphorusOxide":60,
                "potassiumOxide":0,"calciumOxide":0,"magnesiumOxide":0,"sulfur":0}
                """.strip());
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.content.fertilizers[0].composition.nitrogen").value(12.0));
        mvc.perform(get("/api/v1/programs/" + id).header("Authorization", "Bearer demo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.fertilizers[0].composition.phosphorusOxide").value(60.0));
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON)
                .content(body.replace(id, UUID.randomUUID().toString()).replace("\"nitrogen\":12", "\"nitrogen\":101")))
                .andExpect(status().isBadRequest());
    }
    @Test
    void preservesStageDistributionAndRejectsIncompleteOrIncorrectTotals() throws Exception {
        auth(UUID.randomUUID().toString(), "ADVISOR");
        String id = UUID.randomUUID().toString();
        String body = body(id).replace("\"instructions\":\"Indicación manual\"", "\"instructions\":\"Indicación manual\",\"allocationPercent\":100");
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.content.stages[0].allocationPercent").value(100.0));
        mvc.perform(get("/api/v1/programs/" + id).header("Authorization", "Bearer demo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.stages[0].allocationPercent").value(100.0));
        mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        for (String invalid : java.util.List.of(body.replace("\"allocationPercent\":100", "\"allocationPercent\":99"),
                body.replace("\"allocationPercent\":100", "\"allocationPercent\":101"),
                body.replace("\"stages\":[", "\"stages\":[{\"name\":\"Sin distribución\",\"weeks\":1},"))) {
            mvc.perform(post("/api/v1/programs").header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON)
                    .content(invalid.replace(id, UUID.randomUUID().toString()))).andExpect(status().isBadRequest());
        }
    }

    private String body(String id) {
        return """
                {"id":"%s","species":"Uva de mesa","plot":"Cuartel 1","content":{"name":"Manual de prueba","season":"2026",
                 "startDate":"2026-11-01","endDate":"2026-12-01","targetYieldTonHa":10,
                 "irrigationEfficiencyPercent":90,"nutrients":{"nitrogen":20,"phosphorusOxide":5},
                 "irrigationNotes":"Valores ficticios, sin cálculo automático",
                 "fertilizers":[{"name":"Producto de prueba","doseKgHa":10,"notes":"Dosis manual"}],
                 "stages":[{"name":"Brotación","weeks":4,"irrigationsPerWeek":3,"instructions":"Indicación manual"}]}}
                """.formatted(id);
    }
}
