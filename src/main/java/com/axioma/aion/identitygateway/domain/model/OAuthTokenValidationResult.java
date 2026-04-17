package com.axioma.aion.identitygateway.domain.model;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record OAuthTokenValidationResult(
        String tenantId,
        String subject,
        String channel,
        boolean allowed,
        List<String> authorities,
        Map<String, Object> attributes
) {
}