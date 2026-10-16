package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoriaTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void empresaNovaGerenciaAsPropriasCategorias() throws Exception {
        String a = novaEmpresa("Empresa A", "cat-a");
        String b = novaEmpresa("Empresa B", "cat-b");

        mvc.perform(get("/api/v2/categorias").header("Authorization", a))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[*].name", hasItem("Aluguel")));

        long id = json.readTree(send(post("/api/v2/categorias"), a,
                "{\"name\":\" Marketing \",\"type\":\"DESPESA\"}").andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("id").asLong();
        send(post("/api/v2/categorias"), a, "{\"name\":\"marketing\",\"type\":\"DESPESA\"}")
                .andExpect(status().isUnprocessableEntity());

        // Renomear leva junto as despesas já lançadas.
        send(post("/api/v2/despesas"), a,
                "{\"description\":\"Panfletos\",\"category\":\"Marketing\",\"amount\":50,\"date\":\"2026-10-16\"}")
                .andExpect(status().isOk());
        send(put("/api/v2/categorias/" + id), a, "{\"name\":\"Divulgação\",\"type\":\"DESPESA\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.name", is("Divulgação")));
        mvc.perform(get("/api/v2/despesas?dataInicial=2026-10-16&dataFinal=2026-10-16").header("Authorization", a))
                .andExpect(jsonPath("$[0].category", is("Divulgação")));

        mvc.perform(delete("/api/v2/categorias/" + id).header("Authorization", a))
                .andExpect(status().isUnprocessableEntity());
        send(put("/api/v2/categorias/" + id), a, "{\"name\":\"Divulgação\",\"type\":\"DESPESA\",\"active\":false}")
                .andExpect(jsonPath("$.active", is(false)));

        // Outra empresa não enxerga nem altera.
        mvc.perform(get("/api/v2/categorias").header("Authorization", b))
                .andExpect(jsonPath("$[*].name", not(hasItem("Divulgação"))));
        send(put("/api/v2/categorias/" + id), b, "{\"name\":\"Invasão\",\"type\":\"DESPESA\"}")
                .andExpect(status().isNotFound());

        long livre = json.readTree(send(post("/api/v2/categorias"), a,
                "{\"name\":\"Sem uso\",\"type\":\"DESPESA\"}").andReturn().getResponse().getContentAsString())
                .get("id").asLong();
        mvc.perform(delete("/api/v2/categorias/" + livre).header("Authorization", a))
                .andExpect(status().isNoContent());
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

    private ResultActions send(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder req,
            String token, String body) throws Exception {
        return mvc.perform(req.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
