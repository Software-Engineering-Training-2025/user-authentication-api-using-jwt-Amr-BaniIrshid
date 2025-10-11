package com.securityProject.jwtAuthServer.service.refreshToken;

import com.securityProject.jwtAuthServer.dto.refresh.TokenValidationResult;
import com.securityProject.jwtAuthServer.entity.RefreshToken;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.enums.TokenType;
import com.securityProject.jwtAuthServer.exception.api.MissingTokenException;
import com.securityProject.jwtAuthServer.exception.api.RefreshTokenRevokedException;
import com.securityProject.jwtAuthServer.exception.api.TokenExpiredException;
import com.securityProject.jwtAuthServer.exception.api.UserNotFoundException;
import com.securityProject.jwtAuthServer.repository.UserRepository;
import com.securityProject.jwtAuthServer.service.jwt.core.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenOwnershipValidator {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepoService refreshTokenRepoService;

    public TokenValidationResult validateRefreshToken(String rawToken) {
        if (rawToken == null) throw new MissingTokenException();

        var claims = jwtService.extractAllClaims(rawToken, TokenType.REFRESH);
        String email = claims.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (!jwtService.isTokenValid(rawToken, user, TokenType.REFRESH))
            throw new TokenExpiredException();

        RefreshToken stored = refreshTokenRepoService.findByRawToken(rawToken)
                .orElseThrow(RefreshTokenRevokedException::new);

        if (stored.isRevoked())
            throw new RefreshTokenRevokedException();

        if (!stored.getUser().getId().equals(user.getId()))
            throw new SecurityException("Token ownership mismatch");

        return new TokenValidationResult(user, stored);    }
}
