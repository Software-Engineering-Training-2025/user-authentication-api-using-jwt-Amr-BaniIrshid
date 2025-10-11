package com.securityProject.jwtAuthServer.dto.login;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRequest {
    private String email;
    private String password;
}
