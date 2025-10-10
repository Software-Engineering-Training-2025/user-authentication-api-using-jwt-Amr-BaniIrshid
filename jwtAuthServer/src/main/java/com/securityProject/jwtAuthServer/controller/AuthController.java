package com.securityProject.jwtAuthServer.controller;

import com.securityProject.jwtAuthServer.dto.login.LoginRequest;
import com.securityProject.jwtAuthServer.dto.login.LoginResponse;
import com.securityProject.jwtAuthServer.dto.refresh.RefreshResponse;
import com.securityProject.jwtAuthServer.dto.register.RegisterRequest;
import com.securityProject.jwtAuthServer.dto.register.RegisterResponse;
import com.securityProject.jwtAuthServer.service.auth.AuthFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        authFacade.register(request.getEmail(), request.getUsername(),request.getPassword(), request.getRole());
        return ResponseEntity.ok(new RegisterResponse("Registration successful! Please check your email for verification link."));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {
        String message = authFacade.verifyEmail(token);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest http,
            HttpServletResponse response
    ) {
        String deviceIp = http.getRemoteAddr();
        return ResponseEntity.ok(authFacade.login(request, deviceIp, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        RefreshResponse tokens = authFacade.refreshToken(request, response);
        return ResponseEntity.ok(tokens);
    }

}
