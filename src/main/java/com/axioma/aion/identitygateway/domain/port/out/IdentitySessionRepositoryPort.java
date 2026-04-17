package com.axioma.aion.identitygateway.domain.port.out;

import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface IdentitySessionRepositoryPort {

    Mono<IdentitySession> findBySessionTokenId(TokenId tokenId);
    Mono<IdentitySession> save(IdentitySession identitySession);
    Mono<Void> markSessionAsRevoked(String sessionId, OffsetDateTime revokedAt);
}