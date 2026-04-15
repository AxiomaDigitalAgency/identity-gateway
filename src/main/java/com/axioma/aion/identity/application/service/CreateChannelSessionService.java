package com.axioma.aion.identity.application.service;

import com.axioma.aion.identity.domain.exception.InvalidChannelSessionBootstrapException;
import com.axioma.aion.identity.domain.exception.MissingChannelCredentialsException;
import com.axioma.aion.identity.domain.model.ChannelSession;
import com.axioma.aion.identity.domain.model.ChannelSessionResult;
import com.axioma.aion.identity.domain.model.ChannelSessionToken;
import com.axioma.aion.identity.domain.model.CreateChannelSessionRequest;
import com.axioma.aion.identity.domain.model.WidgetValidationResult;
import com.axioma.aion.identity.domain.port.in.CreateChannelSessionUseCase;
import com.axioma.aion.identity.domain.port.out.ChannelSessionTokenPort;
import com.axioma.aion.identity.domain.port.out.WidgetCredentialValidationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateChannelSessionService implements CreateChannelSessionUseCase {

    private static final String AUTH_TYPE = "channel-session";

    private final WidgetCredentialValidationPort widgetCredentialValidationPort;
    private final ChannelSessionTokenPort channelSessionTokenPort;

    @Override
    public ChannelSessionResult create(CreateChannelSessionRequest request) {
        validateRequest(request);

        WidgetValidationResult validationResult = widgetCredentialValidationPort.validate(
                request.widgetKey(),
                request.origin(),
                request.channel()
        );

        if (!validationResult.valid()) {
            throw new InvalidChannelSessionBootstrapException(
                    buildErrorMessage(
                            validationResult.errorMessage(),
                            "Invalid widget credentials for channel session creation"
                    )
            );
        }

        String tenantId = validationResult.tenantId();
        String channel = hasText(validationResult.channel()) ? validationResult.channel() : request.channel();

        Instant now = Instant.now();

        ChannelSession session = ChannelSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .channel(channel)
                .widgetKey(request.widgetKey())
                .origin(request.origin())
                .issuedAt(now)
                .expiresAt(null)
                .build();

        ChannelSessionToken sessionToken = channelSessionTokenPort.generate(session);

        return ChannelSessionResult.builder()
                .sessionToken(sessionToken.token())
                .tokenType(sessionToken.tokenType())
                .expiresIn(sessionToken.expiresIn())
                .tenantId(tenantId)
                .subject(session.sessionId())
                .channel(channel)
                .authType(AUTH_TYPE)
                .build();
    }

    private void validateRequest(CreateChannelSessionRequest request) {
        if (request == null) {
            throw new MissingChannelCredentialsException("Channel session request is required");
        }

        if (!hasText(request.channel())) {
            throw new MissingChannelCredentialsException("Channel is required for channel session creation");
        }

        if (!hasText(request.widgetKey())) {
            throw new MissingChannelCredentialsException("Widget key is required for channel session creation");
        }

        if (!hasText(request.origin())) {
            throw new MissingChannelCredentialsException("Origin is required for channel session creation");
        }
    }

    private String buildErrorMessage(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}