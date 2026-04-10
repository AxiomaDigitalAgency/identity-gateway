package com.axioma.aion.identity.domain.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record SecurityRequest(
        String channel,
        String tenantHint,
        String authorizationHeader,
        String widgetKey,
        String origin,
        String sessionToken,
        String clientIp,
        String userAgent,
        Map<String, Object> attributes
) {
}