package com.axioma.aion.identity.adapter.out.widget.dto;

import lombok.Builder;

@Builder
public record TenantWidgetValidationRequestDto(
        String widgetKey,
        String origin
) {
}