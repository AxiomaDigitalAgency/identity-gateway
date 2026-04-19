package com.axioma.aion.identitygateway.adapter.out.redis.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class IdentitySessionRedisEntity {

    private String id;
    private String identityContextId;
    private String tenantId;
    private String channel;
    private String tokenId;
    private String status;
    private OffsetDateTime issuedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime lastSeenAt;
    private String clientIp;
    private String userAgent;
    private String metadataJson;
}