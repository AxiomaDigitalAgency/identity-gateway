package com.axioma.aion.identitygateway.adapter.out.security.provider;

import com.axioma.aion.identitygateway.domain.model.WidgetIdentityValidationResult;
import com.axioma.aion.identitygateway.domain.port.out.WidgetIdentityValidationPort;
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
public class WidgetSecurityProvider implements SecurityProvider {

    private static final String PROVIDER_NAME = "widget";
    private static final String SUPPORTED_CHANNEL = "web";
    private static final String SUPPORTED_AUTH_TYPE = "widget";

    private final WidgetIdentityValidationPort widgetIdentityValidationPort;

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
                    "Unsupported widget authentication request for channel=" +
                            request.channel() + ", authType=" + request.authType()
            ));
        }

        String widgetKey = readString(request.safeCredentials(), "widgetKey");
        String origin = readString(request.safeCredentials(), "origin");

        if (isBlank(widgetKey)) {
            return Mono.error(new InvalidSecurityRequestException("widgetKey must not be blank"));
        }

        if (isBlank(origin)) {
            return Mono.error(new InvalidSecurityRequestException("origin must not be blank"));
        }

        return widgetIdentityValidationPort.validate(widgetKey, origin)
                .switchIfEmpty(Mono.error(new AuthenticationFailedException("Widget identity not found")))
                .flatMap(this::toSecurityResult);
    }

    private Mono<SecurityResult> toSecurityResult(WidgetIdentityValidationResult result) {

        if (!result.allowed()) {
            return Mono.error(new AuthenticationFailedException("Widget authentication not allowed"));
        }

        if (isBlank(result.tenantId())) {
            return Mono.error(new AuthenticationFailedException("Invalid tenantId from widget validation"));
        }

        AuthContext authContext = AuthContext.builder()
                //.tenantId(result.tenantId())
                .subject("widget:" + safe(result.subjectValue()))
                .channel("web")
                //.provider(PROVIDER_NAME)
                //.authenticated(true)
                //.authorities(List.of())
                .attributes(Map.of(
                        "identityContextId", safe(result.identityContextId()),
                        "origin", safe(result.origin()),
                        "subjectType", safe(result.subjectType()),
                        "subjectValue", safe(result.subjectValue())
                ))
                .build();

        return Mono.just(SecurityResult.success(authContext));
    }

    private String readString(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String readIdentityContextId(Map<String, Object> source) {
        Object camelCaseValue = source.get("identityContextId");
        if (camelCaseValue != null) {
            return String.valueOf(camelCaseValue);
        }
        Object snakeCaseValue = source.get("identity_context_id");
        return snakeCaseValue == null ? null : String.valueOf(snakeCaseValue);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
