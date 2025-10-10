package com.securityProject.jwtAuthServer.service.logout;

import com.securityProject.jwtAuthServer.service.refreshToken.RefreshTokenRepoService;
import com.securityProject.jwtAuthServer.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService  {


    private final RefreshTokenRepoService refreshTokenRepoService;
    public void logout(HttpServletRequest request) {
        String jwt = TokenExtractor.extractToken(request);
        if (jwt == null) {
            log.warn("Logout request missing or invalid Authorization header");
            return;
        }

        refreshTokenRepoService.findByRawToken(jwt).ifPresentOrElse(token -> {
            token.setRevoked(true);
            refreshTokenRepoService.save(token);
            log.info("Revoked refresh token for logout: {}", token.getId());
        }, () -> log.warn("Attempted logout with unknown refresh token"));
    }


}