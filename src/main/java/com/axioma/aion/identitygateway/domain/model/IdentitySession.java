package com.axioma.aion.identitygateway.domain.model;

import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.securitycore.model.AuthenticationType;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record IdentitySession(
        UUID id,
        UUID tenantId,
        UUID credentialId,
        String subject,
        String channel,
        AuthenticationType authenticationType,
        String status,
        OffsetDateTime authenticatedAt,
        OffsetDateTime sessionCreatedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime revokedAt
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