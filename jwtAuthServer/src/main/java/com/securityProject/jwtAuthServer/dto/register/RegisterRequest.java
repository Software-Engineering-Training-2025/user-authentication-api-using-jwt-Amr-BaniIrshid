package com.securityProject.jwtAuthServer.dto.register;

import com.securityProject.jwtAuthServer.enums.Role;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private Role role;
}
