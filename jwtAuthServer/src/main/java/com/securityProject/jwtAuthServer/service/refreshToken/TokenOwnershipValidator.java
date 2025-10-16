package com.securityProject.jwtAuthServer.service.refreshToken;

import com.securityProject.jwtAuthServer.dto.refresh.TokenValidationResult;
import com.securityProject.jwtAuthServer.entity.RefreshToken;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.enums.TokenType;
import com.securityProject.jwtAuthServer.exception.api.*;
import com.securityProject.jwtAuthServer.repository.UserRepository;
import com.securityProject.jwtAuthServer.service.jwt.core.JwtService;
import com.securityProject.jwtAuthServer.util.TokenHashUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenOwnershipValidator {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepoService refreshTokenRepoService;

    public TokenValidationResult validateRefreshToken(String rawToken) {
        if (rawToken == null) throw new MissingTokenException();
        String hashedRawToken = TokenHashUtil.hash(rawToken);
        Claims claims = jwtService.extractAllClaims(rawToken, TokenType.REFRESH);
        String email = claims.getSubject();
        Long refreshTokenId = Long.parseLong(claims.getId());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        RefreshToken stored = refreshTokenRepoService.findById(refreshTokenId)
                .orElseThrow(RefreshTokenRevokedException::new);

        if (stored.isRevoked())
            throw new RefreshTokenRevokedException();

        if (!jwtService.isTokenValid(rawToken, user, TokenType.REFRESH))
            throw new TokenExpiredException();

        if (!hashedRawToken.equals(stored.getTokenHash()))
            throw new InvalidTokenException("Token signature mismatch");

        if (!stored.getUser().getId().equals(user.getId()))
            throw new SecurityException("Token ownership mismatch");

        return new TokenValidationResult(user, stored, refreshTokenId);
    }
}
