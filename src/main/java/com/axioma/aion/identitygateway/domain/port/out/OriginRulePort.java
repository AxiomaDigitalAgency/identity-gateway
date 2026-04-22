package com.axioma.aion.identitygateway.domain.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OriginRulePort {
    Mono<Boolean> isAllowed(UUID credentialId, String origin);
}