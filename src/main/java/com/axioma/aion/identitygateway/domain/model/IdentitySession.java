package com.axioma.aion.identitygateway.domain.model;

import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record IdentitySession(
        String id,
        String identityContextId,
        String tenantId,
        String channel,
        TokenId tokenId,
        String status,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime lastSeenAt,
        String clientIp,
        String userAgent,
        String metadataJson
) {

    public boolean isRevoked() {
        return "REVOKED".equalsIgnoreCase(status);
    }

    public boolean isExpired(OffsetDateTime now) {
        return "EXPIRED".equalsIgnoreCase(status)
                || expiresAt == null
                || !expiresAt.isAfter(now);
    }
}