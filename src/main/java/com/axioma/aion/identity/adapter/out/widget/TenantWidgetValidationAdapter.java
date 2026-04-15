package com.axioma.aion.identity.adapter.out.widget;


import com.axioma.aion.identity.adapter.in.web.dto.ErrorResponseDto;
import com.axioma.aion.identity.adapter.out.widget.dto.TenantWidgetValidationRequestDto;
import com.axioma.aion.identity.adapter.out.widget.dto.TenantWidgetValidationResponseDto;
import com.axioma.aion.identity.config.TenantServiceProperties;
import com.axioma.aion.identity.domain.model.WidgetValidationResult;
import com.axioma.aion.identity.domain.port.out.WidgetCredentialValidationPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class TenantWidgetValidationAdapter implements WidgetCredentialValidationPort {

    private final WebClient webClient;
    private final TenantServiceProperties tenantServiceProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public WidgetValidationResult validate(String widgetKey, String origin, String channel) {
        TenantWidgetValidationRequestDto request = TenantWidgetValidationRequestDto.builder()
                .widgetKey(widgetKey)
                .origin(origin)
                .build();

        String uri = buildUri();

        try {
            log.info("Sending widget validation request to tenant-service. uri={}, channel={}, widgetKey={}, origin={}",
                    uri, channel, widgetKey, origin);

            TenantWidgetValidationResponseDto response = webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.warn("Tenant-service returned 4xx for widget validation. uri={}, status={}, body={}",
                                                uri, clientResponse.statusCode(), body);
                                        return clientResponse.createException();
                                    })
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Tenant-service returned 5xx for widget validation. uri={}, status={}, body={}",
                                                uri, clientResponse.statusCode(), body);
                                        return clientResponse.createException();
                                    })
                    )
                    .bodyToMono(TenantWidgetValidationResponseDto.class)
                    .doOnNext(resp -> log.info(
                            "Received widget validation response from tenant-service. uri={}, valid={}, tenantId={}",
                            uri, resp.valid(), resp.tenantId()
                    ))
                    .block();

            if (response == null) {
                log.warn("Tenant-service returned null widget validation response. uri={}, widgetKey={}, origin={}",
                        uri, widgetKey, origin);
                return invalid("TENANT_SERVICE_EMPTY_RESPONSE", "Tenant-service returned an empty response");
            }

            if (Boolean.TRUE.equals(response.valid()) && hasText(response.tenantId())) {
                return WidgetValidationResult.builder()
                        .valid(true)
                        .tenantId(response.tenantId())
                        .widgetKey(widgetKey)
                        .channel(channel)
                        .build();
            }

            log.warn("Widget validation rejected by tenant-service. uri={}, widgetKey={}, origin={}, valid={}, tenantId={}",
                    uri, widgetKey, origin, response.valid(), response.tenantId());

            return invalid("INVALID_WIDGET_CREDENTIALS", "Widget key or origin is invalid");
        } catch (WebClientResponseException ex) {
            ErrorResponseDto errorResponse = parseErrorResponse(ex);

            log.error(
                    "Tenant-service HTTP error during widget validation. uri={}, status={}, error={}, message={}, path={}",
                    uri,
                    ex.getStatusCode().value(),
                    errorResponse != null ? errorResponse.error() : null,
                    errorResponse != null ? errorResponse.message() : ex.getMessage(),
                    errorResponse != null ? errorResponse.path() : null,
                    ex
            );

            return mapHttpError(ex.getStatusCode(), errorResponse);
        } catch (Exception ex) {
            log.error("Unexpected error validating widget credentials against tenant-service. uri={}", uri, ex);
            return invalid(
                    "TENANT_SERVICE_VALIDATION_ERROR",
                    "Unable to validate widget credentials at this time"
            );
        }
    }

    private String buildUri() {
        String uri = tenantServiceProperties.getBaseUrl() + tenantServiceProperties.getValidateWidgetPath();
        log.debug("Tenant-service validation URI: {}", uri);
        return uri;
    }

    private WidgetValidationResult mapHttpError(HttpStatusCode statusCode, ErrorResponseDto errorResponse) {
        int status = statusCode.value();

        if (status >= 400 && status < 500) {
            return invalid(
                    buildErrorCode("TENANT_CLIENT_ERROR", status),
                    buildErrorMessage(errorResponse, "Widget credentials were rejected")
            );
        }

        if (status >= 500 && status < 600) {
            return invalid(
                    buildErrorCode("TENANT_SERVER_ERROR", status),
                    buildErrorMessage(errorResponse, "Tenant-service is unavailable")
            );
        }

        return invalid(
                buildErrorCode("TENANT_UNKNOWN_HTTP_ERROR", status),
                buildErrorMessage(errorResponse, "Unexpected HTTP error")
        );
    }

    private String buildErrorCode(String prefix, int status) {
        return prefix + "_" + status;
    }

    private String buildErrorMessage(ErrorResponseDto errorResponse, String fallback) {
        if (errorResponse == null) {
            return fallback;
        }

        if (hasText(errorResponse.message())) {
            return errorResponse.message();
        }

        if (hasText(errorResponse.error())) {
            return errorResponse.error();
        }

        return fallback;
    }

    private ErrorResponseDto parseErrorResponse(WebClientResponseException ex) {
        try {
            String body = ex.getResponseBodyAsString();
            if (!hasText(body)) {
                return null;
            }
            return objectMapper.readValue(body, ErrorResponseDto.class);
        } catch (Exception parseEx) {
            log.warn("Unable to parse tenant-service error response body", parseEx);
            return null;
        }
    }

    private WidgetValidationResult invalid(String errorCode, String errorMessage) {
        return WidgetValidationResult.builder()
                .valid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}