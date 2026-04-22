package com.axioma.aion.identitygateway.domain.port.in;

import com.axioma.aion.identitygateway.adapter.in.web.dto.CreateSessionResponse;
import com.axioma.aion.identitygateway.application.command.CreateSessionCommand;
import reactor.core.publisher.Mono;

public interface CreateSessionUseCase {

    Mono<CreateSessionResponse> createSession(CreateSessionCommand command);
}