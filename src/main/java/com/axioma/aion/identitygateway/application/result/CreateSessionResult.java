package com.axioma.aion.identitygateway.application.result;

import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.securitycore.model.AuthContext;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record CreateSessionResult(
        String sessionToken,
        String sessionId,
        TokenId tokenId,
        OffsetDateTime expiresAt,
        AuthContext authContext
) {
}