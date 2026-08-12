package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.bootstrap.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:setup;MODE=MySQL;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
class SetupControllerTest {
    @Autowired MockMvc mvc;
    private static final String BODY = """
            {"companyName":"Lava Rápido Central","segment":"Estética Automotiva",
             "ownerName":"João Silva","email":"joao@central.com.br",
             "password":"SenhaForte@2026","plan":"OPERACIONAL_FINANCEIRO"}
            """;

    @Test
    void shouldSetupOnceAndAllowLogin() throws Exception {
        mvc.perform(post("/api/v2/setup").header("X-Setup-Key", "test-setup-key-with-at-least-32-characters").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role", is("ADMIN")));
        mvc.perform(post("/api/v2/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"joao@central.com.br\",\"password\":\"SenhaForte@2026\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v2/setup").header("X-Setup-Key", "test-setup-key-with-at-least-32-characters").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("SETUP_ALREADY_COMPLETED")));
    }

    @Test
    void shouldRejectSetupWithoutProvisioningKey() throws Exception {
        mvc.perform(post("/api/v2/setup").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v2/setup").header("X-Setup-Key", "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
    }
}
