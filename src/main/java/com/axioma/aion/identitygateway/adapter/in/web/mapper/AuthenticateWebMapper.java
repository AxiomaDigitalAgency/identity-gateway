package com.axioma.aion.identitygateway.adapter.in.web.mapper;

import com.axioma.aion.identitygateway.adapter.in.web.dto.AuthenticateRequest;
import com.axioma.aion.identitygateway.application.command.AuthenticateCommand;
import org.springframework.stereotype.Component;

@Component
public class AuthenticateWebMapper {

    public AuthenticateCommand toCommand(AuthenticateRequest request) {
        return new AuthenticateCommand(
                request.authenticationType(),
                request.credential() != null ? request.credential().credentialId() : null,
                request.credential() != null ? request.credential().credentialKey() : null,
                request.credential() != null ? request.credential().clientId() : null,
                request.credential() != null ? request.credential().clientSecret() : null,
                request.credential() != null ? request.credential().token() : null,
                request.channelContext() != null ? request.channelContext().channel() : null,
                request.channelContext() != null ? request.channelContext().origin() : null,
                request.channelContext() != null ? request.channelContext().provider() : null,
                request.subjectContext() != null ? request.subjectContext().subject() : null,
                request.requestMetadata() != null ? request.requestMetadata().ipAddress() : null,
                request.requestMetadata() != null ? request.requestMetadata().userAgent() : null,
                request.requestMetadata() != null ? request.requestMetadata().requestId() : null,
                request.requestMetadata() != null ? request.requestMetadata().providerMessageId() : null
        );
    }
}