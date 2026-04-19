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
    private String id;

    private String identityContextId;
    private String tenantId;
    private String channel;
    private String sessionTokenId;
    private String status;
    private OffsetDateTime issuedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime lastSeenAt;
    private String clientIp;
    private String userAgent;
    private String metadataJson;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;
    private Boolean activeRegInd;
}