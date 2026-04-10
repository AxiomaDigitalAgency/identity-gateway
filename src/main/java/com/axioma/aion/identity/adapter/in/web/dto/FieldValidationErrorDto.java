package com.axioma.aion.identity.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record FieldValidationErrorDto(
        String field,
        Object rejectedValue,
        String message
) {
}