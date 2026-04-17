package com.axioma.aion.identitygateway.adapter.out.audit;

import com.axioma.aion.identitygateway.domain.port.out.AuditEventPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingAuditEventAdapter implements AuditEventPort {

    @Override
    public Mono<Void> recordSessionCreated(
            String tenantId,
            String sessionId,
            String tokenId,
            String subject,
            String channel,
            String provider
    ) {
        log.info("audit_event=session_created tenantId={} sessionId={} tokenId={} subject={} channel={} provider={}",
                tenantId, sessionId, tokenId, subject, channel, provider);
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
        log.info("audit_event=session_revoked tenantId={} sessionId={} tokenId={} reason={} requestedBy={}",
                tenantId, sessionId, tokenId, reason, requestedBy);
        return Mono.empty();
    }

    @Override
    public Mono<Void> recordSessionValidationFailed(
            String tokenId,
            String reason
    ) {
        log.warn("audit_event=session_validation_failed tokenId={} reason={}", tokenId, reason);
        return Mono.empty();
    }
}