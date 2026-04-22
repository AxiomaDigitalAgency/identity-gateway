package com.axioma.aion.identitygateway.adapter.in.web.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CredentialPayload(
        UUID credentialId,
        String credentialKey,
        String clientId,
        String clientSecret,
        String token
) {}