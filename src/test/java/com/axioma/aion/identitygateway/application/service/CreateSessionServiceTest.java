package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.CreateSessionCommand;
import com.axioma.aion.identitygateway.config.SessionProperties;
import com.axioma.aion.identitygateway.domain.model.AuthenticatedPrincipal;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.port.out.AuthenticationStatePort;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.IdGeneratorPort;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionTokenPort;
import com.axioma.aion.identitygateway.domain.port.out.SessionPort;
import com.axioma.aion.securitycore.model.AuthenticationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSessionServiceTest {

    @Mock
    private AuthenticationStatePort authenticationStatePort;
    @Mock
    private SessionPort sessionPort;
    @Mock
    private JwtSessionTokenPort jwtSessionTokenPort;
    @Mock
    private ClockPort clockPort;
    @Mock
    private IdGeneratorPort idGeneratorPort;

    private SessionProperties sessionProperties;
    private CreateSessionService service;

    @BeforeEach
    void setUp() {
        sessionProperties = new SessionProperties();
        sessionProperties.setTtlSeconds(300);
        service = new CreateSessionService(
                authenticationStatePort,
                sessionPort,
                jwtSessionTokenPort,
                clockPort,
                idGeneratorPort,
                sessionProperties
        );
    }

    @Test
    void createSession_shouldPersistGenerateTokenAndConsumeAuthentication() {
        UUID authenticationId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID credentialId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                authenticationId,
                tenantId,
                credentialId,
                "web",
                "subject-1",
                AuthenticationType.valueOf("WIDGET"),
                now.minusSeconds(10),
                now.plusSeconds(120),
                Map.of("origin", "https://app.example.com")
        );

        IdentitySession session = new IdentitySession(
                sessionId,
                tenantId,
                credentialId,
                "subject-1",
                "web",
                AuthenticationType.valueOf("WIDGET"),
                "ACTIVE",
                now.minusSeconds(10),
                now,
                now.plusSeconds(300),
                null
        );

        when(authenticationStatePort.isConsumed(authenticationId)).thenReturn(Mono.just(false));
        when(authenticationStatePort.findByAuthenticationId(authenticationId)).thenReturn(Mono.just(principal));
        when(clockPort.now()).thenReturn(now);
        when(idGeneratorPort.generate()).thenReturn(sessionId);
        when(sessionPort.save(any(IdentitySession.class))).thenReturn(Mono.just(session));
        when(jwtSessionTokenPort.generate(any())).thenReturn(Mono.just("jwt-token"));
        when(authenticationStatePort.markConsumed(authenticationId)).thenReturn(Mono.empty());

        StepVerifier.create(service.createSession(new CreateSessionCommand(authenticationId)))
                .assertNext(response -> {
                    assertEquals(sessionId, response.sessionId());
                    assertEquals("jwt-token", response.sessionToken());
                })
                .verifyComplete();

        verify(authenticationStatePort).markConsumed(authenticationId);
    }

    @Test
    void createSession_shouldFailWhenAuthenticationAlreadyConsumed() {
        UUID authenticationId = UUID.randomUUID();
        when(authenticationStatePort.isConsumed(authenticationId)).thenReturn(Mono.just(true));

        StepVerifier.create(service.createSession(new CreateSessionCommand(authenticationId)))
                .expectErrorSatisfies(error -> assertEquals("Authentication already consumed", error.getMessage()))
                .verify();
    }
}
