package com.axioma.aion.identitygateway.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ValidateSessionRequest(
        @NotBlank(message = "sessionToken must not be blank")
        String sessionToken
) {
}