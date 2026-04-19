package com.axioma.aion.identitygateway.adapter.out.security;

import com.axioma.aion.identitygateway.config.SecurityRoutingProperties;
import com.axioma.aion.identitygateway.domain.port.out.SecurityProviderResolver;
import com.axioma.aion.securitycore.exception.UnsupportedSecurityProviderException;
import com.axioma.aion.securitycore.port.SecurityProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SecurityProviderResolverImpl implements SecurityProviderResolver {

    private final List<SecurityProvider> securityProviders;
    private final SecurityRoutingProperties securityRoutingProperties;

    public SecurityProviderResolverImpl(
            List<SecurityProvider> securityProviders,
            SecurityRoutingProperties securityRoutingProperties
    ) {
        this.securityProviders = securityProviders;
        this.securityRoutingProperties = securityRoutingProperties;
    }

    @Override
    public SecurityProvider resolve(String channel, String authType) {
        log.debug("Resolving security provider for channel: {}, authType: {}", channel, authType);
        String configuredProviderName = securityRoutingProperties.getProviderRouting().get(channel);

        if (configuredProviderName == null || configuredProviderName.isBlank()) {
            throw new UnsupportedSecurityProviderException(
                    "No security provider configured for channel: " + channel
            );
        }

        return securityProviders.stream()
                .filter(provider -> provider.providerName().equalsIgnoreCase(configuredProviderName))
                .filter(provider -> provider.supports(channel, authType))
                .findFirst()
                .orElseThrow(() -> new UnsupportedSecurityProviderException(
                        "No matching security provider found for channel: " + channel +
                                ", authType: " + authType +
                                ", configuredProvider: " + configuredProviderName
                ));
    }
}