package com.axioma.aion.identitygateway.domain.model;

import lombok.Builder;

@Builder
public record WidgetValidationResult(
        String tenantId,
        boolean allowed,
        String widgetKey,
        String origin
) {
}