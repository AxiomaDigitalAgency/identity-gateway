package com.axioma.aion.identitygateway.domain.port.in;

import com.axioma.aion.identitygateway.application.command.AuthenticateCommand;
import com.axioma.aion.identitygateway.adapter.in.web.dto.AuthenticateResponse;
import reactor.core.publisher.Mono;

public interface AuthenticateIdentityUseCase {
    Mono<AuthenticateResponse> authenticate(AuthenticateCommand command);
}