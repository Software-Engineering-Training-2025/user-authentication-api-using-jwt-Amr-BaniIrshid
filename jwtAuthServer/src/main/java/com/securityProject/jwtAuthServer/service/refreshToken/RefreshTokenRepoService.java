package com.securityProject.jwtAuthServer.service.refreshToken;

import com.securityProject.jwtAuthServer.entity.RefreshToken;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.enums.TokenType;
import com.securityProject.jwtAuthServer.repository.RefreshTokenRepository;
import com.securityProject.jwtAuthServer.service.jwt.core.JwtService;
import com.securityProject.jwtAuthServer.service.jwt.strategy.RefreshTokenStrategy;
import com.securityProject.jwtAuthServer.service.jwt.strategy.TokenStrategy;
import com.securityProject.jwtAuthServer.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenRepoService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;



    public RefreshToken createAndSave(User user, String deviceIp ){
        RefreshToken saved = refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .deviceIp(deviceIp)
                        .createdAt(Instant.now())
                        .expiresAt(Instant.now().plusMillis(jwtService.getExpiration(TokenType.REFRESH)))
                        .revoked(false)
                        .build()
        );

        return refreshTokenRepository.save(saved);
    }

    public boolean isRevoked(Long Id) {
        return findById(Id)
                .map(RefreshToken::isRevoked)
                .orElse(true);
    }

    public void revokeToken(Long Id) {
        findById(Id).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    public Optional<RefreshToken> findById(Long id) {
        return refreshTokenRepository.findById(id);
    }




    public void save(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }


}
