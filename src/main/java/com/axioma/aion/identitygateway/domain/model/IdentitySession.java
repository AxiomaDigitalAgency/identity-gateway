package com.axioma.aion.identitygateway.domain.model;

import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.securitycore.model.AuthContext;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record IdentitySession(
        String sessionId,
        String tenantId,
        String subject,
        String channel,
        String provider,
        TokenId tokenId,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime revokedAt,
        String authorities,
        String attributes
) {

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(OffsetDateTime now) {
        return expiresAt == null || !expiresAt.isAfter(now);
    }

    public AuthContext toAuthContext() {
        return AuthContext.builder()
                .tenantId(tenantId)
                .subject(subject)
                .channel(channel)
                .provider(provider)
                .authenticated(true)
                .authorities(List.of())
                .attributes(Map.of())
                .build();
    }
}