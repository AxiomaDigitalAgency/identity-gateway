package com.axioma.aion.identity.adapter.out.session;

import com.axioma.aion.identity.domain.model.ChannelSession;
import com.axioma.aion.identity.domain.model.SessionValidationResult;
import com.axioma.aion.identity.domain.port.out.ChannelSessionTokenPort;
import com.axioma.aion.identity.domain.port.out.SessionTokenPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelSessionValidationAdapter implements SessionTokenPort {

    private static final String AUTH_TYPE = "channel-session";

    private final ChannelSessionTokenPort channelSessionTokenPort;

    @Override
    public SessionValidationResult validate(String sessionToken, String channel) {
        try {
            ChannelSession session = channelSessionTokenPort.validateAndParse(sessionToken);

            if (!hasText(session.channel()) || !session.channel().equals(channel)) {
                return SessionValidationResult.builder()
                        .valid(false)
                        .errorCode("INVALID_SESSION_CHANNEL")
                        .errorMessage("Session token channel does not match request channel")
                        .build();
            }

            return SessionValidationResult.builder()
                    .valid(true)
                    .tenantId(session.tenantId())
                    .subject(session.sessionId())
                    .channel(session.channel())
                    .authType(AUTH_TYPE)
                    .build();

        } catch (Exception ex) {
            log.error("Session token validation failed", ex);

            return SessionValidationResult.builder()
                    .valid(false)
                    .errorCode("INVALID_SESSION_TOKEN")
                    .errorMessage("Session token is invalid")
                    .build();
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}