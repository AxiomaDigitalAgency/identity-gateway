package com.axioma.aion.identitygateway.domain.port.out;


import com.axioma.aion.identitygateway.domain.model.AuthenticatedPrincipal;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AuthenticationStatePort {
    Mono<Void> save(AuthenticatedPrincipal principal);
    Mono<AuthenticatedPrincipal> findByAuthenticationId(UUID authenticationId);
    Mono<Void> markConsumed(UUID authenticationId);
    Mono<Boolean> isConsumed(UUID authenticationId);
    Mono<Void> delete(UUID authenticationId);
}