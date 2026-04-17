package com.axioma.aion.identitygateway.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RevokeSessionRequest(
        @NotBlank(message = "sessionToken must not be blank")
        String sessionToken,

        @NotBlank(message = "reason must not be blank")
        String reason,

        @NotBlank(message = "requestedBy must not be blank")
        String requestedBy
) {
}