package com.axioma.aion.identitygateway.domain.port.out;

import com.axioma.aion.securitycore.model.AuthContext;
import reactor.core.publisher.Mono;

public interface JwtSessionTokenPort {

    Mono<String> generate(AuthContext authContext);
}