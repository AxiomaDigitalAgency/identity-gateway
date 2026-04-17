package com.axioma.aion.identitygateway.adapter.out.redis;

import com.axioma.aion.identitygateway.adapter.out.redis.entity.IdentitySessionRedisEntity;
import com.axioma.aion.identitygateway.adapter.out.redis.mapper.IdentitySessionRedisMapper;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.identitygateway.domain.port.out.SessionCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisSessionCacheAdapter implements SessionCachePort {

    private static final String SESSION_PREFIX = "identity:session:";
    private static final String BLACKLIST_PREFIX = "identity:session:blacklist:";

    private final ReactiveRedisTemplate<String, IdentitySessionRedisEntity> sessionRedisTemplate;
    private final ReactiveStringRedisTemplate stringRedisTemplate;
    private final IdentitySessionRedisMapper redisMapper;

    @Override
    public Mono<IdentitySession> findByTokenId(TokenId tokenId) {
        return sessionRedisTemplate.opsForValue()
                .get(buildSessionKey(tokenId))
                .map(redisMapper::toDomain);
    }

    @Override
    public Mono<Void> save(IdentitySession session, Duration ttl) {
        IdentitySessionRedisEntity entity = redisMapper.toEntity(session);
        Duration effectiveTtl = (ttl == null || ttl.isNegative()) ? Duration.ZERO : ttl;

        if (effectiveTtl.isZero()) {
            return sessionRedisTemplate.opsForValue()
                    .set(buildSessionKey(session.tokenId()), entity)
                    .then();
        }

        return sessionRedisTemplate.opsForValue()
                .set(buildSessionKey(session.tokenId()), entity, effectiveTtl)
                .then();
    }

    @Override
    public Mono<Void> delete(TokenId tokenId) {
        return sessionRedisTemplate.delete(buildSessionKey(tokenId))
                .then();
    }

    @Override
    public Mono<Void> blacklistSession(String sessionId, Duration ttl) {
        Duration effectiveTtl = (ttl == null || ttl.isNegative()) ? Duration.ZERO : ttl;

        if (effectiveTtl.isZero()) {
            return stringRedisTemplate.opsForValue()
                    .set(buildBlacklistKey(sessionId), "true")
                    .then();
        }

        return stringRedisTemplate.opsForValue()
                .set(buildBlacklistKey(sessionId), "true", effectiveTtl)
                .then();
    }

    @Override
    public Mono<Boolean> isSessionBlacklisted(String sessionId) {
        return stringRedisTemplate.hasKey(buildBlacklistKey(sessionId))
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> removeSession(String sessionId) {
        return stringRedisTemplate.delete(buildBlacklistKey(sessionId))
                .then();
    }

    private String buildSessionKey(TokenId tokenId) {
        return SESSION_PREFIX + tokenId.value();
    }

    private String buildBlacklistKey(String sessionId) {
        return BLACKLIST_PREFIX + sessionId;
    }
}