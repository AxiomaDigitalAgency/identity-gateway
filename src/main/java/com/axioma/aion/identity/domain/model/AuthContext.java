package com.axioma.aion.identity.domain.model;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record AuthContext(
        boolean authenticated,
        String tenantId,
        String subject,
        String channel,
        String authType,
        List<String> scopes,
        Map<String, Object> claims
) {
}