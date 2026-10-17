package br.com.serviceflow.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
    // Sem /auth/refresh: ele exige um token aleatório (não há senha para adivinhar) e roda a cada
    // página aberta, então limitá-lo derrubava a sessão de quem só navega.
    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/api/v2/auth/login", "/api/v2/auth/esqueci-minha-senha",
            "/api/v2/auth/redefinir-senha", "/api/v2/setup");
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration windowDuration;

    public AuthRateLimitFilter(@Value("${app.security.rate-limit.auth-attempts:5}") int maxAttempts,
                               @Value("${app.security.rate-limit.window-minutes:15}") long windowMinutes) {
        if (maxAttempts < 1 || windowMinutes < 1) throw new IllegalArgumentException("Rate limit inválido");
        this.maxAttempts = maxAttempts;
        this.windowDuration = Duration.ofMinutes(windowMinutes);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !PROTECTED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = request.getRequestURI() + ":" + clientIp(request);
        Instant now = Instant.now();
        Window window = windows.compute(key, (ignored, current) ->
                current == null || now.isAfter(current.started.plus(windowDuration))
                        ? new Window(now, 1) : new Window(current.started, current.count + 1));
        if (window.count > maxAttempts) {
            long retryAfter = Math.max(1, Duration.between(now, window.started.plus(windowDuration)).toSeconds());
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Muitas tentativas; tente novamente mais tarde\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    // No Cloud Run getRemoteAddr() é o proxy do Google, igual para todos os usuários. O IP real é o
    // último valor do X-Forwarded-For, que o Google acrescenta; os anteriores o cliente pode forjar.
    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) return request.getRemoteAddr();
        String[] hops = forwarded.split(",");
        return hops[hops.length - 1].trim();
    }

    private record Window(Instant started, int count) {}
}
