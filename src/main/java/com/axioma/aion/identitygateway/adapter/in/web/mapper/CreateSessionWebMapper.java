package com.axioma.aion.identitygateway.adapter.in.web.mapper;

import com.axioma.aion.identitygateway.adapter.in.web.dto.CreateSessionRequest;
import com.axioma.aion.identitygateway.application.command.CreateSessionCommand;
import org.springframework.stereotype.Component;

@Component
public class CreateSessionWebMapper {

    public CreateSessionCommand toCommand(CreateSessionRequest request) {
        return new CreateSessionCommand(request.authenticationId());
    }
}