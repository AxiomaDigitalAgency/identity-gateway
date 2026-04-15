package com.axioma.aion.identity.domain.model;

import lombok.Builder;

@Builder
public record CreateChannelSessionRequest(
        String channel,
        String widgetKey,
        String origin,
        String clientIp,
        String userAgent
) {
}