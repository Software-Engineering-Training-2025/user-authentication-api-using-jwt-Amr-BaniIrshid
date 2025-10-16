package com.securityProject.jwtAuthServer.service.login;

import com.securityProject.jwtAuthServer.dto.login.LoginRequest;
import com.securityProject.jwtAuthServer.dto.login.LoginResponse;
import com.securityProject.jwtAuthServer.entity.RefreshToken;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.enums.TokenType;
import com.securityProject.jwtAuthServer.exception.api.EmailNotVerifiedException;
import com.securityProject.jwtAuthServer.exception.api.InvalidCredentialsException;
import com.securityProject.jwtAuthServer.exception.api.UserNotFoundException;
import com.securityProject.jwtAuthServer.repository.UserRepository;
import com.securityProject.jwtAuthServer.service.jwt.core.JwtService;
import com.securityProject.jwtAuthServer.service.refreshToken.RefreshTokenRepoService;
import com.securityProject.jwtAuthServer.util.CookieUtil;
import com.securityProject.jwtAuthServer.util.TokenIssuerUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuerUtil tokenIssuerUtil;

    @Transactional
    public LoginResponse login(LoginRequest request, String deviceIp, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        validateCredentials(user, request);

        String accessToken = tokenIssuerUtil.issueTokens(user, deviceIp, response);

        return new LoginResponse(accessToken, "Login successful");
    }

    private void validateCredentials(User user, LoginRequest req) {
        if (!user.isEmailVerified()) throw new EmailNotVerifiedException();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new InvalidCredentialsException();
    }
}
