package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.ValidateSessionCommand;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.JwtSessionClaims;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionProviderPort;
import com.axioma.aion.identitygateway.domain.port.out.SessionPort;
import com.axioma.aion.securitycore.model.AuthenticationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateSessionServiceTest {

    @Mock
    private JwtSessionProviderPort jwtSessionProviderPort;
    @Mock
    private SessionPort sessionPort;
    @Mock
    private ClockPort clockPort;

    @Test
    void execute_shouldReturnValidAuthContextWhenSessionIsValid() {
        ValidateSessionService service = new ValidateSessionService(
                jwtSessionProviderPort,
                sessionPort,
                clockPort
        );

        UUID sessionId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID credentialId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        JwtSessionClaims claims = JwtSessionClaims.builder()
                .sessionId(sessionId.toString())
                .tenantId(tenantId.toString())
                .subject("subject-1")
                .channel("web")
                .tokenId(new TokenId("jti-1"))
                .build();

        IdentitySession session = new IdentitySession(
                sessionId,
                tenantId,
                credentialId,
                "subject-1",
                "web",
                AuthenticationType.valueOf("WIDGET"),
                "ACTIVE",
                now.minusSeconds(20),
                now.minusSeconds(10),
                now.plusSeconds(300),
                null
        );

        when(jwtSessionProviderPort.parse("token-1")).thenReturn(Mono.just(claims));
        when(sessionPort.findById(sessionId)).thenReturn(Mono.just(session));
        when(clockPort.now()).thenReturn(now);

        StepVerifier.create(service.execute(ValidateSessionCommand.builder().sessionToken("token-1").build()))
                .assertNext(result -> {
                    assertEquals(tenantId, result.authContext().tenantId());
                    assertEquals(sessionId, result.authContext().sessionId());
                    assertEquals("subject-1", result.authContext().subject());
                })
                .verifyComplete();
    }

    @Test
    void execute_shouldFailWhenSessionIsRevoked() {
        ValidateSessionService service = new ValidateSessionService(
                jwtSessionProviderPort,
                sessionPort,
                clockPort
        );

        UUID sessionId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        JwtSessionClaims claims = JwtSessionClaims.builder()
                .sessionId(sessionId.toString())
                .tenantId(tenantId.toString())
                .subject("subject-1")
                .channel("web")
                .tokenId(new TokenId("jti-2"))
                .build();

        IdentitySession revoked = new IdentitySession(
                sessionId,
                tenantId,
                UUID.randomUUID(),
                "subject-1",
                "web",
                AuthenticationType.valueOf("WIDGET"),
                "REVOKED",
                now.minusSeconds(20),
                now.minusSeconds(10),
                now.plusSeconds(300),
                now.minusSeconds(5)
        );

        when(jwtSessionProviderPort.parse("token-2")).thenReturn(Mono.just(claims));
        when(sessionPort.findById(sessionId)).thenReturn(Mono.just(revoked));

        StepVerifier.create(service.execute(ValidateSessionCommand.builder().sessionToken("token-2").build()))
                .expectErrorSatisfies(error -> assertEquals("Session revoked", error.getMessage()))
                .verify();
    }
}
