package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.RevokeSessionCommand;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevokeSessionServiceTest {

    @Mock
    private SessionPort sessionPort;

    @Test
    void execute_shouldRevokeActiveSession() {
        RevokeSessionService service = new RevokeSessionService(sessionPort);

        UUID sessionId = UUID.randomUUID();
        IdentitySession session = new IdentitySession(
                sessionId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "subject-1",
                "web",
                AuthenticationType.valueOf("WIDGET"),
                "ACTIVE",
                OffsetDateTime.now().minusMinutes(1),
                OffsetDateTime.now().minusMinutes(1),
                OffsetDateTime.now().plusMinutes(10),
                null
        );

        when(sessionPort.findById(sessionId)).thenReturn(Mono.just(session));
        when(sessionPort.revoke(sessionId)).thenReturn(Mono.empty());

        StepVerifier.create(service.execute(RevokeSessionCommand.builder()
                        .sessionId(sessionId)
                        .reason("logout")
                        .requestedBy("tester")
                        .build()))
                .assertNext(result -> {
                    assertEquals(sessionId.toString(), result.sessionId());
                    assertEquals(true, result.revoked());
                })
                .verifyComplete();

        verify(sessionPort).revoke(sessionId);
    }

    @Test
    void execute_shouldFailWhenSessionIdIsNull() {
        RevokeSessionService service = new RevokeSessionService(sessionPort);

        StepVerifier.create(service.execute(RevokeSessionCommand.builder()
                        .sessionId(null)
                        .reason("logout")
                        .requestedBy("tester")
                        .build()))
                .expectErrorSatisfies(error -> assertEquals("sessionId is required", error.getMessage()))
                .verify();
    }
}
