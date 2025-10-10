package com.securityProject.jwtAuthServer.exception.api;

import com.securityProject.jwtAuthServer.exception.api.base.ApiException;
import org.springframework.http.HttpStatus;

public class MissingTokenException extends ApiException {

    public MissingTokenException() {
        super("Authorization header is missing or does not contain a valid Bearer token.", HttpStatus.BAD_REQUEST);
    }

    public MissingTokenException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}