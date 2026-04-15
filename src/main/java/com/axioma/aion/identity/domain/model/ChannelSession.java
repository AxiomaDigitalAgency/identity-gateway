package com.axioma.aion.identity.domain.model;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ChannelSession(
        String sessionId,
        String tenantId,
        String channel,
        String widgetKey,
        String origin,
        Instant issuedAt,
        Instant expiresAt
) {}