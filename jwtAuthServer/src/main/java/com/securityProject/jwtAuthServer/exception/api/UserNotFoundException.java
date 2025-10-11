package com.securityProject.jwtAuthServer.exception.api;

import com.securityProject.jwtAuthServer.exception.api.base.ApiException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(String email) {
        super("User with email '" + email + "' not found.", HttpStatus.NOT_FOUND);
    }
}
