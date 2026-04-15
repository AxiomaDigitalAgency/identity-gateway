package com.axioma.aion.identity.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateChannelSessionRequestDto(
        @NotBlank(message = "channel is required")
        String channel,

        @NotBlank(message = "widgetKey is required")
        String widgetKey,

        @NotBlank(message = "origin is required")
        String origin
) {
}