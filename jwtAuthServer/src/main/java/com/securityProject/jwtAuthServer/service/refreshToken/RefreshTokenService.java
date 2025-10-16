package com.securityProject.jwtAuthServer.service.refreshToken;

import com.securityProject.jwtAuthServer.dto.refresh.RefreshResponse;
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
import com.securityProject.jwtAuthServer.util.CookieUtil;
import com.securityProject.jwtAuthServer.util.TokenExtractor;
import com.securityProject.jwtAuthServer.util.TokenIssuerUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final TokenOwnershipValidator tokenOwnershipValidator;
    private final RefreshTokenRepoService refreshTokenRepoService;
    private final TokenIssuerUtil tokenIssuerUtil;

    @Transactional
    public RefreshResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String oldToken = TokenExtractor.extractFromCookie(request);
        if (oldToken == null) throw new MissingTokenException();

        TokenValidationResult result = tokenOwnershipValidator.validateRefreshToken(oldToken);
        User user = result.user();

        refreshTokenRepoService.revokeToken(result.refreshTokenId());

        String newAccessToken = tokenIssuerUtil.issueTokens(user, request.getRemoteAddr(), response);

        return new RefreshResponse(newAccessToken);
    }
}
