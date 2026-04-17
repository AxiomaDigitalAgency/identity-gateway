package com.axioma.aion.identitygateway.adapter.out.security.provider;

import com.axioma.aion.identitygateway.domain.model.OAuthTokenValidationResult;
import com.axioma.aion.identitygateway.domain.port.out.OAuthTokenValidationPort;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthSecurityProviderTest {

    @Mock
    private OAuthTokenValidationPort oauthTokenValidationPort;

    private OAuthSecurityProvider provider;

    @BeforeEach
    void setUp() {
        provider = new OAuthSecurityProvider(oauthTokenValidationPort);
    }

    @Test
    void shouldAuthenticateSuccessfully() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("api")
                .authType("oauth")
                .credentials(Map.of("bearerToken", "valid-token"))
                .build();

        when(oauthTokenValidationPort.validate("valid-token"))
                .thenReturn(Mono.just(
                        OAuthTokenValidationResult.builder()
                                .tenantId("tenant-api")
                                .subject("user:123")
                                .channel("api")
                                .allowed(true)
                                .authorities(List.of("read", "write"))
                                .attributes(Map.of("issuer", "https://issuer.example.com"))
                                .build()
                ));

        StepVerifier.create(provider.authenticate(request))
                .assertNext(result -> {
                    assert result.authenticated();
                    assert "tenant-api".equals(result.authContext().tenantId());
                    assert "user:123".equals(result.authContext().subject());
                    assert "oauth".equals(result.authContext().provider());
                    assert "api".equals(result.authContext().channel());
                    assert result.authContext().authorities().size() == 2;
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenBearerTokenIsBlank() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("api")
                .authType("oauth")
                .credentials(Map.of("bearerToken", " "))
                .build();

        StepVerifier.create(provider.authenticate(request))
                .expectError(InvalidSecurityRequestException.class)
                .verify();
    }

    @Test
    void shouldFailWhenUnsupportedRequest() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .authType("oauth")
                .credentials(Map.of("bearerToken", "valid-token"))
                .build();

        StepVerifier.create(provider.authenticate(request))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }

    @Test
    void shouldFailWhenValidationIsNotAllowed() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("api")
                .authType("oauth")
                .credentials(Map.of("bearerToken", "valid-token"))
                .build();

        when(oauthTokenValidationPort.validate("valid-token"))
                .thenReturn(Mono.just(
                        OAuthTokenValidationResult.builder()
                                .allowed(false)
                                .build()
                ));

        StepVerifier.create(provider.authenticate(request))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }

    @Test
    void shouldFailWhenTenantIdIsMissing() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("api")
                .authType("oauth")
                .credentials(Map.of("bearerToken", "valid-token"))
                .build();

        when(oauthTokenValidationPort.validate("valid-token"))
                .thenReturn(Mono.just(
                        OAuthTokenValidationResult.builder()
                                .allowed(true)
                                .tenantId(null)
                                .subject("user:123")
                                .channel("api")
                                .build()
                ));

        StepVerifier.create(provider.authenticate(request))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }

    @Test
    void shouldFailWhenSubjectIsMissing() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("api")
                .authType("oauth")
                .credentials(Map.of("bearerToken", "valid-token"))
                .build();

        when(oauthTokenValidationPort.validate("valid-token"))
                .thenReturn(Mono.just(
                        OAuthTokenValidationResult.builder()
                                .allowed(true)
                                .tenantId("tenant-api")
                                .subject(null)
                                .channel("api")
                                .build()
                ));

        StepVerifier.create(provider.authenticate(request))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }

    @Test
    void shouldFailWhenValidationReturnsEmpty() {
        SecurityRequest request = SecurityRequest.builder()
                .channel("api")
                .authType("oauth")
                .credentials(Map.of("bearerToken", "valid-token"))
                .build();

        when(oauthTokenValidationPort.validate(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(provider.authenticate(request))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }
}