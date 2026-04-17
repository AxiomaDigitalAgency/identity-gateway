package com.axioma.aion.identitygateway.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record RevokeSessionResponse(
        String sessionId,
        boolean revoked
) {
}