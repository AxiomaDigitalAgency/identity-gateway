package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.RevokeSessionCommand;
import com.axioma.aion.identitygateway.application.result.RevokeSessionResult;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.port.out.AuditEventPort;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.IdentitySessionRepositoryPort;
import com.axioma.aion.identitygateway.domain.port.out.SessionCachePort;
import com.axioma.aion.securitycore.exception.AuthenticationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RevokeSessionService {

    private final IdentitySessionRepositoryPort identitySessionRepositoryPort;
    private final SessionCachePort sessionCachePort;
    private final ClockPort clockPort;
    private final AuditEventPort auditEventPort;

    public Mono<RevokeSessionResult> execute(RevokeSessionCommand command) {
        return identitySessionRepositoryPort.findBySessionTokenId(command.tokenId())
                .switchIfEmpty(Mono.error(new AuthenticationFailedException("Session not found")))
                .flatMap(session -> revokeIfNeeded(command, session));
    }

    private Mono<RevokeSessionResult> revokeIfNeeded(RevokeSessionCommand command, IdentitySession session) {
        if (session.isRevoked()) {
            return Mono.just(buildResult(session, command));
        }

        OffsetDateTime now = clockPort.now();
        Duration ttl = calculateRemainingTtl(now, session.expiresAt());

        return identitySessionRepositoryPort.markSessionAsRevoked(session.sessionId(), now)
                .then(sessionCachePort.delete(command.tokenId()))
                .then(sessionCachePort.blacklistSession(session.sessionId(), ttl))
                .then(auditEventPort.recordSessionRevoked(
                        session.tenantId(),
                        session.sessionId(),
                        command.tokenId().value(),
                        command.reason(),
                        command.requestedBy()
                ))
                .thenReturn(buildResult(session, command));
    }

    private RevokeSessionResult buildResult(IdentitySession session, RevokeSessionCommand command) {
        return RevokeSessionResult.builder()
                .sessionId(session.sessionId())
                .tokenId(command.tokenId())
                .revoked(true)
                .build();
    }

    private Duration calculateRemainingTtl(OffsetDateTime now, OffsetDateTime expiresAt) {
        if (expiresAt == null || !expiresAt.isAfter(now)) {
            return Duration.ZERO;
        }
        return Duration.between(now, expiresAt);
    }
}