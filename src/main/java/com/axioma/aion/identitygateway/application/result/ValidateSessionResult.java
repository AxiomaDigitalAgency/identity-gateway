package com.axioma.aion.identitygateway.application.result;

import com.axioma.aion.securitycore.model.AuthContext;
import lombok.Builder;

@Builder
public record ValidateSessionResult(
        AuthContext authContext
) {
}