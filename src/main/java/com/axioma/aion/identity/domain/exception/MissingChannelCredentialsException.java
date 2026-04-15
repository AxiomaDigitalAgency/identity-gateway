package com.axioma.aion.identity.domain.exception;

import org.springframework.http.HttpStatus;

public class MissingChannelCredentialsException extends IdentityException {

    public MissingChannelCredentialsException(String message) {
        super("MISSING_CHANNEL_CREDENTIALS", message, HttpStatus.BAD_REQUEST);
    }
}