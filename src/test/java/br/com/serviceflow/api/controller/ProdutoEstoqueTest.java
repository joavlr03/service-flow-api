package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class ProdutoEstoqueTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void vendaBaixaEstoqueAoFinalizarEDevolveAoCancelar() throws Exception {
        String a = novaEmpresa("Loja A", "loja-a");
        String b = novaEmpresa("Loja B", "loja-b");

        send(post("/api/v2/empresa/onboarding"), a, """
                {"name":"Loja A","segment":"Varejo","usageMode":"VENDAS","usesVehicle":true,"usesDuration":false,
                 "products":[{"name":"Cera","price":10,"stock":5}]}""").andExpect(status().isOk());
        mvc.perform(get("/api/v2/empresa").header("Authorization", a)).andExpect(jsonPath("$.usageMode", is("VENDAS")))
                .andExpect(jsonPath("$.usesVehicle", is(false)));

        long cera = json.readTree(mvc.perform(get("/api/v2/produtos").header("Authorization", a))
                .andReturn().getResponse().getContentAsString()).get(0).get("id").asLong();
        send(post("/api/v2/produtos"), a, "{\"name\":\"cera\",\"price\":1,\"stock\":1}")
                .andExpect(status().isUnprocessableEntity());

        long cliente = id(send(post("/api/v2/clientes"), a, "{\"name\":\"Ana\",\"phone\":\"11999999999\"}"));
        long veiculo = id(send(post("/api/v2/veiculos"), a,
                "{\"clientId\":" + cliente + ",\"brand\":\"Fiat\",\"model\":\"Uno\"}"));
        String base = "\"clientId\":" + cliente + ",\"vehicleId\":" + veiculo
                + ",\"date\":\"2026-10-16\",\"time\":\"10:00\",\"price\":0";

        send(post("/api/v2/ordens-servico"), a, "{" + base + "}").andExpect(status().isUnprocessableEntity());

        String venda1 = send(post("/api/v2/ordens-servico"), a,
                "{" + base + ",\"items\":[{\"productId\":" + cera + ",\"quantity\":3}]}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.serviceId", nullValue()))
                .andExpect(jsonPath("$.price", is(30.0))).andExpect(jsonPath("$.items[0].name", is("Cera")))
                .andReturn().getResponse().getContentAsString();
        long v1 = json.readTree(venda1).get("id").asLong();

        // Agendada não mexe no estoque; finalizar baixa.
        mvc.perform(get("/api/v2/produtos").header("Authorization", a)).andExpect(jsonPath("$[0].stock", is(5)));
        mudarStatus(a, v1, "FINALIZADO").andExpect(status().isOk());
        mvc.perform(get("/api/v2/produtos").header("Authorization", a)).andExpect(jsonPath("$[0].stock", is(2)));

        long v2 = id(send(post("/api/v2/ordens-servico"), a,
                "{" + base + ",\"items\":[{\"productId\":" + cera + ",\"quantity\":3}]}"));
        mudarStatus(a, v2, "FINALIZADO").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("Estoque insuficiente de Cera")));

        // Venda finalizada não troca produtos; cancelar devolve; excluir produto vendido é bloqueado.
        send(put("/api/v2/ordens-servico/" + v1), a,
                "{" + base + ",\"items\":[{\"productId\":" + cera + ",\"quantity\":1}]}")
                .andExpect(status().isUnprocessableEntity());
        mudarStatus(a, v1, "CANCELADO").andExpect(status().isOk());
        mvc.perform(get("/api/v2/produtos").header("Authorization", a)).andExpect(jsonPath("$[0].stock", is(5)));
        mvc.perform(delete("/api/v2/produtos/" + cera).header("Authorization", a))
                .andExpect(status().isUnprocessableEntity());

        // Excluir uma venda finalizada devolve o estoque.
        mudarStatus(a, v2, "FINALIZADO").andExpect(status().isOk());
        mvc.perform(delete("/api/v2/ordens-servico/" + v2).header("Authorization", a)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v2/produtos").header("Authorization", a)).andExpect(jsonPath("$[0].stock", is(5)));

        // Outra empresa não vê nem usa o produto.
        mvc.perform(get("/api/v2/produtos").header("Authorization", b)).andExpect(jsonPath("$", hasSize(0)));
        send(put("/api/v2/produtos/" + cera), b, "{\"name\":\"X\",\"price\":1,\"stock\":99}")
                .andExpect(status().isNotFound());
    }

    private ResultActions mudarStatus(String token, long id, String s) throws Exception {
        return send(patch("/api/v2/ordens-servico/" + id + "/status"), token, "{\"status\":\"" + s + "\"}");
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
