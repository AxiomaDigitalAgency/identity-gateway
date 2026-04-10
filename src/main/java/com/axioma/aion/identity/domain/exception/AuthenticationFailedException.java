package com.axioma.aion.identity.domain.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationFailedException extends IdentityException {

    public AuthenticationFailedException(String message) {
        super("AUTHENTICATION_FAILED", message, HttpStatus.UNAUTHORIZED);
    }
}