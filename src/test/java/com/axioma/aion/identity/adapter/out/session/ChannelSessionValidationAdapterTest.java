package com.axioma.aion.identity.adapter.out.session;

import com.axioma.aion.identity.domain.model.ChannelSession;
import com.axioma.aion.identity.domain.model.SessionValidationResult;
import com.axioma.aion.identity.domain.port.out.ChannelSessionTokenPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChannelSessionValidationAdapterTest {

    @Test
    void should_return_valid_result_when_token_is_valid_and_channel_matches() {
        ChannelSessionTokenPort tokenPort = mock(ChannelSessionTokenPort.class);

        ChannelSession session = ChannelSession.builder()
                .sessionId("session-123")
                .tenantId("novasmile-dental")
                .channel("web")
                .widgetKey("wk_novasmile_123")
                .origin("http://localhost:5173")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(tokenPort.validateAndParse("valid-token")).thenReturn(session);

        ChannelSessionValidationAdapter adapter = new ChannelSessionValidationAdapter(tokenPort);

        SessionValidationResult result = adapter.validate("valid-token", "web");

        assertTrue(result.valid());
        assertEquals("novasmile-dental", result.tenantId());
        assertEquals("session-123", result.subject());
        assertEquals("web", result.channel());
        assertEquals("channel-session", result.authType());
    }

    @Test
    void should_return_invalid_result_when_channel_does_not_match() {
        ChannelSessionTokenPort tokenPort = mock(ChannelSessionTokenPort.class);

        ChannelSession session = ChannelSession.builder()
                .sessionId("session-123")
                .tenantId("novasmile-dental")
                .channel("whatsapp")
                .build();

        when(tokenPort.validateAndParse("valid-token")).thenReturn(session);

        ChannelSessionValidationAdapter adapter = new ChannelSessionValidationAdapter(tokenPort);

        SessionValidationResult result = adapter.validate("valid-token", "web");

        assertFalse(result.valid());
        assertEquals("INVALID_SESSION_CHANNEL", result.errorCode());
    }

    @Test
    void should_return_invalid_result_when_token_is_invalid() {
        ChannelSessionTokenPort tokenPort = mock(ChannelSessionTokenPort.class);
        when(tokenPort.validateAndParse("bad-token")).thenThrow(new RuntimeException("invalid token"));

        ChannelSessionValidationAdapter adapter = new ChannelSessionValidationAdapter(tokenPort);

        SessionValidationResult result = adapter.validate("bad-token", "web");

        assertFalse(result.valid());
        assertEquals("INVALID_SESSION_TOKEN", result.errorCode());
    }
}