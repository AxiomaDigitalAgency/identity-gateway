package com.axioma.aion.identity.domain.exception;

import org.springframework.http.HttpStatus;

public class SecurityProviderNotFoundException extends IdentityException {

    public SecurityProviderNotFoundException(String message) {
        super("SECURITY_PROVIDER_NOT_FOUND", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}