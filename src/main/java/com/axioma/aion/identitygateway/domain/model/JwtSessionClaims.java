package com.axioma.aion.identitygateway.domain.model;

import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record JwtSessionClaims(
        String sessionId,
        String tenantId,
        String subject,
        String channel,
        String provider,
        TokenId tokenId,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt
) {
}