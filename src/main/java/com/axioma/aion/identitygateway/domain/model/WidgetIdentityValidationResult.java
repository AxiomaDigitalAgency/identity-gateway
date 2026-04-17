package com.axioma.aion.identitygateway.domain.model;

import lombok.Builder;

@Builder
public record WidgetIdentityValidationResult(
        String identityContextId,
        String tenantId,
        String channel,
        String subjectType,
        String subjectValue,
        boolean allowed,
        String origin
) {
}