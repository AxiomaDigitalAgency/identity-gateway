package com.axioma.aion.identitygateway.adapter.in.web.dto;

import com.axioma.aion.securitycore.model.AuthContext;
import lombok.Builder;

@Builder
public record AuthenticateResponse(
        boolean authenticated,
        AuthContext authContext
) {
}