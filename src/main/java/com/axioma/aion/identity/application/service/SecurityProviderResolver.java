package com.axioma.aion.identity.application.service;

import com.axioma.aion.identity.config.IdentityProperties;
import com.axioma.aion.identity.domain.exception.SecurityProviderNotFoundException;
import com.axioma.aion.identity.domain.port.out.SecurityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityProviderResolver {

    private final List<SecurityProvider> securityProviders;
    private final IdentityProperties identityProperties;

    public SecurityProvider resolve() {
        return securityProviders.stream()
                .filter(provider -> provider.supports() == identityProperties.getMode())
                .findFirst()
                .orElseThrow(() -> new SecurityProviderNotFoundException(
                        "No SecurityProvider found for mode: " + identityProperties.getMode()
                ));
    }
}