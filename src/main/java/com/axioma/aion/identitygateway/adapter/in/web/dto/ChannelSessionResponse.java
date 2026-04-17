package com.axioma.aion.identitygateway.adapter.in.web.dto;

import com.axioma.aion.securitycore.model.AuthContext;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ChannelSessionResponse(
        boolean authenticated,
        String sessionToken,
        String sessionId,
        OffsetDateTime expiresAt,
        AuthContext authContext
) {
}