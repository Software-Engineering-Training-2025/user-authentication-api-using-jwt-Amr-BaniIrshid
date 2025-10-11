package com.securityProject.jwtAuthServer.exception.api;

import com.securityProject.jwtAuthServer.exception.api.base.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends ApiException {
    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

