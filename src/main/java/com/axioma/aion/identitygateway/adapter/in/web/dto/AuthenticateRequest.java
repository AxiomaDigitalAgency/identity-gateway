package com.axioma.aion.identitygateway.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Map;

@Builder
public record AuthenticateRequest(
        @NotBlank(message = "channel must not be blank")
        String channel,

        @NotBlank(message = "authType must not be blank")
        String authType,

        Map<String, Object> credentials,

        Map<String, Object> metadata
) {
}