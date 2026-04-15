package com.axioma.aion.identity.adapter.out.security.channel;

import com.axioma.aion.identity.domain.exception.InvalidChannelAuthenticationException;
import com.axioma.aion.identity.domain.exception.MissingChannelCredentialsException;
import com.axioma.aion.identity.domain.model.AuthenticationResult;
import com.axioma.aion.identity.domain.model.SecurityRequest;
import com.axioma.aion.identity.domain.model.SessionValidationResult;
import com.axioma.aion.identity.domain.model.WidgetValidationResult;
import com.axioma.aion.identity.domain.port.out.SessionTokenPort;
import com.axioma.aion.identity.domain.port.out.WidgetCredentialValidationPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChannelSecurityProviderTest {

    @Test
    void should_authenticate_with_session_token_when_valid() {
        WidgetCredentialValidationPort widgetPort = mock(WidgetCredentialValidationPort.class);
        SessionTokenPort sessionTokenPort = mock(SessionTokenPort.class);

        when(sessionTokenPort.validate("test-session-token", "web"))
                .thenReturn(SessionValidationResult.builder()
                        .valid(true)
                        .tenantId("axioma-agency")
                        .subject("session-user")
                        .channel("web")
                        .authType("channel-session")
                        .build());

        ChannelSecurityProvider provider = new ChannelSecurityProvider(widgetPort, sessionTokenPort);

        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .sessionToken("test-session-token")
                .build();

        AuthenticationResult result = provider.authenticate(request);

        assertTrue(result.success());
        assertNotNull(result.authContext());
        assertEquals("axioma-agency", result.authContext().tenantId());
        assertEquals("session-user", result.authContext().subject());
        assertEquals("web", result.authContext().channel());
        assertEquals("channel-session", result.authContext().authType());

        verify(sessionTokenPort).validate("test-session-token", "web");
        verifyNoInteractions(widgetPort);
    }

    @Test
    void should_authenticate_with_widget_credentials_when_valid() {
        WidgetCredentialValidationPort widgetPort = mock(WidgetCredentialValidationPort.class);
        SessionTokenPort sessionTokenPort = mock(SessionTokenPort.class);

        when(widgetPort.validate("test-widget-key", "http://localhost:3000", "web"))
                .thenReturn(WidgetValidationResult.builder()
                        .valid(true)
                        .tenantId("axioma-agency")
                        .widgetKey("test-widget-key")
                        .channel("web")
                        .build());

        ChannelSecurityProvider provider = new ChannelSecurityProvider(widgetPort, sessionTokenPort);

        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .widgetKey("test-widget-key")
                .origin("http://localhost:3000")
                .build();

        AuthenticationResult result = provider.authenticate(request);

        assertTrue(result.success());
        assertNotNull(result.authContext());
        assertEquals("axioma-agency", result.authContext().tenantId());
        assertEquals("test-widget-key", result.authContext().subject());
        assertEquals("web", result.authContext().channel());
        assertEquals("channel", result.authContext().authType());

        verify(widgetPort).validate("test-widget-key", "http://localhost:3000", "web");
        verifyNoInteractions(sessionTokenPort);
    }

    @Test
    void should_throw_bad_request_when_credentials_are_missing() {
        WidgetCredentialValidationPort widgetPort = mock(WidgetCredentialValidationPort.class);
        SessionTokenPort sessionTokenPort = mock(SessionTokenPort.class);

        ChannelSecurityProvider provider = new ChannelSecurityProvider(widgetPort, sessionTokenPort);

        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .build();

        assertThrows(MissingChannelCredentialsException.class, () -> provider.authenticate(request));
    }

    @Test
    void should_throw_bad_request_when_channel_is_missing() {
        WidgetCredentialValidationPort widgetPort = mock(WidgetCredentialValidationPort.class);
        SessionTokenPort sessionTokenPort = mock(SessionTokenPort.class);

        ChannelSecurityProvider provider = new ChannelSecurityProvider(widgetPort, sessionTokenPort);

        SecurityRequest request = SecurityRequest.builder()
                .widgetKey("test-widget-key")
                .origin("http://localhost:3000")
                .build();

        assertThrows(MissingChannelCredentialsException.class, () -> provider.authenticate(request));
    }

    @Test
    void should_throw_unauthorized_when_widget_credentials_are_invalid() {
        WidgetCredentialValidationPort widgetPort = mock(WidgetCredentialValidationPort.class);
        SessionTokenPort sessionTokenPort = mock(SessionTokenPort.class);

        when(widgetPort.validate("bad-key", "http://localhost:3000", "web"))
                .thenReturn(WidgetValidationResult.builder()
                        .valid(false)
                        .errorCode("INVALID_WIDGET_CREDENTIALS")
                        .errorMessage("Widget key or origin is invalid")
                        .build());

        ChannelSecurityProvider provider = new ChannelSecurityProvider(widgetPort, sessionTokenPort);

        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .widgetKey("bad-key")
                .origin("http://localhost:3000")
                .build();

        assertThrows(InvalidChannelAuthenticationException.class, () -> provider.authenticate(request));
    }

    @Test
    void should_throw_unauthorized_when_session_token_is_invalid() {
        WidgetCredentialValidationPort widgetPort = mock(WidgetCredentialValidationPort.class);
        SessionTokenPort sessionTokenPort = mock(SessionTokenPort.class);

        when(sessionTokenPort.validate("bad-session-token", "web"))
                .thenReturn(SessionValidationResult.builder()
                        .valid(false)
                        .errorCode("INVALID_SESSION_TOKEN")
                        .errorMessage("Session token is invalid")
                        .build());

        ChannelSecurityProvider provider = new ChannelSecurityProvider(widgetPort, sessionTokenPort);

        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .sessionToken("bad-session-token")
                .build();

        assertThrows(InvalidChannelAuthenticationException.class, () -> provider.authenticate(request));
    }
}