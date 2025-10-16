package com.securityProject.jwtAuthServer.service.register;

import com.securityProject.jwtAuthServer.entity.EmailVerificationToken;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.enums.Role;
import com.securityProject.jwtAuthServer.exception.api.DuplicateEmailException;
import com.securityProject.jwtAuthServer.repository.EmailVerificationTokenRepository;
import com.securityProject.jwtAuthServer.repository.UserRepository;
import com.securityProject.jwtAuthServer.service.email.EmailSenderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSenderService emailSenderService;
    private final EmailVerificationTokenRepository tokenRepository;

    @Transactional
    public void register(String email, String username, String password, Role role) {
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new DuplicateEmailException(email);
        });

        User user = User.builder()
                .email(email)
                .displayname(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        EmailVerificationToken token = createToken(user);
        tokenRepository.save(token);

        String verifyUrl = "http://localhost:8080/auth/verify?token=" + token.getToken();
        emailSenderService.sendVerificationEmail(user.getEmail(), verifyUrl);
    }

    private EmailVerificationToken createToken(User user) {
        return EmailVerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
    }
}