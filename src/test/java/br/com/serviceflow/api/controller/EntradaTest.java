package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EntradaTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private static final String PERIODO = "?dataInicial=2026-10-01&dataFinal=2026-10-31";

    @Test
    void entradaSomaNaReceitaESegueACategoria() throws Exception {
        String a = novaEmpresa("Empresa A", "ent-a");
        String b = novaEmpresa("Empresa B", "ent-b");

        mvc.perform(get("/api/v2/categorias?tipo=ENTRADA").header("Authorization", a))
                .andExpect(jsonPath("$", hasSize(4))).andExpect(jsonPath("$[*].name", hasItem("Comissão")));

        long id = json.readTree(send(post("/api/v2/entradas"), a,
                "{\"description\":\"Venda de cera\",\"category\":\"Venda de produto\",\"amount\":40.50,\"date\":\"2026-10-16\"}")
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asLong();
        send(post("/api/v2/entradas"), a, "{\"description\":\"\",\"category\":\"Outros\",\"amount\":0,\"date\":\"2026-10-16\"}")
                .andExpect(status().isUnprocessableEntity());

        // Receita realizada e lucro incluem a entrada; ticket médio não (não há atendimento).
        mvc.perform(get("/api/v2/financeiro/resumo" + PERIODO).header("Authorization", a))
                .andExpect(jsonPath("$.realized", is(40.5))).andExpect(jsonPath("$.profit", is(40.5)))
                .andExpect(jsonPath("$.averageTicket", is(0)));

        send(put("/api/v2/entradas/" + id), a,
                "{\"description\":\"Venda de cera\",\"category\":\"Venda de produto\",\"amount\":60,\"date\":\"2026-10-16\"}")
                .andExpect(jsonPath("$.amount", is(60)));

        // Renomear a categoria de entrada leva junto o lançamento; excluir em uso é bloqueado.
        JsonNode cats = json.readTree(mvc.perform(get("/api/v2/categorias?tipo=ENTRADA").header("Authorization", a))
                .andReturn().getResponse().getContentAsString());
        long venda = 0;
        for (JsonNode c : cats) if (c.get("name").asText().equals("Venda de produto")) venda = c.get("id").asLong();
        send(put("/api/v2/categorias/" + venda), a, "{\"name\":\"Vendas\",\"type\":\"ENTRADA\"}").andExpect(status().isOk());
        mvc.perform(get("/api/v2/entradas" + PERIODO).header("Authorization", a))
                .andExpect(jsonPath("$[0].category", is("Vendas")));
        mvc.perform(delete("/api/v2/categorias/" + venda).header("Authorization", a))
                .andExpect(status().isUnprocessableEntity());

        // Outra empresa não vê nem altera.
        mvc.perform(get("/api/v2/entradas" + PERIODO).header("Authorization", b)).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(delete("/api/v2/entradas/" + id).header("Authorization", b)).andExpect(status().isNotFound());

        mvc.perform(delete("/api/v2/entradas/" + id).header("Authorization", a)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v2/entradas" + PERIODO).header("Authorization", a)).andExpect(jsonPath("$", hasSize(0)));
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
}
