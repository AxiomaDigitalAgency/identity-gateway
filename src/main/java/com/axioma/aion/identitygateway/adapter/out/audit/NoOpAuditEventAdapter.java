package com.axioma.aion.identitygateway.adapter.out.audit;

import com.axioma.aion.identitygateway.domain.port.out.AuditEventPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class NoOpAuditEventAdapter implements AuditEventPort {

    @Override
    public Mono<Void> recordSessionCreated(
            String tenantId,
            String sessionId,
            String tokenId,
            String subject,
            String channel,
            String provider
    ) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> recordSessionRevoked(
            String tenantId,
            String sessionId,
            String tokenId,
            String reason,
            String requestedBy
    ) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> recordSessionValidationFailed(
            String tokenId,
            String reason
    ) {
        return Mono.empty();
    }
}