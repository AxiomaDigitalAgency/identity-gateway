package com.axioma.aion.identity.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record CreateChannelSessionResponseDto(
        String sessionToken,
        String tokenType,
        long expiresIn,
        String tenantId,
        String subject,
        String channel,
        String authType
) {
}