package com.securityProject.jwtAuthServer.service.refreshToken;

import com.securityProject.jwtAuthServer.dto.refresh.RefreshResponse;
import com.securityProject.jwtAuthServer.entity.RefreshToken;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.enums.TokenType;
import com.securityProject.jwtAuthServer.exception.api.MissingTokenException;
import com.securityProject.jwtAuthServer.exception.api.RefreshTokenRevokedException;
import com.securityProject.jwtAuthServer.exception.api.TokenExpiredException;
import com.securityProject.jwtAuthServer.exception.api.UserNotFoundException;
import com.securityProject.jwtAuthServer.repository.UserRepository;
import com.securityProject.jwtAuthServer.service.jwt.core.JwtService;
import com.securityProject.jwtAuthServer.util.CookieUtil;
import com.securityProject.jwtAuthServer.util.TokenExtractor;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepoService refreshTokenService;

    public RefreshResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String oldToken = TokenExtractor.extractToken(request);
        if (oldToken == null) throw new MissingTokenException();

        Claims claims = jwtService.extractAllClaims(oldToken, TokenType.REFRESH);
        String email = claims.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (!jwtService.isTokenValid(oldToken, user, TokenType.REFRESH))
            throw new TokenExpiredException();

        boolean revoked = refreshTokenService.findByRawToken(oldToken)
                .map(RefreshToken::isRevoked)
                .orElse(true);

        if (revoked) throw new RefreshTokenRevokedException();

        refreshTokenService.revokeToken(oldToken);

        String newAccess = jwtService.generateToken(user, TokenType.ACCESS);
        String newRefresh = jwtService.generateToken(user, TokenType.REFRESH);
        refreshTokenService.createAndSave(user, newRefresh, request.getRemoteAddr());
         CookieUtil.addRefreshToCookie(response, newRefresh);
        return new RefreshResponse(newAccess, newRefresh);
    }
}
