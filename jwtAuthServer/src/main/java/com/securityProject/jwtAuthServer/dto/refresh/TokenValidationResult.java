package com.securityProject.jwtAuthServer.dto.refresh;

import com.securityProject.jwtAuthServer.entity.RefreshToken;
import com.securityProject.jwtAuthServer.entity.User;

public record TokenValidationResult(User user, RefreshToken refreshToken) {}
