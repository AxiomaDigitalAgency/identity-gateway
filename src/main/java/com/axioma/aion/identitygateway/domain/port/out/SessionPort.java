package com.axioma.aion.identitygateway.domain.port.out;

import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SessionPort {
    Mono<IdentitySession> save(IdentitySession session);
    Mono<IdentitySession> findById(UUID sessionId);
    Mono<Void> revoke(UUID sessionId);
}