package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.AuthenticateCommand;
import com.axioma.aion.identitygateway.config.SessionProperties;
import com.axioma.aion.identitygateway.domain.model.AuthenticatedPrincipal;
import com.axioma.aion.identitygateway.domain.model.IdentityCredential;
import com.axioma.aion.identitygateway.domain.port.out.AuthenticationStatePort;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.CredentialPort;
import com.axioma.aion.identitygateway.domain.port.out.IdGeneratorPort;
import com.axioma.aion.identitygateway.domain.port.out.OriginRulePort;
import com.axioma.aion.securitycore.model.AuthenticationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateIdentityServiceTest {

    @Mock
    private CredentialPort credentialPort;
    @Mock
    private OriginRulePort originRulePort;
    @Mock
    private AuthenticationStatePort authenticationStatePort;
    @Mock
    private ClockPort clockPort;
    @Mock
    private IdGeneratorPort idGeneratorPort;

    private SessionProperties sessionProperties;
    private AuthenticateIdentityService service;

    @BeforeEach
    void setUp() {
        sessionProperties = new SessionProperties();
        sessionProperties.setTtlSeconds(120);
        service = new AuthenticateIdentityService(
                credentialPort,
                originRulePort,
                authenticationStatePort,
                clockPort,
                idGeneratorPort,
                sessionProperties
        );
    }

    @Test
    void authenticate_shouldReturnAuthenticationIdAndSaveState() {
        UUID credentialId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID authenticationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        IdentityCredential credential = new IdentityCredential(
                credentialId,
                UUID.randomUUID(),
                tenantId,
                "WIDGET_KEY",
                "widget-key",
                "hash",
                "ACTIVE",
                true
        );

        AuthenticateCommand command = new AuthenticateCommand(
                AuthenticationType.valueOf("WIDGET"),
                credentialId,
                null,
                null,
                null,
                null,
                "web",
                "https://app.example.com",
                "widget",
                "user-1",
                "10.0.0.1",
                "UA",
                "req-1",
                "pm-1"
        );

        when(credentialPort.findById(credentialId)).thenReturn(Mono.just(credential));
        when(originRulePort.isAllowed(credentialId, "https://app.example.com")).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(now);
        when(idGeneratorPort.generate()).thenReturn(authenticationId);
        when(authenticationStatePort.save(any(AuthenticatedPrincipal.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.authenticate(command))
                .assertNext(response -> {
                    assertEquals(authenticationId, response.authenticationId());
                    assertEquals(tenantId, response.tenantId());
                    assertEquals(credentialId, response.credentialId());
                    assertEquals(now.plusSeconds(120), response.expiresAt());
                    assertEquals("user-1", response.subject());
                })
                .verifyComplete();

        ArgumentCaptor<AuthenticatedPrincipal> captor = ArgumentCaptor.forClass(AuthenticatedPrincipal.class);
        verify(authenticationStatePort).save(captor.capture());
        assertEquals(authenticationId, captor.getValue().authenticationId());
    }

    @Test
    void authenticate_shouldFailWhenGeneratedAuthenticationIdIsNull() {
        UUID credentialId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        IdentityCredential credential = new IdentityCredential(
                credentialId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SERVICE_CLIENT",
                "service-client",
                "hash",
                "ACTIVE",
                true
        );

        AuthenticateCommand command = new AuthenticateCommand(
                AuthenticationType.valueOf("SERVICE"),
                credentialId,
                null,
                null,
                null,
                null,
                "api",
                null,
                null,
                "svc",
                null,
                null,
                "req-2",
                null
        );

        when(credentialPort.findById(credentialId)).thenReturn(Mono.just(credential));
        when(clockPort.now()).thenReturn(now);
        when(idGeneratorPort.generate()).thenReturn(null);

        StepVerifier.create(service.authenticate(command))
                .expectErrorSatisfies(error -> {
                    assertNotNull(error);
                    assertEquals("Generated authenticationId is null", error.getMessage());
                })
                .verify();
    }
}
