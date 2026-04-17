package com.axioma.aion.identitygateway.domain.port.out;

import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.JwtSessionClaims;
import reactor.core.publisher.Mono;

public interface JwtSessionProviderPort {

    Mono<String> generate(IdentitySession identitySession);

    Mono<JwtSessionClaims> parse(String sessionToken);
}