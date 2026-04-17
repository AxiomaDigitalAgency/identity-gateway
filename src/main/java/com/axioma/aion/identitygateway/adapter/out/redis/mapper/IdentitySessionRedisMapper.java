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
                .sessionId(entity.getSessionId())
                .tenantId(entity.getTenantId())
                .subject(entity.getSubject())
                .channel(entity.getChannel())
                .provider(entity.getProvider())
                .tokenId(new TokenId(entity.getTokenId()))
                .issuedAt(entity.getIssuedAt())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .authorities(entity.getAuthorities())
                .attributes(entity.getAttributes())
                .build();
    }

    public IdentitySessionRedisEntity toEntity(IdentitySession session) {
        if (session == null) {
            return null;
        }

        IdentitySessionRedisEntity entity = new IdentitySessionRedisEntity();
        entity.setTokenId(session.tokenId().value());
        entity.setSessionId(session.sessionId());
        entity.setTenantId(session.tenantId());
        entity.setSubject(session.subject());
        entity.setChannel(session.channel());
        entity.setProvider(session.provider());
        entity.setIssuedAt(session.issuedAt());
        entity.setExpiresAt(session.expiresAt());
        entity.setRevokedAt(session.revokedAt());
        entity.setAuthorities(session.authorities());
        entity.setAttributes(session.attributes());
        return entity;
    }
}