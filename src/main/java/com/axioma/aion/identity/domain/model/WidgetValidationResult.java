package com.axioma.aion.identity.domain.model;

import lombok.Builder;

@Builder
public record WidgetValidationResult(
        boolean valid,
        String tenantId,
        String widgetKey,
        String channel,
        String errorCode,
        String errorMessage
) {
}