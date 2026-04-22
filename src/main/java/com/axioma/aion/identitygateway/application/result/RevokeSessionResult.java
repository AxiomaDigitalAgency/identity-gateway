package com.axioma.aion.identitygateway.application.result;

import lombok.Builder;

@Builder
public record RevokeSessionResult(
        String sessionId,
        boolean revoked
) {
}
