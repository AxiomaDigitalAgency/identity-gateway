package com.axioma.aion.identity.domain.model;

import lombok.Builder;

@Builder
public record SessionValidationResult(
        boolean valid,
        String tenantId,
        String subject,
        String channel,
        String authType,
        String errorCode,
        String errorMessage
) {
}