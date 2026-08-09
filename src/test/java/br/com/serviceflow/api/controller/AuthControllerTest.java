package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void shouldLoginAndReadAuthenticatedUser() throws Exception {
        String body = """
                {"email":"admin@brilhototal.com.br","password":"admin123"}
                """;
        String response = mockMvc.perform(post("/api/v2/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.user.email", is("admin@brilhototal.com.br")))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        mockMvc.perform(get("/api/v2/auth/me")
                        .header("Authorization", "Bearer " + json.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    void shouldRejectInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/v2/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@brilhototal.com.br\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")));
    }

    @Test
    @Transactional
    void shouldPersistChangedPassword() throws Exception {
        String login = mockMvc.perform(post("/api/v2/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@brilhototal.com.br\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(login).get("accessToken").asText();

        mockMvc.perform(post("/api/v2/auth/alterar-senha")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"admin123\",\"newPassword\":\"NovaSenha@123\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v2/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@brilhototal.com.br\",\"password\":\"NovaSenha@123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRotateAndRevokeRefreshToken() throws Exception {
        String login = mockMvc.perform(post("/api/v2/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@brilhototal.com.br\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String first = objectMapper.readTree(login).get("refreshToken").asText();
        String refreshed = mockMvc.perform(post("/api/v2/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + first + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String second = objectMapper.readTree(refreshed).get("refreshToken").asText();
        mockMvc.perform(post("/api/v2/auth/logout").header("Authorization", "Bearer " + objectMapper.readTree(refreshed).get("accessToken").asText())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + second + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v2/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + second + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
