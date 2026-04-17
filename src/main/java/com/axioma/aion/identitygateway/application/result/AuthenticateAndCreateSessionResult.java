package com.axioma.aion.identitygateway.application.result;

import com.axioma.aion.securitycore.model.AuthContext;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record AuthenticateAndCreateSessionResult(
        boolean authenticated,
        String sessionToken,
        String sessionId,
        OffsetDateTime expiresAt,
        AuthContext authContext
) {
}