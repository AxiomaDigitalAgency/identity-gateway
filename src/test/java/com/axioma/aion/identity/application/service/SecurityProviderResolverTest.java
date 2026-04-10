package com.axioma.aion.identity.application.service;

import com.axioma.aion.identity.config.IdentityProperties;
import com.axioma.aion.identity.domain.exception.SecurityProviderNotFoundException;
import com.axioma.aion.identity.domain.model.SecurityMode;
import com.axioma.aion.identity.domain.port.out.SecurityProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityProviderResolverTest {

    @Test
    void should_resolve_channel_provider_when_mode_is_channel() {
        SecurityProvider channelProvider = mock(SecurityProvider.class);
        SecurityProvider oauthProvider = mock(SecurityProvider.class);

        when(channelProvider.supports()).thenReturn(SecurityMode.CHANNEL);
        when(oauthProvider.supports()).thenReturn(SecurityMode.OAUTH);

        IdentityProperties properties = new IdentityProperties();
        properties.setMode(SecurityMode.CHANNEL);

        SecurityProviderResolver resolver = new SecurityProviderResolver(
                List.of(channelProvider, oauthProvider),
                properties
        );

        SecurityProvider result = resolver.resolve();

        assertSame(channelProvider, result);
    }

    @Test
    void should_throw_exception_when_no_provider_matches_mode() {
        SecurityProvider oauthProvider = mock(SecurityProvider.class);
        when(oauthProvider.supports()).thenReturn(SecurityMode.OAUTH);

        IdentityProperties properties = new IdentityProperties();
        properties.setMode(SecurityMode.CHANNEL);

        SecurityProviderResolver resolver = new SecurityProviderResolver(
                List.of(oauthProvider),
                properties
        );

        assertThrows(SecurityProviderNotFoundException.class, resolver::resolve);
    }
}