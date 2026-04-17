package com.axioma.aion.identitygateway.adapter.out.security.provider;

import com.axioma.aion.identitygateway.domain.model.OAuthTokenValidationResult;
import com.axioma.aion.identitygateway.domain.port.out.OAuthTokenValidationPort;
import com.axioma.aion.securitycore.exception.AuthenticationFailedException;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import com.axioma.aion.securitycore.model.AuthContext;
import com.axioma.aion.securitycore.model.SecurityRequest;
import com.axioma.aion.securitycore.model.SecurityResult;
import com.axioma.aion.securitycore.port.SecurityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuthSecurityProvider implements SecurityProvider {

    private static final String PROVIDER_NAME = "oauth";
    private static final String SUPPORTED_CHANNEL = "api";
    private static final String SUPPORTED_AUTH_TYPE = "oauth";

    private final OAuthTokenValidationPort oauthTokenValidationPort;

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean supports(String channel, String authType) {
        return SUPPORTED_CHANNEL.equalsIgnoreCase(channel)
                && SUPPORTED_AUTH_TYPE.equalsIgnoreCase(authType);
    }

    @Override
    public Mono<SecurityResult> authenticate(SecurityRequest request) {
        if (request == null) {
            return Mono.error(new InvalidSecurityRequestException("Security request must not be null"));
        }

        if (!supports(request.channel(), request.authType())) {
            return Mono.error(new AuthenticationFailedException(
                    "Unsupported oauth authentication request for channel=" +
                            request.channel() + ", authType=" + request.authType()
            ));
        }

        String bearerToken = readString(request.safeCredentials(), "bearerToken");

        if (isBlank(bearerToken)) {
            return Mono.error(new InvalidSecurityRequestException("bearerToken must not be blank"));
        }

        return oauthTokenValidationPort.validate(bearerToken)
                .switchIfEmpty(Mono.error(new AuthenticationFailedException("OAuth validation returned empty result")))
                .flatMap(this::toSecurityResult);
    }

    private Mono<SecurityResult> toSecurityResult(OAuthTokenValidationResult result) {
        if (!result.allowed()) {
            return Mono.error(new AuthenticationFailedException("OAuth authentication not allowed"));
        }

        if (isBlank(result.tenantId())) {
            return Mono.error(new AuthenticationFailedException("OAuth validation returned invalid tenantId"));
        }

        if (isBlank(result.subject())) {
            return Mono.error(new AuthenticationFailedException("OAuth validation returned invalid subject"));
        }

        AuthContext authContext = AuthContext.builder()
                .tenantId(result.tenantId())
                .subject(result.subject())
                .channel(safe(result.channel(), SUPPORTED_CHANNEL))
                .provider(PROVIDER_NAME)
                .authenticated(true)
                .authorities(result.authorities() != null ? result.authorities() : List.of())
                .attributes(result.attributes() != null ? result.attributes() : Map.of())
                .build();

        return Mono.just(SecurityResult.success(authContext));
    }

    private String readString(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String safe(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}