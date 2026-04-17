package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.AuthenticateIdentityCommand;
import com.axioma.aion.identitygateway.domain.port.out.SecurityProviderResolver;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import com.axioma.aion.securitycore.model.AuthContext;
import com.axioma.aion.securitycore.model.SecurityRequest;
import com.axioma.aion.securitycore.model.SecurityResult;
import com.axioma.aion.securitycore.port.SecurityProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateIdentityServiceTest {

    @Mock
    private SecurityProviderResolver securityProviderResolver;

    @Mock
    private SecurityProvider securityProvider;

    private AuthenticateIdentityService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticateIdentityService(securityProviderResolver);
    }

    @Test
    void shouldAuthenticateSuccessfully() {
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

        when(securityProviderResolver.resolve("web", "widget")).thenReturn(securityProvider);
        when(securityProvider.authenticate(securityRequest))
                .thenReturn(Mono.just(SecurityResult.success(authContext)));

        StepVerifier.create(service.execute(
                        AuthenticateIdentityCommand.builder()
                                .securityRequest(securityRequest)
                                .build()
                ))
                .assertNext(result -> {
                    assert result.authenticated();
                    assert "tenant-demo".equals(result.authContext().tenantId());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenSecurityRequestIsNull() {
        StepVerifier.create(service.execute(
                        AuthenticateIdentityCommand.builder()
                                .securityRequest(null)
                                .build()
                ))
                .expectError(InvalidSecurityRequestException.class)
                .verify();
    }
}