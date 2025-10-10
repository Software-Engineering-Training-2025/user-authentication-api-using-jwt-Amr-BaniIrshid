package com.securityProject.jwtAuthServer.filter;

import com.securityProject.jwtAuthServer.service.logout.LogoutService;
import com.securityProject.jwtAuthServer.util.TokenExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogoutFilter extends OncePerRequestFilter {

    private final LogoutService logoutService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        if (!path.startsWith("/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setContentType("application/json");

        String jwt = TokenExtractor.extractToken(request);
        if (jwt == null) {
            log.warn("Logout attempted without Authorization header");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Missing token in Authorization header\"}");
            return;
        }

        try {
            logoutService.logout(request);
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Authorization", "");
            response.getWriter().write("{\"message\": \"Logout successful\"}");
            log.info("User successfully logged out and token revoked.");
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Logout failed. Please try again later.\"}");
        }
    }
}
