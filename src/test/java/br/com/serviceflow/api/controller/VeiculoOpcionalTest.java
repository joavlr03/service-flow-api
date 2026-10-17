package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VeiculoOpcionalTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void veiculoSoEObrigatorioParaQuemUsa() throws Exception {
        String a = novaEmpresa("Salão", "salao");
        mvc.perform(get("/api/v2/empresa").header("Authorization", a)).andExpect(jsonPath("$.usesVehicle", is(true)));

        long cliente = id(send(post("/api/v2/clientes"), a, "{\"name\":\"Bia\",\"phone\":\"11988887777\"}"));
        long servico = id(send(post("/api/v2/servicos"), a,
                "{\"name\":\"Corte\",\"price\":50,\"durationMin\":30,\"active\":true}"));
        String semVeiculo = "{\"clientId\":" + cliente + ",\"serviceId\":" + servico
                + ",\"date\":\"2026-10-16\",\"time\":\"10:00\",\"price\":50}";

        // Padrão (usa veículo): continua exigindo.
        send(post("/api/v2/ordens-servico"), a, semVeiculo).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", is("Informe o veículo")));

        send(post("/api/v2/empresa/onboarding"), a, """
                {"name":"Salão","segment":"Salão / barbearia","usageMode":"SERVICOS","usesVehicle":false,
                 "usesDuration":true,"services":[{"name":"Escova","price":60,"durationMin":45}]}""")
                .andExpect(status().isOk()).andExpect(jsonPath("$.usesVehicle", is(false)));

        long os = id(send(post("/api/v2/ordens-servico"), a, semVeiculo));
        mvc.perform(get("/api/v2/ordens-servico/" + os).header("Authorization", a))
                .andExpect(jsonPath("$.vehicleId", nullValue()));
        send(put("/api/v2/ordens-servico/" + os), a, semVeiculo.replace("\"price\":50", "\"price\":60"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.price", is(60)));
    }

    private String novaEmpresa(String nome, String email) throws Exception {
        mvc.perform(post("/api/v2/setup").contentType(MediaType.APPLICATION_JSON).content("""
                {"companyName":"%s","segment":"Teste","ownerName":"Dono","email":"%s",
                 "password":"SenhaForte@2026","inviteKey":"test-invite-key"}""".formatted(nome, email)))
                .andExpect(status().is2xxSuccessful());
        String login = mvc.perform(post("/api/v2/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"SenhaForte@2026\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(login).get("accessToken").asText();
    }

    private ResultActions send(MockHttpServletRequestBuilder req, String token, String body) throws Exception {
        return mvc.perform(req.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private long id(ResultActions r) throws Exception {
        return json.readTree(r.andExpect(status().isOk()).andReturn().getResponse().getContentAsString())
                .get("id").asLong();
    }
}
