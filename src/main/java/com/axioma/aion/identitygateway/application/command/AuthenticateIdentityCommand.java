package com.axioma.aion.identitygateway.application.command;

import com.axioma.aion.securitycore.model.SecurityRequest;
import lombok.Builder;

@Builder
public record AuthenticateIdentityCommand(
        SecurityRequest securityRequest
) {
}