package com.axioma.aion.identitygateway.domain.port.out;

import reactor.core.publisher.Mono;

public interface AuditEventPort {

    Mono<Void> recordSessionCreated(
            String tenantId,
            String sessionId,
            String tokenId,
            String subject,
            String channel,
            String provider
    );

    Mono<Void> recordSessionRevoked(
            String tenantId,
            String sessionId,
            String tokenId,
            String reason,
            String requestedBy
    );

    Mono<Void> recordSessionValidationFailed(
            String tokenId,
            String reason
    );
}