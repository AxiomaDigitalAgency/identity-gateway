package com.axioma.aion.identitygateway.domain.port.out;

import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface SessionCachePort {

    Mono<IdentitySession> findByTokenId(TokenId tokenId);
    Mono<Void> save(IdentitySession session, Duration ttl);
    Mono<Void> delete(TokenId tokenId);
    Mono<Void> blacklistSession(String sessionId, Duration ttl);
    Mono<Boolean> isSessionBlacklisted(String sessionId);
    Mono<Void> removeSession(String sessionId);
}