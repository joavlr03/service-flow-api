package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.serviceflow.api.model.Usuario;
import br.com.serviceflow.api.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PagamentoEServicosTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwordEncoder;

    private String auth(String email, String password) throws Exception {
        String r = mvc.perform(post("/api/v2/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(r).get("accessToken").asText();
    }

    private long create(String path, String token, String body) throws Exception {
        String r = mvc.perform(post(path).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(r).get("id").asLong();
    }

    private ResultActions patchStatus(String token, long id, String body) throws Exception {
        return mvc.perform(patch("/api/v2/ordens-servico/" + id + "/status").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void finalizaComFormaDePagamentoEDepoisMarcaComoPago() throws Exception {
        String admin = auth("admin@brilhototal.com.br", "AdminTest@123");
        long client = create("/api/v2/clientes", admin, "{\"name\":\"Cliente Pgto\",\"phone\":\"11988887777\"}");
        long vehicle = create("/api/v2/veiculos", admin,
                "{\"clientId\":" + client + ",\"brand\":\"Fiat\",\"model\":\"Uno\"}");
        long service = create("/api/v2/servicos", admin,
                "{\"name\":\"Lavagem\",\"price\":80,\"durationMin\":40}");
        long order = create("/api/v2/ordens-servico", admin, "{\"clientId\":" + client + ",\"vehicleId\":" + vehicle
                + ",\"serviceId\":" + service + ",\"date\":\"2026-10-13\",\"time\":\"10:00\",\"price\":80}");

        // Finalizado e "fica a receber": sem forma, pendente
        patchStatus(admin, order, "{\"status\":\"FINALIZADO\",\"paid\":false}").andExpect(status().isOk())
                .andExpect(jsonPath("$.paid", is(false))).andExpect(jsonPath("$.paymentMethod", nullValue()));

        patchStatus(admin, order, "{\"status\":\"FINALIZADO\",\"paid\":true}").andExpect(status().is(422));
        patchStatus(admin, order, "{\"status\":\"FINALIZADO\",\"paymentMethod\":\"CHEQUE\",\"paid\":true}")
                .andExpect(status().is(422));

        patchStatus(admin, order, "{\"status\":\"FINALIZADO\",\"paymentMethod\":\"PIX\",\"paid\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid", is(true))).andExpect(jsonPath("$.paymentMethod", is("PIX")));

        // Mudar só o status não apaga o pagamento
        patchStatus(admin, order, "{\"status\":\"AGENDADO\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.paid", is(true))).andExpect(jsonPath("$.paymentMethod", is("PIX")));
    }

    @Test
    void soAdminCriaOuEditaServico() throws Exception {
        Usuario admin = usuarios.findAll().get(0);
        if (usuarios.findAll().stream().noneMatch(u -> u.getEmail().equals("func@brilhototal.com.br"))) {
            Usuario func = new Usuario();
            func.setEmpresa(admin.getEmpresa());
            func.setNome("Funcionário");
            func.setEmail("func@brilhototal.com.br");
            func.setSenhaHash(passwordEncoder.encode("FuncTest@1234"));
            func.setPerfil("USER");
            func.setAtivo(true);
            func.setTrocaSenhaObrigatoria(false);
            func.setCriadoEm(LocalDateTime.now());
            usuarios.save(func);
        }
        String adminToken = auth("admin@brilhototal.com.br", "AdminTest@123");
        String func = auth("func@brilhototal.com.br", "FuncTest@1234");
        String body = "{\"name\":\"Corte\",\"price\":50,\"durationMin\":30,\"active\":true}";
        long service = create("/api/v2/servicos", adminToken, body);

        mvc.perform(post("/api/v2/servicos").header("Authorization", func)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(put("/api/v2/servicos/" + service).header("Authorization", func)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(put("/api/v2/servicos/" + service).header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Corte\",\"price\":65.90,\"durationMin\":30,\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(65.9))).andExpect(jsonPath("$.active", is(false)));
    }
}
