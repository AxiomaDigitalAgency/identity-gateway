package com.axioma.aion.identitygateway.application.command;

import lombok.Builder;

import java.util.UUID;

@Builder
public record RevokeSessionCommand(
        UUID sessionId,
        String reason,
        String requestedBy
) {
}
