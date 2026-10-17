package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class OnboardingTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void empresaNovaConcluiOPrimeiroAcessoUmaVez() throws Exception {
        String a = novaEmpresa("Barbearia", "barba");

        // Empresa nova: assistente pendente e nenhum serviço semeado.
        mvc.perform(get("/api/v2/empresa").header("Authorization", a))
                .andExpect(jsonPath("$.onboardingDone", is(false)));
        mvc.perform(get("/api/v2/servicos").header("Authorization", a)).andExpect(jsonPath("$", hasSize(0)));

        // Modo serviços sem serviço é recusado e nada é gravado.
        onboarding(a, """
                {"name":"Barbearia do Zé","segment":"Salão / barbearia","usageMode":"SERVICOS",
                 "usesVehicle":false,"usesDuration":false,"services":[]}""")
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/v2/empresa").header("Authorization", a))
                .andExpect(jsonPath("$.onboardingDone", is(false)));

        onboarding(a, """
                {"name":"Barbearia do Zé","segment":"Salão / barbearia","usageMode":"SERVICOS",
                 "usesVehicle":false,"usesDuration":false,"themeColor":"#2563eb","appIcon":"tesoura",
                 "services":[{"name":"Corte","price":50},{"name":"Barba","price":35}]}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboardingDone", is(true))).andExpect(jsonPath("$.name", is("Barbearia do Zé")))
                .andExpect(jsonPath("$.usesDuration", is(false))).andExpect(jsonPath("$.themeColor", is("#2563EB")))
                .andExpect(jsonPath("$.appIcon", is("tesoura")));
        mvc.perform(get("/api/v2/servicos").header("Authorization", a))
                .andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].durationMin", is(60)));

        // Ícone fora da lista é recusado (só vale antes de concluir, então testa em outra empresa).
        onboarding(novaEmpresa("Outra", "outra-icone"), """
                {"name":"X","segment":"Outro","usageMode":"SERVICOS","usesVehicle":true,"usesDuration":true,
                 "appIcon":"foguete","services":[{"name":"Y","price":1}]}""").andExpect(status().isUnprocessableEntity());

        // Não roda de novo.
        onboarding(a, """
                {"name":"X","segment":"Outro","usageMode":"SERVICOS","usesVehicle":true,"usesDuration":true,
                 "services":[{"name":"Y","price":1}]}""").andExpect(status().isConflict());
    }

    private ResultActions onboarding(String token, String body) throws Exception {
        return mvc.perform(post("/api/v2/empresa/onboarding").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
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
}
