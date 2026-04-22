package com.axioma.aion.identitygateway.adapter.in.web.dto;

import com.axioma.aion.securitycore.model.AuthenticationType;
import lombok.Builder;

@Builder
public record AuthenticateRequest(
        AuthenticationType authenticationType,
        CredentialPayload credential,
        ChannelContext channelContext,
        SubjectContext subjectContext,
        RequestMetadata requestMetadata
) {}