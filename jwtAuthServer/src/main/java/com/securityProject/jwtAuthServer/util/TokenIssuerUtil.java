package com.securityProject.jwtAuthServer.util;

import com.securityProject.jwtAuthServer.entity.RefreshToken;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.enums.TokenType;
import com.securityProject.jwtAuthServer.service.jwt.core.JwtService;
import com.securityProject.jwtAuthServer.service.refreshToken.RefreshTokenRepoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenIssuerUtil {

    private final JwtService jwtService;
    private final RefreshTokenRepoService refreshTokenRepoService;
    private final PasswordEncoder passwordEncoder;


    public String issueTokens(User user, String deviceIp, HttpServletResponse response) {
        RefreshToken refreshEntity = refreshTokenRepoService.createAndSave(user, deviceIp);

        String accessToken  = jwtService.generateToken(user, TokenType.ACCESS, null);
        String refreshToken = jwtService.generateToken(user, TokenType.REFRESH, refreshEntity.getId());

            refreshEntity.setTokenHash(TokenHashUtil.hash(refreshToken));
        refreshTokenRepoService.save(refreshEntity);

        CookieUtil.addRefreshToCookie(response, refreshToken);

        return accessToken;
    }
}
