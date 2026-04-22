package com.axioma.aion.identitygateway.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record RequestMetadata(
        String ipAddress,
        String userAgent,
        String requestId,
        String providerMessageId
) {}