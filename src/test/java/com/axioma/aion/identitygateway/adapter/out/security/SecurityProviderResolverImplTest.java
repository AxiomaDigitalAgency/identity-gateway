package com.axioma.aion.identitygateway.adapter.out.security;

import com.axioma.aion.identitygateway.config.SecurityRoutingProperties;
import com.axioma.aion.securitycore.exception.UnsupportedSecurityProviderException;
import com.axioma.aion.securitycore.model.SecurityRequest;
import com.axioma.aion.securitycore.model.SecurityResult;
import com.axioma.aion.securitycore.port.SecurityProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityProviderResolverImplTest {

    private SecurityRoutingProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SecurityRoutingProperties();
        properties.setProviderRouting(Map.of(
                "web", "widget",
                "api", "oauth"
        ));
    }

    @Test
    void shouldResolveConfiguredProvider() {
        SecurityProvider widgetProvider = new FakeSecurityProvider("widget", true);
        SecurityProvider oauthProvider = new FakeSecurityProvider("oauth", true);

        SecurityProviderResolverImpl resolver = new SecurityProviderResolverImpl(
                List.of(widgetProvider, oauthProvider),
                properties
        );

        SecurityProvider resolved = resolver.resolve("web", "widget");

        assertEquals("widget", resolved.providerName());
    }

    @Test
    void shouldFailWhenNoProviderConfiguredForChannel() {
        SecurityProvider widgetProvider = new FakeSecurityProvider("widget", true);

        SecurityProviderResolverImpl resolver = new SecurityProviderResolverImpl(
                List.of(widgetProvider),
                properties
        );

        assertThrows(
                UnsupportedSecurityProviderException.class,
                () -> resolver.resolve("whatsapp", "widget")
        );
    }

    @Test
    void shouldFailWhenConfiguredProviderDoesNotExist() {
        SecurityProvider widgetProvider = new FakeSecurityProvider("widget", true);

        SecurityProviderResolverImpl resolver = new SecurityProviderResolverImpl(
                List.of(widgetProvider),
                properties
        );

        assertThrows(
                UnsupportedSecurityProviderException.class,
                () -> resolver.resolve("api", "oauth")
        );
    }

    @Test
    void shouldFailWhenProviderDoesNotSupportRequest() {
        SecurityProvider widgetProvider = new FakeSecurityProvider("widget", false);

        SecurityProviderResolverImpl resolver = new SecurityProviderResolverImpl(
                List.of(widgetProvider),
                properties
        );

        assertThrows(
                UnsupportedSecurityProviderException.class,
                () -> resolver.resolve("web", "widget")
        );
    }

    private static class FakeSecurityProvider implements SecurityProvider {

        private final String providerName;
        private final boolean supports;

        private FakeSecurityProvider(String providerName, boolean supports) {
            this.providerName = providerName;
            this.supports = supports;
        }

        @Override
        public String providerName() {
            return providerName;
        }

        @Override
        public boolean supports(String channel, String authType) {
            return supports;
        }

        @Override
        public Mono<SecurityResult> authenticate(SecurityRequest request) {
            return Mono.empty();
        }
    }
}