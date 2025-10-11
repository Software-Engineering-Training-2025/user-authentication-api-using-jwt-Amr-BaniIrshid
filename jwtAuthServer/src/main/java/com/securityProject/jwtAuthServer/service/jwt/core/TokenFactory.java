package com.securityProject.jwtAuthServer.service.jwt.core;

import com.securityProject.jwtAuthServer.enums.TokenType;
import com.securityProject.jwtAuthServer.service.jwt.strategy.AccessTokenStrategy;
import com.securityProject.jwtAuthServer.service.jwt.strategy.RefreshTokenStrategy;
import com.securityProject.jwtAuthServer.service.jwt.strategy.TokenStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenFactory {

    private final AccessTokenStrategy accessTokenStrategy;
    private final RefreshTokenStrategy refreshTokenStrategy;

    public TokenStrategy getStrategy(TokenType type) {
        return switch (type) {
            case ACCESS -> accessTokenStrategy;
            case REFRESH -> refreshTokenStrategy;
        };
    }
}