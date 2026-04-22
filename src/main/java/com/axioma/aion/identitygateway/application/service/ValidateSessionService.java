package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.ValidateSessionCommand;
import com.axioma.aion.identitygateway.application.result.ValidateSessionResult;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.JwtSessionClaims;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionProviderPort;
import com.axioma.aion.identitygateway.domain.port.out.SessionPort;
import com.axioma.aion.securitycore.exception.AuthenticationFailedException;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import com.axioma.aion.securitycore.model.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidateSessionService {

    private final JwtSessionProviderPort jwtSessionProviderPort;
    private final SessionPort sessionPort;
    private final ClockPort clockPort;

    public Mono<ValidateSessionResult> execute(ValidateSessionCommand command) {
        if (command.sessionToken() == null || command.sessionToken().isBlank()) {
            return Mono.error(new InvalidSecurityRequestException("sessionToken must not be blank"));
        }

        log.info("validate_session_start sessionTokenPresent={}", true);

        return jwtSessionProviderPort.parse(command.sessionToken())
                .doOnNext(claims -> log.info(
                        "validate_session_token_parsed sessionId={} tenantId={} channel={} subject={}",
                        claims.sessionId(),
                        claims.tenantId(),
                        claims.channel(),
                        claims.subject()))
                .flatMap(claims -> resolveSession(claims)
                        .flatMap(session -> validateResolvedSession(claims, session))
                        .map(this::toAuthContext)
                )
                .map(authContext -> ValidateSessionResult.builder()
                        .authContext(authContext)
                        .build())
                .doOnSuccess(result -> log.info(
                        "validate_session_success tenantId={} sessionId={}",
                        result != null && result.authContext() != null ? result.authContext().tenantId() : null,
                        result != null && result.authContext() != null ? result.authContext().sessionId() : null))
                .doOnError(error -> log.error("validate_session_error message={}", error.getMessage(), error));
    }

    private Mono<IdentitySession> resolveSession(JwtSessionClaims claims) {
        UUID sessionId = parseSessionId(claims.sessionId());
        return sessionPort.findById(sessionId)
                .switchIfEmpty(Mono.error(new AuthenticationFailedException("Session not found")));
    }

    private Mono<IdentitySession> validateResolvedSession(JwtSessionClaims claims, IdentitySession session) {
        if (session.isRevoked()) {
            return Mono.error(new AuthenticationFailedException("Session revoked"));
        }

        if (session.isExpired(clockPort.now())) {
            return Mono.error(new AuthenticationFailedException("Session expired"));
        }

        if (!safeEquals(session.id() != null ? session.id().toString() : null, claims.sessionId())
                || !safeEquals(session.tenantId() != null ? session.tenantId().toString() : null, claims.tenantId())
                || !safeEquals(session.subject(), claims.subject())
                || !safeEquals(session.channel(), claims.channel())) {
            return Mono.error(new AuthenticationFailedException("Session claims mismatch"));
        }

        return Mono.just(session);
    }

    private AuthContext toAuthContext(IdentitySession session) {
        return new AuthContext(
                session.tenantId(),
                session.credentialId(),
                normalizeChannel(session.channel()),
                session.subject(),
                session.id(),
                session.authenticationType(),
                session.authenticatedAt(),
                session.expiresAt(),
                Map.of()
        );
    }

    private String normalizeChannel(String channel) {
        return channel == null ? null : channel.toLowerCase();
    }

    private boolean safeEquals(String left, String right) {
        return Objects.equals(left, right);
    }

    private UUID parseSessionId(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new InvalidSecurityRequestException("Invalid sessionId in token");
        }
    }
}
