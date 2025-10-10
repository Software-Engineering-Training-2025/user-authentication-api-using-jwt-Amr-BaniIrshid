package com.securityProject.jwtAuthServer.exception.api;

import com.securityProject.jwtAuthServer.exception.api.base.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidVerificationTokenException extends ApiException {

    public InvalidVerificationTokenException() {
        super("Invalid verification token provided.", HttpStatus.BAD_REQUEST);
    }

    public InvalidVerificationTokenException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}