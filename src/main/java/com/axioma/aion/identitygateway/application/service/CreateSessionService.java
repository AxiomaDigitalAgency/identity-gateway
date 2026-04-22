package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.adapter.in.web.dto.CreateSessionResponse;
import com.axioma.aion.identitygateway.application.command.CreateSessionCommand;
import com.axioma.aion.identitygateway.config.SessionProperties;
import com.axioma.aion.identitygateway.domain.model.AuthenticatedPrincipal;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.port.in.CreateSessionUseCase;
import com.axioma.aion.identitygateway.domain.port.out.AuthenticationStatePort;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.IdGeneratorPort;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionTokenPort;
import com.axioma.aion.identitygateway.domain.port.out.SessionPort;
import com.axioma.aion.securitycore.model.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateSessionService implements CreateSessionUseCase {

    private final AuthenticationStatePort authenticationStatePort;
    private final SessionPort sessionPort;
    private final JwtSessionTokenPort jwtSessionTokenPort;
    private final ClockPort clockPort;
    private final IdGeneratorPort idGeneratorPort;
    private final SessionProperties sessionProperties;

    @Override
    public Mono<CreateSessionResponse> createSession(CreateSessionCommand command) {
        if (command.authenticationId() == null) {
            return Mono.error(new IllegalArgumentException("authenticationId is required"));
        }

        log.info("create_session_start authenticationId={}", command.authenticationId());

        return authenticationStatePort.isConsumed(command.authenticationId())
                .doOnNext(consumed -> log.info(
                        "create_session_consumed_check authenticationId={} consumed={}",
                        command.authenticationId(),
                        consumed))
                .flatMap(consumed -> consumed
                        ? Mono.error(new IllegalStateException("Authentication already consumed"))
                        : authenticationStatePort.findByAuthenticationId(command.authenticationId())
                )
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Authentication not found")))
                .doOnNext(principal -> log.info(
                        "create_session_authentication_state_found authenticationId={} tenantId={} credentialId={} expiresAt={}",
                        command.authenticationId(),
                        principal.tenantId(),
                        principal.credentialId(),
                        principal.expiresAt()))
                .flatMap(this::validateAuthenticationState)
                .flatMap(principal -> createSession(principal)
                        .flatMap(session -> {
                            log.info("create_session_persisted authenticationId={} sessionId={} tenantId={} expiresAt={}",
                                    principal.authenticationId(), session.id(), session.tenantId(), session.expiresAt());
                            AuthContext authContext = toAuthContext(principal, session);
                            return jwtSessionTokenPort.generate(authContext)
                                    .doOnNext(token -> log.info(
                                            "create_session_token_generated authenticationId={} sessionId={} tokenLength={}",
                                            principal.authenticationId(),
                                            session.id(),
                                            token != null ? token.length() : 0))
                                    .flatMap(token -> authenticationStatePort.markConsumed(principal.authenticationId())
                                            .doOnSuccess(unused -> log.info(
                                                    "create_session_authentication_marked_consumed authenticationId={}",
                                                    principal.authenticationId()))
                                            .thenReturn(new CreateSessionResponse(
                                                    session.id(),
                                                    token,
                                                    authContext
                                            )));
                        })
                );
    }

    private Mono<AuthenticatedPrincipal> validateAuthenticationState(AuthenticatedPrincipal principal) {
        OffsetDateTime now = clockPort.now();
        if (principal.expiresAt().isBefore(now)) {
            return Mono.error(new IllegalStateException("Authentication expired"));
        }
        log.info("create_session_authentication_state_valid authenticationId={} now={} expiresAt={}",
                principal.authenticationId(), now, principal.expiresAt());
        return Mono.just(principal);
    }

    private Mono<IdentitySession> createSession(AuthenticatedPrincipal principal) {
        OffsetDateTime sessionCreatedAt = clockPort.now();
        OffsetDateTime sessionExpiresAt = sessionCreatedAt.plusSeconds(sessionProperties.getTtlSeconds());
        UUID sessionId = idGeneratorPort.generate();
        if (sessionId == null) {
            return Mono.error(new IllegalStateException("Generated sessionId is null"));
        }

        IdentitySession session = new IdentitySession(
                sessionId,
                principal.tenantId(),
                principal.credentialId(),
                principal.subject(),
                principal.channel(),
                principal.authenticationType(),
                "ACTIVE",
                principal.authenticatedAt(),
                sessionCreatedAt,
                sessionExpiresAt,
                null
        );

        return sessionPort.save(session);
    }

    private AuthContext toAuthContext(AuthenticatedPrincipal principal, IdentitySession session) {
        return new AuthContext(
                session.tenantId(),
                session.credentialId(),
                session.channel(),
                session.subject(),
                session.id(),
                session.authenticationType(),
                session.authenticatedAt(),
                session.expiresAt(),
                new HashMap<>(principal.attributes())
        );
    }
}
