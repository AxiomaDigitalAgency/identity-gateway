package com.axioma.aion.identitygateway.application.command;

import java.util.UUID;

public record CreateSessionCommand(
        UUID authenticationId
) {}