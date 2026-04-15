package com.axioma.aion.identity.adapter.in.web.mapper;

import com.axioma.aion.identity.adapter.in.web.dto.CreateChannelSessionRequestDto;
import com.axioma.aion.identity.adapter.in.web.dto.CreateChannelSessionResponseDto;
import com.axioma.aion.identity.domain.model.ChannelSessionResult;
import com.axioma.aion.identity.domain.model.CreateChannelSessionRequest;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChannelSessionWebMapper {

    @Mapping(target = "channel", source = "dto.channel")
    @Mapping(target = "widgetKey", source = "dto.widgetKey")
    @Mapping(target = "origin", source = "dto.origin")
    @Mapping(target = "clientIp", expression = "java(extractClientIp(forwardedFor))")
    @Mapping(target = "userAgent", source = "userAgent")
    CreateChannelSessionRequest toDomain(
            CreateChannelSessionRequestDto dto,
            String forwardedFor,
            String userAgent
    );

    @Mapping(target = "sessionToken", source = "sessionToken")
    @Mapping(target = "tokenType", source = "tokenType")
    @Mapping(target = "expiresIn", source = "expiresIn")
    @Mapping(target = "tenantId", source = "tenantId")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "channel", source = "channel")
    @Mapping(target = "authType", source = "authType")
    CreateChannelSessionResponseDto toResponse(ChannelSessionResult result);

    default String extractClientIp(String forwardedFor) {
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return null;
        }

        String[] values = forwardedFor.split(",");
        return values.length > 0 ? values[0].trim() : null;
    }
}