package com.axioma.aion.identitygateway.adapter.out.redis.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class IdentitySessionRedisEntity {

    private String tokenId;
    private String sessionId;
    private String tenantId;
    private String subject;
    private String channel;
    private String provider;
    private OffsetDateTime issuedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime revokedAt;
    private String authorities;
    private String attributes;
}