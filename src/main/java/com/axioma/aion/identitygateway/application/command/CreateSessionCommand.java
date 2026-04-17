package com.axioma.aion.identitygateway.application.command;

import com.axioma.aion.securitycore.model.AuthContext;
import lombok.Builder;

@Builder
public record CreateSessionCommand(
        AuthContext authContext
) {
}