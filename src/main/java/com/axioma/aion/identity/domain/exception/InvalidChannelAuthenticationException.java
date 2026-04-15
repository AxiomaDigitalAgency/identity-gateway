package com.axioma.aion.identity.domain.exception;

import org.springframework.http.HttpStatus;

public class InvalidChannelAuthenticationException extends IdentityException {

    public InvalidChannelAuthenticationException(String message) {
        super("INVALID_CHANNEL_AUTHENTICATION", message, HttpStatus.UNAUTHORIZED);
    }
}