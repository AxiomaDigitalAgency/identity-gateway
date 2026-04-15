package com.axioma.aion.identity.adapter.out.security.channel;

import com.axioma.aion.identity.config.JwtSessionProperties;
import com.axioma.aion.identity.domain.model.ChannelSession;
import com.axioma.aion.identity.domain.model.ChannelSessionToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtChannelSessionTokenAdapterTest {

    private JwtEncoder sessionJwtEncoder;
    private JwtDecoder sessionJwtDecoder;
    private JwtSessionProperties properties;
    private JwtChannelSessionTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        sessionJwtEncoder = mock(JwtEncoder.class);
        sessionJwtDecoder = mock(JwtDecoder.class);

        properties = new JwtSessionProperties();
        properties.setIssuer("identity-gateway");
        properties.setTtlSeconds(3600L);

        adapter = new JwtChannelSessionTokenAdapter(sessionJwtEncoder, sessionJwtDecoder, properties);
    }

    @Test
    void generate_shouldBuildBearerToken() {
        Jwt encodedJwt = mock(Jwt.class);
        when(encodedJwt.getTokenValue()).thenReturn("signed-token");
        when(sessionJwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(encodedJwt);

        ChannelSession session = ChannelSession.builder()
                .sessionId("session-123")
                .tenantId("tenant-1")
                .channel("web")
                .widgetKey("widget-abc")
                .origin("http://localhost:3000")
                .build();

        ChannelSessionToken result = adapter.generate(session);

        assertNotNull(result);
        assertEquals("signed-token", result.token());
        assertEquals("Bearer", result.tokenType());
        assertEquals(3600L, result.expiresIn());
    }

    @Test
    void validateAndParse_shouldReturnSession_whenTokenIsValid() {
        Instant issuedAt = Instant.parse("2026-04-10T10:00:00Z");
        Instant expiresAt = issuedAt.plusSeconds(3600);

        Jwt jwt = Jwt.withTokenValue("signed-token")
                .header("alg", "HS256")
                .claim("session_type", "CHANNEL_SESSION")
                .claim("tenant_id", "tenant-1")
                .claim("channel", "web")
                .claim("widget_key", "widget-abc")
                .claim("origin", "http://localhost:3000")
                .subject("session-123")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        when(sessionJwtDecoder.decode("signed-token")).thenReturn(jwt);

        ChannelSession result = adapter.validateAndParse("signed-token");

        assertNotNull(result);
        assertEquals("session-123", result.sessionId());
        assertEquals("tenant-1", result.tenantId());
        assertEquals("web", result.channel());
        assertEquals("widget-abc", result.widgetKey());
        assertEquals("http://localhost:3000", result.origin());
        assertEquals(issuedAt, result.issuedAt());
        assertEquals(expiresAt, result.expiresAt());
    }

    @Test
    void validateAndParse_shouldThrowWhenSessionTypeIsInvalid() {
        Jwt jwt = Jwt.withTokenValue("signed-token")
                .header("alg", "HS256")
                .claim("session_type", "WRONG_TYPE")
                .subject("session-123")
                .build();

        when(sessionJwtDecoder.decode("signed-token")).thenReturn(jwt);

        JwtValidationException ex = assertThrows(
                JwtValidationException.class,
                () -> adapter.validateAndParse("signed-token")
        );

        assertTrue(ex.getMessage().contains("Invalid session token type"));
    }

    @Test
    void validateAndParse_shouldPropagateJwtException_whenDecoderFails() {
        when(sessionJwtDecoder.decode("bad-token"))
                .thenThrow(new JwtException("invalid token"));

        JwtException ex = assertThrows(
                JwtException.class,
                () -> adapter.validateAndParse("bad-token")
        );

        assertEquals("invalid token", ex.getMessage());
    }
}