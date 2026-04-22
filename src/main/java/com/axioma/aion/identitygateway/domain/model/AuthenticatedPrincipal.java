package com.axioma.aion.identitygateway.domain.model;

import com.axioma.aion.securitycore.model.AuthenticationType;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record AuthenticatedPrincipal(
        UUID authenticationId,
        UUID tenantId,
        UUID credentialId,
        String channel,
        String subject,
        AuthenticationType authenticationType,
        OffsetDateTime authenticatedAt,
        OffsetDateTime expiresAt,
        Map<String, Object> attributes
) {}