package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.AuthenticateAndCreateSessionCommand;
import com.axioma.aion.identitygateway.application.result.AuthenticateIdentityResult;
import com.axioma.aion.identitygateway.application.result.CreateSessionResult;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import com.axioma.aion.securitycore.model.AuthContext;
import com.axioma.aion.securitycore.model.SecurityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateAndCreateSessionServiceTest {

    @Mock
    private AuthenticateIdentityService authenticateIdentityService;

    @Mock
    private CreateSessionService createSessionService;

    private AuthenticateAndCreateSessionService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticateAndCreateSessionService(
                authenticateIdentityService,
                createSessionService
        );
    }

    @Test
    void shouldAuthenticateAndCreateSessionSuccessfully() {
        SecurityRequest securityRequest = SecurityRequest.builder()
                .channel("web")
                .authType("widget")
                .credentials(Map.of("widgetKey", "abc", "origin", "http://localhost:3000"))
                .build();

        AuthContext authContext = AuthContext.builder()
                .tenantId("tenant-demo")
                .subject("widget:demo")
                .channel("web")
                .provider("widget")
                .authenticated(true)
                .authorities(List.of())
                .attributes(Map.of())
                .build();

        when(authenticateIdentityService.execute(any()))
                .thenReturn(Mono.just(
                        AuthenticateIdentityResult.builder()
                                .authenticated(true)
                                .authContext(authContext)
                                .build()
                ));

        when(createSessionService.execute(any()))
                .thenReturn(Mono.just(
                        CreateSessionResult.builder()
                                .sessionToken("jwt-token")
                                .sessionId("sess-123")
                                .tokenId(new TokenId("tok-123"))
                                .expiresAt(OffsetDateTime.parse("2026-04-17T12:00:00Z"))
                                .authContext(authContext)
                                .build()
                ));

        StepVerifier.create(service.execute(
                        AuthenticateAndCreateSessionCommand.builder()
                                .securityRequest(securityRequest)
                                .build()
                ))
                .assertNext(result -> {
                    assert result.authenticated();
                    assert "jwt-token".equals(result.sessionToken());
                    assert "sess-123".equals(result.sessionId());
                    assert "tenant-demo".equals(result.authContext().tenantId());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenSecurityRequestIsNull() {
        StepVerifier.create(service.execute(
                        AuthenticateAndCreateSessionCommand.builder()
                                .securityRequest(null)
                                .build()
                ))
                .expectError(InvalidSecurityRequestException.class)
                .verify();
    }
}