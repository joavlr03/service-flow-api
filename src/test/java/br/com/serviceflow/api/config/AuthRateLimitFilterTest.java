package br.com.serviceflow.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

// Em produção todo pedido chega do mesmo proxy do Google; o limite tem que separar os usuários.
class AuthRateLimitFilterTest {
    private final AuthRateLimitFilter filter = new AuthRateLimitFilter(2, 15);

    private int post(String path, String forwardedFor) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", path);
        req.setRemoteAddr("169.254.1.1"); // proxy, igual para todos
        if (forwardedFor != null) req.addHeader("X-Forwarded-For", forwardedFor);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        return res.getStatus();
    }

    @Test
    void separaUsuariosAtrasDoMesmoProxy() throws Exception {
        assertEquals(200, post("/api/v2/auth/login", "200.1.1.1"));
        assertEquals(200, post("/api/v2/auth/login", "200.1.1.1"));
        assertEquals(429, post("/api/v2/auth/login", "200.1.1.1"));
        // Outro usuário não é afetado.
        assertEquals(200, post("/api/v2/auth/login", "200.2.2.2"));
        // Forjar o começo do X-Forwarded-For não escapa: vale o último valor, que o Google acrescenta.
        assertEquals(429, post("/api/v2/auth/login", "9.9.9.9, 200.1.1.1"));
    }

    @Test
    void refreshNaoEntraNoLimite() throws Exception {
        for (int i = 0; i < 10; i++) assertEquals(200, post("/api/v2/auth/refresh", "200.3.3.3"));
    }
}
