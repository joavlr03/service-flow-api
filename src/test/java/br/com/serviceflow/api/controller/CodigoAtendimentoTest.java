package br.com.serviceflow.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// Bug 001: o código do atendimento colidia entre empresas e depois de excluir um atendimento.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CodigoAtendimentoTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void codigoESequencialPorEmpresaESemColisao() throws Exception {
        String a = novaEmpresa("Empresa A", "dono-a");
        String b = novaEmpresa("Empresa B", "dono-b");

        assertEquals("OS-1001", criarAtendimento(a).get("code").asText());
        assertEquals("OS-1001", criarAtendimento(b).get("code").asText());

        JsonNode segundo = criarAtendimento(a);
        assertEquals("OS-1002", segundo.get("code").asText());
        mvc.perform(delete("/api/v2/ordens-servico/" + segundo.get("id").asLong()).header("Authorization", a))
                .andExpect(status().isNoContent());
        // Antes, contar os atendimentos depois de uma exclusão repetia um código ainda em uso.
        criarAtendimento(a);
        assertEquals("OS-1003", criarAtendimento(a).get("code").asText());
    }

    private String novaEmpresa(String nome, String email) throws Exception {
        mvc.perform(post("/api/v2/setup").contentType(MediaType.APPLICATION_JSON).content("""
                {"companyName":"%s","segment":"Teste","ownerName":"Dono","email":"%s",
                 "password":"SenhaForte@2026","inviteKey":"test-invite-key"}""".formatted(nome, email)))
                .andExpect(status().is2xxSuccessful());
        JsonNode login = json.readTree(postJson("/api/v2/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"SenhaForte@2026\"}"));
        return "Bearer " + login.get("accessToken").asText();
    }

    private JsonNode criarAtendimento(String token) throws Exception {
        long cliente = id(postJson("/api/v2/clientes", token, "{\"name\":\"Cliente\",\"phone\":\"11999999999\"}"));
        long veiculo = id(postJson("/api/v2/veiculos", token,
                "{\"clientId\":" + cliente + ",\"brand\":\"Fiat\",\"model\":\"Uno\"}"));
        long servico = id(postJson("/api/v2/servicos", token,
                "{\"name\":\"Lavagem\",\"price\":10,\"durationMin\":30,\"active\":true}"));
        return json.readTree(postJson("/api/v2/ordens-servico", token,
                "{\"clientId\":" + cliente + ",\"vehicleId\":" + veiculo + ",\"serviceId\":" + servico
                        + ",\"date\":\"2026-10-16\",\"time\":\"09:00\",\"price\":10}"));
    }

    private String postJson(String path, String token, String body) throws Exception {
        var req = post(path).contentType(MediaType.APPLICATION_JSON).content(body);
        if (token != null) req.header("Authorization", token);
        return mvc.perform(req).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private long id(String response) throws Exception {
        return json.readTree(response).get("id").asLong();
    }
}
