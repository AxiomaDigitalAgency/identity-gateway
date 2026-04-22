package com.axioma.aion.identitygateway.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record ChannelContext(
        String channel,
        String origin,
        String provider
) {}