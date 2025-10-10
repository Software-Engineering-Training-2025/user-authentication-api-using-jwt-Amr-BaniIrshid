package com.securityProject.jwtAuthServer.exception.api;

import com.securityProject.jwtAuthServer.exception.api.base.ApiException;
import org.springframework.http.HttpStatus;

public class RefreshTokenRevokedException extends ApiException {
    public RefreshTokenRevokedException() {
        super("Refresh token has been revoked.", HttpStatus.UNAUTHORIZED);
    }
}
