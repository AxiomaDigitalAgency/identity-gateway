package com.axioma.aion.identitygateway.application.command;

import com.axioma.aion.securitycore.model.AuthenticationType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthenticateCommand(
        AuthenticationType authenticationType,
        UUID credentialId,
        String credentialKey,
        String clientId,
        String clientSecret,
        String token,
        String channel,
        String origin,
        String provider,
        String subject,
        String ipAddress,
        String userAgent,
        String requestId,
        String providerMessageId
) {}