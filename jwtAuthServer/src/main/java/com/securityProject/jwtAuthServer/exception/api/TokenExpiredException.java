package com.securityProject.jwtAuthServer.exception.api;

import com.securityProject.jwtAuthServer.exception.api.base.ApiException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends ApiException {
    public TokenExpiredException() {
        super("Token has expired or is invalid.", HttpStatus.UNAUTHORIZED);
    }
}