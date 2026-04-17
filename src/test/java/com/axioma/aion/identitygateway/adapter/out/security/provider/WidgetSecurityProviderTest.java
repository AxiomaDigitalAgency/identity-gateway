package com.axioma.aion.identitygateway.adapter.out.security.provider;

import com.axioma.aion.identitygateway.domain.model.WidgetIdentityValidationResult;
import com.axioma.aion.identitygateway.domain.port.out.WidgetIdentityValidationPort;
import com.axioma.aion.securitycore.exception.AuthenticationFailedException;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import com.axioma.aion.securitycore.model.SecurityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WidgetSecurityProviderTest {

    @Mock
    private WidgetIdentityValidationPort validationPort;

    private WidgetSecurityProvider provider;

    @BeforeEach
    void setUp() {
        provider = new WidgetSecurityProvider(validationPort);
    }

    @Test
    void shouldAuthenticateSuccessfully() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .authType("widget")
                .credentials(Map.of(
                        "widgetKey", "widget-demo",
                        "origin", "http://localhost:3000"
                ))
                .build();

        when(validationPort.validate("widget-demo", "http://localhost:3000"))
                .thenReturn(Mono.just(
                        WidgetIdentityValidationResult.builder()
                                .tenantId("tenant-demo")
                                .identityContextId("ctx-1")
                                .subjectType("WIDGET")
                                .subjectValue("widget-demo")
                                .allowed(true)
                                .origin("http://localhost:3000")
                                .build()
                ));

        StepVerifier.create(provider.authenticate(request))
                .assertNext(result -> {
                    assert result.authenticated();
                    assert result.authContext().tenantId().equals("tenant-demo");
                    assert result.authContext().provider().equals("widget");
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenWidgetKeyIsBlank() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .authType("widget")
                .credentials(Map.of(
                        "widgetKey", "",
                        "origin", "http://localhost:3000"
                ))
                .build();

        StepVerifier.create(provider.authenticate(request))
                .expectError(InvalidSecurityRequestException.class)
                .verify();
    }

    @Test
    void shouldFailWhenOriginIsBlank() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .authType("widget")
                .credentials(Map.of(
                        "widgetKey", "widget-demo",
                        "origin", ""
                ))
                .build();

        StepVerifier.create(provider.authenticate(request))
                .expectError(InvalidSecurityRequestException.class)
                .verify();
    }

    @Test
    void shouldFailWhenValidationIsNotAllowed() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .authType("widget")
                .credentials(Map.of(
                        "widgetKey", "widget-demo",
                        "origin", "http://localhost:3000"
                ))
                .build();

        when(validationPort.validate(any(), any()))
                .thenReturn(Mono.just(
                        WidgetIdentityValidationResult.builder()
                                .allowed(false)
                                .build()
                ));

        StepVerifier.create(provider.authenticate(request))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }

    @Test
    void shouldFailWhenUnsupportedChannel() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("api")
                .authType("widget")
                .credentials(Map.of(
                        "widgetKey", "widget-demo",
                        "origin", "http://localhost:3000"
                ))
                .build();

        StepVerifier.create(provider.authenticate(request))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }
}