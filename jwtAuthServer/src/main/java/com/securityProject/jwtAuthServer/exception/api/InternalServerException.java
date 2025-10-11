package com.securityProject.jwtAuthServer.exception.api;

import com.securityProject.jwtAuthServer.exception.api.base.ApiException;
import org.springframework.http.HttpStatus;

public class InternalServerException extends ApiException {
    public InternalServerException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}