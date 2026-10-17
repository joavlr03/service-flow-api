package br.com.serviceflow.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Sem @Transactional: o e-mail só sai depois do commit do cadastro.
@SpringBootTest(properties = {"app.mail.enabled=true", "spring.mail.username=teste@serviceflow.local"})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WelcomeMailTest {
    @Autowired MockMvc mvc;
    @MockitoBean JavaMailSender mail;

    @BeforeEach
    void mimeReal() {
        when(mail.createMimeMessage()).thenAnswer(i -> new MimeMessage(Session.getInstance(new Properties())));
    }

    private void cadastrar(String email) throws Exception {
        mvc.perform(post("/api/v2/setup").contentType(MediaType.APPLICATION_JSON).content("""
                {"companyName":"Barbearia do Zé","segment":"Beleza","ownerName":"José Silva","email":"%s",
                 "password":"SenhaForte@2026","inviteKey":"test-invite-key"}""".formatted(email)))
                .andExpect(status().isCreated());
    }

    @Test
    void enviaBoasVindasParaOEmailCadastrado() throws Exception {
        cadastrar("ze@barbearia.com");
        ArgumentCaptor<MimeMessage> sent = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mail, timeout(3000)).send(sent.capture());
        MimeMessage m = sent.getValue();
        assertEquals("ze@barbearia.com", m.getAllRecipients()[0].toString());
        assertEquals("Bem-vindo ao ServiceFlow, Barbearia do Zé!", m.getSubject());
        m.saveChanges();
        String todo = conteudo(m);
        assertTrue(todo.contains("Olá, José!") && todo.contains("Parabéns por instalar"), todo);
    }

    @Test
    void identificadorSemEmailNaoRecebe() throws Exception {
        cadastrar("ze-barbearia");
        verify(mail, after(800).never()).send(any(MimeMessage.class));
    }

    @Test
    void falhaNoEnvioNaoDerrubaOCadastro() throws Exception {
        doThrow(new MailSendException("SMTP fora")).when(mail).send(any(MimeMessage.class));
        cadastrar("ze@barbearia.com"); // continua 201
        verify(mail, timeout(3000)).send(any(MimeMessage.class));
    }

    // Junta o texto de todas as partes (texto puro e HTML) da mensagem.
    private static String conteudo(jakarta.mail.Part p) throws Exception {
        Object c = p.getContent();
        if (c instanceof String s) return s;
        if (c instanceof MimeMultipart mp) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < mp.getCount(); i++) b.append(conteudo(mp.getBodyPart(i)));
            return b.toString();
        }
        return "";
    }
}
