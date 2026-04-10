package com.axioma.aion.identity.adapter.in.web.mapper;

import com.axioma.aion.identity.adapter.in.web.dto.AuthenticateRequestDto;
import com.axioma.aion.identity.adapter.in.web.dto.AuthenticateResponseDto;
import com.axioma.aion.identity.domain.model.AuthenticationResult;
import com.axioma.aion.identity.domain.model.SecurityRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IdentityWebMapper {

    // =========================
    // REQUEST → DOMAIN
    // =========================
    @Mapping(target = "clientIp", source = "forwardedFor")
    @Mapping(target = "userAgent", source = "userAgent")
    SecurityRequest toDomain(
            AuthenticateRequestDto dto,
            String forwardedFor,
            String userAgent
    );

    // =========================
    // SUCCESS RESPONSE
    // =========================
    @Mapping(target = "success", constant = "true")
    @Mapping(target = "tenantId", source = "authContext.tenantId")
    @Mapping(target = "subject", source = "authContext.subject")
    @Mapping(target = "channel", source = "authContext.channel")
    @Mapping(target = "authType", source = "authContext.authType")
    @Mapping(target = "scopes", source = "authContext.scopes")
    @Mapping(target = "claims", source = "authContext.claims")
    @Mapping(target = "errorCode", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    AuthenticateResponseDto toSuccessResponse(AuthenticationResult result);

    // =========================
    // ERROR RESPONSE
    // =========================
    @Mapping(target = "success", constant = "false")
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "channel", ignore = true)
    @Mapping(target = "authType", ignore = true)
    @Mapping(target = "scopes", ignore = true)
    @Mapping(target = "claims", ignore = true)
    @Mapping(target = "errorCode", source = "errorCode")
    @Mapping(target = "errorMessage", source = "errorMessage")
    AuthenticateResponseDto toErrorResponse(AuthenticationResult result);

    // =========================
    // HELPERS
    // =========================
    default String extractClientIp(String forwardedFor) {
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return null;
        }
        String[] values = forwardedFor.split(",");
        return values.length > 0 ? values[0].trim() : null;
    }
}