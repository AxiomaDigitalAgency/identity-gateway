package com.axioma.aion.identitygateway.adapter.in.web.dto;

import com.axioma.aion.securitycore.model.AuthContext;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateSessionResponse(
        UUID sessionId,
        String sessionToken,
        AuthContext authContext
) {}