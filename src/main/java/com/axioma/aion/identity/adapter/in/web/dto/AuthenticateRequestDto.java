package com.axioma.aion.identity.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Map;

@Builder
public record AuthenticateRequestDto(
        @NotBlank(message = "channel is required")
        String channel,

        String tenantHint,
        String authorizationHeader,
        String widgetKey,
        String origin,
        String sessionToken,
        Map<String, Object> attributes
) {
}