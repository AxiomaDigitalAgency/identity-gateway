package com.axioma.aion.identitygateway.adapter.out.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Getter
@Setter
@Table("identity_session")
public class IdentitySessionEntity {

    @Id
    private String sessionId;

    private String tenantId;
    private String subject;
    private String channel;
    private String provider;
    private String tokenId;
    private OffsetDateTime issuedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime revokedAt;

    private String authorities;
    private String attributes;
}