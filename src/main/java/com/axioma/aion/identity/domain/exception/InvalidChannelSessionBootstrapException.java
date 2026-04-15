package com.axioma.aion.identity.domain.exception;

import org.springframework.http.HttpStatus;

public class InvalidChannelSessionBootstrapException extends IdentityException {

    public InvalidChannelSessionBootstrapException(String message) {
        super("INVALID_CHANNEL_SESSION_BOOTSTRAP", message, HttpStatus.UNAUTHORIZED);
    }
}