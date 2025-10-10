package com.securityProject.jwtAuthServer.dto.refresh;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshResponse {
    private String accessToken;
    private String refreshToken;
}
