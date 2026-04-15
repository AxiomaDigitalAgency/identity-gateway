package com.axioma.aion.identity.domain.model;

import lombok.Builder;

@Builder
public record ChannelSessionResult(
        String sessionToken,
        String tokenType,
        long expiresIn,
        String tenantId,
        String subject,
        String channel,
        String authType
) {
}