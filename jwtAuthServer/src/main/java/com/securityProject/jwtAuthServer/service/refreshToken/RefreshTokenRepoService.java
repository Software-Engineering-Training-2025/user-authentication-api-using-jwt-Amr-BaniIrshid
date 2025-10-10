package com.securityProject.jwtAuthServer.service.refreshToken;

import com.securityProject.jwtAuthServer.entity.RefreshToken;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.repository.RefreshTokenRepository;
import com.securityProject.jwtAuthServer.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenRepoService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMillis;

    public RefreshToken createAndSave(User user, String rawRefreshToken, String deviceIp) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(TokenHashUtil.hash(rawRefreshToken))
                .deviceIp(deviceIp)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshExpirationMillis))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    public boolean isRevoked(String rawToken) {
        return findByRawToken(rawToken)
                .map(RefreshToken::isRevoked)
                .orElse(true);
    }

    public void revokeToken(String rawToken) {
        findByRawToken(rawToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    public Optional<RefreshToken> findByRawToken(String rawToken) {
        String hash = TokenHashUtil.hash(rawToken);
        return refreshTokenRepository.findByTokenHash(hash);
    }

    public void save(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }
}
