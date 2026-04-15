package com.axioma.aion.identity.domain.model;

import lombok.Builder;

@Builder
public record ChannelSessionToken(
        String token,
        String tokenType,
        long expiresIn
) {}