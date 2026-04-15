package com.axioma.aion.identity.adapter.out.widget.dto;

import lombok.Builder;

@Builder
public record TenantWidgetValidationResponseDto(
        String tenantId,
        String status,
        Boolean valid
) {
}