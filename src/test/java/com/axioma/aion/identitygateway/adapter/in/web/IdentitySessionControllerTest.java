package com.axioma.aion.identitygateway.adapter.in.web;

import com.axioma.aion.identitygateway.adapter.in.web.dto.CreateSessionRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.CreateSessionResponse;
import com.axioma.aion.identitygateway.adapter.in.web.mapper.CreateSessionWebMapper;
import com.axioma.aion.identitygateway.application.command.CreateSessionCommand;
import com.axioma.aion.identitygateway.domain.port.in.CreateSessionUseCase;
import com.axioma.aion.securitycore.model.AuthContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentitySessionControllerTest {

    @Mock
    private CreateSessionUseCase createSessionUseCase;
    @Mock
    private CreateSessionWebMapper createSessionWebMapper;

    @Test
    void createSession_shouldDelegateToUseCaseAndReturnResponse() {
        IdentitySessionController controller = new IdentitySessionController(createSessionUseCase, createSessionWebMapper);

        UUID authenticationId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        CreateSessionRequest request = CreateSessionRequest.builder().authenticationId(authenticationId).build();
        CreateSessionCommand command = new CreateSessionCommand(authenticationId);
        CreateSessionResponse response = CreateSessionResponse.builder()
                .sessionId(sessionId)
                .sessionToken("jwt-token")
                .authContext(mock(AuthContext.class))
                .build();

        when(createSessionWebMapper.toCommand(request)).thenReturn(command);
        when(createSessionUseCase.createSession(command)).thenReturn(Mono.just(response));

        StepVerifier.create(controller.createSession(request))
                .assertNext(actual -> {
                    assertEquals(sessionId, actual.sessionId());
                    assertEquals("jwt-token", actual.sessionToken());
                })
                .verifyComplete();

        verify(createSessionWebMapper).toCommand(request);
        verify(createSessionUseCase).createSession(command);
    }

    @Test
    void createSession_shouldFailWhenRequestBodyIsNull() {
        IdentitySessionController controller = new IdentitySessionController(createSessionUseCase, createSessionWebMapper);

        StepVerifier.create(controller.createSession(null))
                .expectErrorSatisfies(error -> assertEquals("Request body is required", error.getMessage()))
                .verify();
    }
}
