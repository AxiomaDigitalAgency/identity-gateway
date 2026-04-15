package com.axioma.aion.identity.adapter.out.security.channel;

import com.axioma.aion.identity.domain.exception.InvalidChannelAuthenticationException;
import com.axioma.aion.identity.domain.exception.MissingChannelCredentialsException;
import com.axioma.aion.identity.domain.model.AuthContext;
import com.axioma.aion.identity.domain.model.AuthenticationResult;
import com.axioma.aion.identity.domain.model.SecurityMode;
import com.axioma.aion.identity.domain.model.SecurityRequest;
import com.axioma.aion.identity.domain.model.SessionValidationResult;
import com.axioma.aion.identity.domain.model.WidgetValidationResult;
import com.axioma.aion.identity.domain.port.out.SecurityProvider;
import com.axioma.aion.identity.domain.port.out.SessionTokenPort;
import com.axioma.aion.identity.domain.port.out.WidgetCredentialValidationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChannelSecurityProvider implements SecurityProvider {

    private static final String AUTH_TYPE = "channel";
    private static final String CHANNEL_ACCESS_SCOPE = "channel:access";

    private final WidgetCredentialValidationPort widgetCredentialValidationPort;
    private final SessionTokenPort sessionTokenPort;

    @Override
    public SecurityMode supports() {
        return SecurityMode.CHANNEL;
    }

    @Override
    public AuthenticationResult authenticate(SecurityRequest request) {
        validateRequest(request);

        if (hasText(request.sessionToken())) {
            return authenticateBySessionToken(request);
        }

        if (hasText(request.widgetKey()) && hasText(request.origin())) {
            return authenticateByWidgetCredentials(request);
        }

        throw new MissingChannelCredentialsException(
                "Channel authentication requires either sessionToken or widgetKey with origin"
        );
    }

    private AuthenticationResult authenticateBySessionToken(SecurityRequest request) {
        SessionValidationResult validationResult = sessionTokenPort.validate(
                request.sessionToken(),
                request.channel()
        );

        if (!validationResult.valid()) {
            throw new InvalidChannelAuthenticationException(
                    buildErrorMessage(
                            validationResult.errorMessage(),
                            "Invalid session token for channel authentication"
                    )
            );
        }

        AuthContext authContext = AuthContext.builder()
                .authenticated(true)
                .tenantId(validationResult.tenantId())
                .subject(validationResult.subject())
                .channel(hasText(validationResult.channel()) ? validationResult.channel() : request.channel())
                .authType(hasText(validationResult.authType()) ? validationResult.authType() : AUTH_TYPE)
                .scopes(List.of(CHANNEL_ACCESS_SCOPE))
                .claims(Map.of(
                        "authentication_method", "session_token"
                ))
                .build();

        return AuthenticationResult.builder()
                .success(true)
                .authContext(authContext)
                .build();
    }

    private AuthenticationResult authenticateByWidgetCredentials(SecurityRequest request) {
        WidgetValidationResult validationResult = widgetCredentialValidationPort.validate(
                request.widgetKey(),
                request.origin(),
                request.channel()
        );

        if (!validationResult.valid()) {
            throw new InvalidChannelAuthenticationException(
                    buildErrorMessage(
                            validationResult.errorMessage(),
                            "Invalid widget credentials for channel authentication"
                    )
            );
        }

        AuthContext authContext = AuthContext.builder()
                .authenticated(true)
                .tenantId(validationResult.tenantId())
                .subject(request.widgetKey())
                .channel(hasText(validationResult.channel()) ? validationResult.channel() : request.channel())
                .authType(AUTH_TYPE)
                .scopes(List.of(CHANNEL_ACCESS_SCOPE))
                .claims(Map.of(
                        "authentication_method", "widget_key",
                        "origin", request.origin(),
                        "widgetKey", request.widgetKey()
                ))
                .build();

        return AuthenticationResult.builder()
                .success(true)
                .authContext(authContext)
                .build();
    }

    private void validateRequest(SecurityRequest request) {
        if (request == null) {
            throw new MissingChannelCredentialsException("Security request is required");
        }

        if (!hasText(request.channel())) {
            throw new MissingChannelCredentialsException("Channel is required for channel authentication");
        }
    }

    private String buildErrorMessage(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}