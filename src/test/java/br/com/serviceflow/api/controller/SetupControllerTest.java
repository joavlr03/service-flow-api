package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.test-data.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:setup;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SetupControllerTest {
    @Autowired MockMvc mvc;
    private static final String BODY = """
            {"companyName":"Lava Rápido Central","segment":"Estética Automotiva",
             "ownerName":"João Silva","email":"joao-central",
             "password":"SenhaForte@2026","inviteKey":"test-invite-key"}
            """;
    private static final String BODY_SECOND_COMPANY = """
            {"companyName":"Lava Rápido Norte","segment":"Estética Automotiva",
             "ownerName":"Maria Souza","email":"maria-norte",
             "password":"SenhaForte@2026","inviteKey":"test-invite-key"}
            """;

    @Test
    void shouldSetupAndAllowLogin() throws Exception {
        mvc.perform(get("/api/v2/setup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(true)));
        mvc.perform(post("/api/v2/setup").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role", is("ADMIN")));
        mvc.perform(post("/api/v2/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"joao-central\",\"password\":\"SenhaForte@2026\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v2/setup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void shouldAllowSetupOfSecondCompanyWithValidInviteKey() throws Exception {
        mvc.perform(post("/api/v2/setup").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v2/setup").contentType(MediaType.APPLICATION_JSON).content(BODY_SECOND_COMPANY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    void shouldRejectSetupWithInvalidInviteKey() throws Exception {
        String badBody = BODY.replace("test-invite-key", "chave-errada");
        mvc.perform(post("/api/v2/setup").contentType(MediaType.APPLICATION_JSON).content(badBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")));
    }

    @Test
    void shouldValidateSetupPayload() throws Exception {
        mvc.perform(post("/api/v2/setup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyName\":\"\",\"password\":\"curta\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }
}
