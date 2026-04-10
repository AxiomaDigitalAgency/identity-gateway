package com.axioma.aion.identity.adapter.in.web.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ErrorResponseDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationErrorDto> fieldErrors
) {
}