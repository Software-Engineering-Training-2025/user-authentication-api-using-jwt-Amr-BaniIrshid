package com.securityProject.jwtAuthServer.filter;

import com.securityProject.jwtAuthServer.service.logout.LogoutService;
import com.securityProject.jwtAuthServer.util.CookieUtil;
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
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getServletPath().equals("/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setContentType("application/json");

        try {
            logoutService.logout(request);

            SecurityContextHolder.clearContext();
            response.setHeader("Authorization", "");
            CookieUtil.clearCookie(response, "refresh_token");

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\":\"Logout successful\"}");

            log.info("Logout successful for path {}", request.getServletPath());
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
