package br.com.serviceflow.api.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

@SpringBootTest
@AutoConfigureMockMvc
class EmpresaTemaTest {
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

    private void putTema(String token, String body, int expected) throws Exception {
        mvc.perform(put("/api/v2/empresa/tema").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expected));
    }

    @Test
    void adminAlteraCorEValidaFormato() throws Exception {
        String admin = auth("admin@brilhototal.com.br", "AdminTest@123");
        putTema(admin, "{\"color\":\"#2563eb\"}", 200);
        mvc.perform(get("/api/v2/empresa").header("Authorization", admin))
                .andExpect(jsonPath("$.themeColor", is("#2563EB")));

        putTema(admin, "{\"color\":\"azul\"}", 422);
        putTema(admin, "{\"color\":\"#12345\"}", 422);
        putTema(admin, "{}", 422);
    }

    @Test
    void usuarioQueNaoEAdminNaoAltera() throws Exception {
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
        String func = auth("func@brilhototal.com.br", "FuncTest@1234");
        putTema(func, "{\"color\":\"#16A34A\"}", 403);
        mvc.perform(get("/api/v2/empresa").header("Authorization", func)).andExpect(status().isOk());
    }
}
