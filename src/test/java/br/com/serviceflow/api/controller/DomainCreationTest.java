package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
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
@Transactional
class DomainCreationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void shouldPersistClientOrderAndExpense() throws Exception {
        String token = authenticate();

        long clientId = id(postJson("/api/v2/clientes", token,
                "{\"name\":\"Cliente Teste\",\"phone\":\"11999999999\",\"whatsapp\":\"11999999999\"}"));
        long vehicleId = id(postJson("/api/v2/veiculos", token,
                "{\"clientId\":" + clientId + ",\"brand\":\"Fiat\",\"model\":\"Uno\",\"plate\":\"ABC1D23\",\"color\":\"Prata\"}"));

        long serviceId = id(postJson("/api/v2/servicos", token,
                "{\"name\":\"Lavagem completa\",\"price\":120.50,\"durationMin\":60,\"active\":true}"));

        postJson("/api/v2/ordens-servico", token,
                "{\"clientId\":" + clientId + ",\"vehicleId\":" + vehicleId + ",\"serviceId\":"
                        + serviceId + ",\"date\":\"2026-08-09\",\"time\":\"09:00\",\"price\":120.50}");
        postJson("/api/v2/despesas", token,
                "{\"description\":\"Produto teste\",\"category\":\"Produtos\",\"amount\":25.50,\"date\":\"2026-08-09\"}");

        mvc.perform(get("/api/v2/clientes").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].name", hasItem("Cliente Teste")));
        mvc.perform(get("/api/v2/ordens-servico?dataInicial=2026-08-09&dataFinal=2026-08-09")
                        .header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].price", is(120.5)));
        mvc.perform(get("/api/v2/despesas?dataInicial=2026-08-09&dataFinal=2026-08-09")
                        .header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].description", is("Produto teste")));
    }

    private String authenticate() throws Exception {
        String response = mvc.perform(post("/api/v2/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@brilhototal.com.br\",\"password\":\"AdminTest@123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(response).get("accessToken").asText();
    }

    private String postJson(String path, String token, String body) throws Exception {
        return mvc.perform(post(path).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private long id(String response) throws Exception {
        JsonNode node = json.readTree(response);
        return node.get("id").asLong();
    }
}
