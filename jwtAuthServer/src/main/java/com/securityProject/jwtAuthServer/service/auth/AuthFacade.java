package com.securityProject.jwtAuthServer.service.auth;

import com.securityProject.jwtAuthServer.dto.login.LoginRequest;
import com.securityProject.jwtAuthServer.dto.login.LoginResponse;
import com.securityProject.jwtAuthServer.dto.refresh.RefreshResponse;
import com.securityProject.jwtAuthServer.enums.Role;
import com.securityProject.jwtAuthServer.service.email.EmailVerificationService;
import com.securityProject.jwtAuthServer.service.login.LoginService;
import com.securityProject.jwtAuthServer.service.refreshToken.RefreshTokenService;
import com.securityProject.jwtAuthServer.service.register.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFacade {

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final EmailVerificationService verificationService;
    private final RefreshTokenService tokenRefreshService;

    public void register(String email, String username, String password, Role role) {
        registrationService.register(email, username, password, role);
    }

    public LoginResponse login(LoginRequest req, String deviceIp, HttpServletResponse response) {
        return loginService.login(req, deviceIp, response);
    }

    public String verifyEmail(String token) {
        return verificationService.verify(token);
    }

    public RefreshResponse refreshToken(HttpServletRequest req, HttpServletResponse res) {
        return tokenRefreshService.refresh(req, res);
    }
}