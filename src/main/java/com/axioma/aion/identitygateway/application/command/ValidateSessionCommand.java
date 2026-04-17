package com.axioma.aion.identitygateway.application.command;

import lombok.Builder;

@Builder
public record ValidateSessionCommand(
        String sessionToken
) {
}