package com.securityProject.jwtAuthServer.service.login;

import com.securityProject.jwtAuthServer.dto.login.LoginRequest;
import com.securityProject.jwtAuthServer.dto.login.LoginResponse;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.enums.TokenType;
import com.securityProject.jwtAuthServer.exception.api.EmailNotVerifiedException;
import com.securityProject.jwtAuthServer.exception.api.InvalidCredentialsException;
import com.securityProject.jwtAuthServer.exception.api.UserNotFoundException;
import com.securityProject.jwtAuthServer.repository.UserRepository;
import com.securityProject.jwtAuthServer.service.jwt.core.JwtService;
import com.securityProject.jwtAuthServer.service.refreshToken.RefreshTokenRepoService;
import com.securityProject.jwtAuthServer.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepoService refreshTokenRepoService;

    public LoginResponse login(LoginRequest request, String deviceIp, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        validateCredentials(user, request);

        String accessToken  = jwtService.generateToken(user, TokenType.ACCESS);
        String refreshToken = jwtService.generateToken(user, TokenType.REFRESH);

        refreshTokenRepoService.createAndSave(user, refreshToken, deviceIp);
        CookieUtil.addRefreshToCookie(response, refreshToken);

        return new LoginResponse(accessToken, refreshToken, "Login successful");
    }

    private void validateCredentials(User user, LoginRequest req) {
        if (!user.isEmailVerified()) throw new EmailNotVerifiedException();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new InvalidCredentialsException();
    }

}
