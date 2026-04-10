package com.axioma.aion.identity.adapter.in.web.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record AuthenticateResponseDto(
        boolean success,
        String tenantId,
        String subject,
        String channel,
        String authType,
        List<String> scopes,
        Map<String, Object> claims,
        String errorCode,
        String errorMessage
) {
}