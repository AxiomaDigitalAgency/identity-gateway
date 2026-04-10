package com.axioma.aion.identity.domain.model;

import lombok.Builder;

@Builder
public record AuthenticationResult(
        boolean success,
        AuthContext authContext,
        String errorCode,
        String errorMessage
) {
}