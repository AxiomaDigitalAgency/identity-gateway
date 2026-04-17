package com.axioma.aion.identitygateway.adapter.out.security.oauth;

import com.axioma.aion.identitygateway.domain.model.OAuthTokenValidationResult;
import com.axioma.aion.identitygateway.domain.port.out.OAuthTokenValidationPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Primary
public class InMemoryOAuthTokenValidationAdapter implements OAuthTokenValidationPort {

    @Override
    public Mono<OAuthTokenValidationResult> validate(String bearerToken) {
        if ("valid-token".equals(bearerToken)) {
            return Mono.just(OAuthTokenValidationResult.builder()
                    .tenantId("tenant-api")
                    .subject("user:123")
                    .channel("api")
                    .allowed(true)
                    .authorities(List.of("read"))
                    .attributes(Map.of("source", "in-memory"))
                    .build());
        }

        return Mono.just(OAuthTokenValidationResult.builder()
                .allowed(false)
                .build());
    }
}