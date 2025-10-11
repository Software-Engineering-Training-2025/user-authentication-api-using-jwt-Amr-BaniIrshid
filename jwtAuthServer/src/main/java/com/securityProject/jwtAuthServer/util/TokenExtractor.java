package com.securityProject.jwtAuthServer.util;

import jakarta.servlet.http.HttpServletRequest;

public final class TokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    private TokenExtractor() {
    }


    public static String extractToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX.length());
    }

    public static boolean hasBearerToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }
}
