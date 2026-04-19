package com.axioma.aion.identitygateway.adapter.out.redis.mapper;

import com.axioma.aion.identitygateway.adapter.out.redis.entity.IdentitySessionRedisEntity;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import org.springframework.stereotype.Component;

@Component
public class IdentitySessionRedisMapper {

    public IdentitySession toDomain(IdentitySessionRedisEntity entity) {
        if (entity == null) {
            return null;
        }

        return IdentitySession.builder()
                .id(entity.getId())
                .identityContextId(entity.getIdentityContextId())
                .tenantId(entity.getTenantId())
                .channel(entity.getChannel())
                .tokenId(new TokenId(entity.getTokenId()))
                .status(entity.getStatus())
                .issuedAt(entity.getIssuedAt())
                .expiresAt(entity.getExpiresAt())
                .lastSeenAt(entity.getLastSeenAt())
                .clientIp(entity.getClientIp())
                .userAgent(entity.getUserAgent())
                .metadataJson(entity.getMetadataJson())
                .build();
    }

    public IdentitySessionRedisEntity toEntity(IdentitySession session) {
        if (session == null) {
            return null;
        }

        IdentitySessionRedisEntity entity = new IdentitySessionRedisEntity();
        entity.setId(session.id());
        entity.setIdentityContextId(session.identityContextId());
        entity.setTenantId(session.tenantId());
        entity.setChannel(session.channel());
        entity.setTokenId(session.tokenId().value());
        entity.setStatus(session.status());
        entity.setIssuedAt(session.issuedAt());
        entity.setExpiresAt(session.expiresAt());
        entity.setLastSeenAt(session.lastSeenAt());
        entity.setClientIp(session.clientIp());
        entity.setUserAgent(session.userAgent());
        entity.setMetadataJson(session.metadataJson());

        return entity;
    }
}