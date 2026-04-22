package com.axioma.aion.identitygateway.adapter.out.persistence;


import com.axioma.aion.identitygateway.adapter.out.repository.IdentityOriginRuleRepository;
import com.axioma.aion.identitygateway.domain.port.out.OriginRulePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OriginRulePersistenceAdapter implements OriginRulePort {

    private final IdentityOriginRuleRepository repository;

    @Override
    public Mono<Boolean> isAllowed(UUID credentialId, String origin) {
        return repository.findByIdentityCredentialId(credentialId)
                .filter(rule -> "ACTIVE".equalsIgnoreCase(rule.getStatus()))
                .any(rule -> originMatches(rule.getAllowedOrigin(), origin));
    }

    private boolean originMatches(String allowed, String actual) {
        if (allowed == null || actual == null) return false;
        return actual.equalsIgnoreCase(allowed);
    }
}