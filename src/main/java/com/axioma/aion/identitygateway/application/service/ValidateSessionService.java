package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.ValidateSessionCommand;
import com.axioma.aion.identitygateway.application.result.ValidateSessionResult;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.JwtSessionClaims;
import com.axioma.aion.identitygateway.domain.port.out.AuditEventPort;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.IdentitySessionRepositoryPort;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionProviderPort;
import com.axioma.aion.identitygateway.domain.port.out.SessionCachePort;
import com.axioma.aion.securitycore.exception.AuthenticationFailedException;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ValidateSessionService {

    private final JwtSessionProviderPort jwtSessionProviderPort;
    private final SessionCachePort sessionCachePort;
    private final IdentitySessionRepositoryPort identitySessionRepositoryPort;
    private final ClockPort clockPort;
    private final AuditEventPort auditEventPort;

    public Mono<ValidateSessionResult> execute(ValidateSessionCommand command) {
        if (command.sessionToken() == null || command.sessionToken().isBlank()) {
            return Mono.error(new InvalidSecurityRequestException("sessionToken must not be blank"));
        }

        return jwtSessionProviderPort.parse(command.sessionToken())
                .flatMap(claims -> resolveSession(claims)
                        .flatMap(session -> validateResolvedSession(claims, session)))
                .map(session -> ValidateSessionResult.builder()
                        .authContext(session.toAuthContext())
                        .build());
    }

    private Mono<IdentitySession> resolveSession(JwtSessionClaims claims) {
        return sessionCachePort.findByTokenId(claims.tokenId())
                .switchIfEmpty(Mono.defer(() ->
                        identitySessionRepositoryPort.findBySessionTokenId(claims.tokenId())
                ))
                .switchIfEmpty(Mono.error(new AuthenticationFailedException("Session not found")));
    }

    private Mono<IdentitySession> validateResolvedSession(JwtSessionClaims claims, IdentitySession session) {
        if (!session.tokenId().equals(claims.tokenId())
                || !safeEquals(session.sessionId(), claims.sessionId())
                || !safeEquals(session.tenantId(), claims.tenantId())
                || !safeEquals(session.subject(), claims.subject())) {
            return auditEventPort.recordSessionValidationFailed(
                            claims.tokenId().value(),
                            "session_claims_mismatch"
                    )
                    .then(Mono.error(new AuthenticationFailedException("Session claims mismatch")));
        }

        if (session.isRevoked()) {
            return auditEventPort.recordSessionValidationFailed(session.tokenId().value(), "session_revoked")
                    .then(Mono.error(new AuthenticationFailedException("Session revoked")));
        }

        if (session.isExpired(clockPort.now())) {
            return auditEventPort.recordSessionValidationFailed(session.tokenId().value(), "session_expired")
                    .then(Mono.error(new AuthenticationFailedException("Session expired")));
        }

        return sessionCachePort.isSessionBlacklisted(session.sessionId())
                .defaultIfEmpty(false)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return auditEventPort.recordSessionValidationFailed(session.tokenId().value(), "session_blacklisted")
                                .then(Mono.error(new AuthenticationFailedException("Session revoked")));
                    }
                    return Mono.just(session);
                });
    }

    private boolean safeEquals(String left, String right) {
        return java.util.Objects.equals(left, right);
    }
}