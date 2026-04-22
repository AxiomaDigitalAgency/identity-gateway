package com.axioma.aion.identitygateway.adapter.in.web.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateSessionRequest(
        UUID authenticationId
) {}