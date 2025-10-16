package com.securityProject.jwtAuthServer.dto.login;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {
    private String accessToken;
    private String message;
}