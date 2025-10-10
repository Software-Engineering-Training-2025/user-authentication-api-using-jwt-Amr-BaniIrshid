package com.securityProject.jwtAuthServer.service.email;

import com.securityProject.jwtAuthServer.entity.EmailVerificationToken;
import com.securityProject.jwtAuthServer.entity.User;
import com.securityProject.jwtAuthServer.exception.api.InvalidVerificationTokenException;
import com.securityProject.jwtAuthServer.exception.api.VerificationTokenExpiredException;
import com.securityProject.jwtAuthServer.repository.EmailVerificationTokenRepository;
import com.securityProject.jwtAuthServer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;

    public String verify(String token) {
        EmailVerificationToken verificationToken= tokenRepository.findByToken(token)
                .orElseThrow(InvalidVerificationTokenException::new);

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new VerificationTokenExpiredException();
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);

        return "Email verified successfully!";
    }
}
