package com.axioma.aion.identitygateway.domain.model;

import java.util.UUID;

public record IdentityCredential(
        UUID id,
        UUID identityContextId,
        UUID tenantId,
        String credentialType,
        String credentialKey,
        String secretHash,
        String status,
        boolean enabled
) {
}