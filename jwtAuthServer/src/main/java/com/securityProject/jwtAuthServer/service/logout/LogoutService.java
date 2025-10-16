package com.securityProject.jwtAuthServer.service.logout;

import com.securityProject.jwtAuthServer.dto.refresh.TokenValidationResult;
import com.securityProject.jwtAuthServer.entity.RefreshToken;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.enums.TokenType;
import com.securityProject.jwtAuthServer.exception.api.MissingTokenException;
import com.securityProject.jwtAuthServer.exception.api.TokenExpiredException;
import com.securityProject.jwtAuthServer.exception.api.UserNotFoundException;
import com.securityProject.jwtAuthServer.repository.UserRepository;
import com.securityProject.jwtAuthServer.service.jwt.core.JwtService;
import com.securityProject.jwtAuthServer.service.refreshToken.RefreshTokenRepoService;
import com.securityProject.jwtAuthServer.service.refreshToken.TokenOwnershipValidator;
import com.securityProject.jwtAuthServer.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {

    private final TokenOwnershipValidator tokenOwnershipValidator;
    private final RefreshTokenRepoService refreshTokenRepoService;

    public void logout(HttpServletRequest request) {
        String oldToken = TokenExtractor.extractFromCookie(request);
        TokenValidationResult result = tokenOwnershipValidator.validateRefreshToken(oldToken);

        User user = result.user();
        RefreshToken stored = result.refreshToken();

        if (!stored.isRevoked()) {
            stored.setRevoked(true);
            refreshTokenRepoService.save(stored);
            log.info("Revoked refresh token {} for user {}", stored.getId(), user.getEmail());
        } else {
            log.warn("Logout called with already revoked token {}", stored.getId());
        }
    }
}