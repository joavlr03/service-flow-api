package br.com.serviceflow.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {
    private static final Set<String> ALLOWED = Set.of(
            "/api/v2/auth/me", "/api/v2/auth/alterar-senha", "/api/v2/auth/logout");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token
                && Boolean.TRUE.equals(token.getToken().getClaim("passwordChangeRequired"))
                && !ALLOWED.contains(request.getRequestURI())) {
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"PASSWORD_CHANGE_REQUIRED\",\"message\":\"Altere a senha antes de continuar\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
