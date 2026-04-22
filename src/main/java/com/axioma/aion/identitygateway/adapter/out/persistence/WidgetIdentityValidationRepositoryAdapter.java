package com.axioma.aion.identitygateway.adapter.out.persistence;

import com.axioma.aion.identitygateway.adapter.out.persistence.readmodel.WidgetIdentityJoinRow;
import com.axioma.aion.identitygateway.domain.model.WidgetIdentityValidationResult;
import com.axioma.aion.identitygateway.domain.port.out.CredentialHashVerifierPort;
import com.axioma.aion.identitygateway.domain.port.out.WidgetIdentityValidationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class WidgetIdentityValidationRepositoryAdapter implements WidgetIdentityValidationPort {

    private final DatabaseClient databaseClient;
    private final CredentialHashVerifierPort credentialHashVerifierPort;

    private static final String SQL = """
            SELECT
                ic.id               AS identity_context_id,
                ic.tenant_id        AS tenant_id,
                ic.channel          AS channel,
                ic.subject_type     AS subject_type,
                ic.subject_value    AS subject_value,
                cred.credential_value_hash AS credential_value_hash,
                ior.origin_type     AS origin_type,
                ior.origin_value    AS origin_value,
                ior.priority        AS origin_priority
            FROM identity_context ic
            JOIN identity_credential cred
              ON cred.identity_context_id = ic.id
            JOIN identity_origin_rule ior
              ON ior.identity_context_id = ic.id
            WHERE ic.channel = 'WEB'
              AND ic.subject_type = 'WIDGET'
              AND ic.status = 'ACTIVE'
              AND ic.active_reg_ind = TRUE
              AND ic.tenant_id = :tenantId
              AND cred.credential_type = 'WIDGET_KEY_HASH'
              AND cred.status = 'ACTIVE'
              AND cred.active_reg_ind = TRUE
              AND (cred.expires_at IS NULL OR cred.expires_at > CURRENT_TIMESTAMP)
              AND ior.status = 'ACTIVE'
              AND ior.active_reg_ind = TRUE
            """;

    @Override
    public Mono<WidgetIdentityValidationResult> validate(String widgetKey, String origin) {
        return databaseClient.sql(SQL)
                .bind("tenantId", widgetKey)
                .map((row, metadata) -> WidgetIdentityJoinRow.builder()
                        .identityContextId(row.get("identity_context_id", String.class))
                        .tenantId(row.get("tenant_id", String.class))
                        .channel(row.get("channel", String.class))
                        .subjectType(row.get("subject_type", String.class))
                        .subjectValue(row.get("subject_value", String.class))
                        .credentialValueHash(row.get("credential_value_hash", String.class))
                        .originType(row.get("origin_type", String.class))
                        .originValue(row.get("origin_value", String.class))
                        .originPriority(row.get("origin_priority", Integer.class))
                        .build())
                .all()
                .filter(row -> credentialHashVerifierPort.matches(widgetKey, row.credentialValueHash()))
                .filter(row -> matchesOrigin(origin, row.originType(), row.originValue()))
                .sort(Comparator.comparing(
                        row -> row.originPriority() == null ? 0 : row.originPriority(),
                        Comparator.reverseOrder()
                ))
                .next()
                .map(row -> WidgetIdentityValidationResult.builder()
                        .identityContextId(row.identityContextId())
                        .tenantId(row.tenantId())
                        .channel(row.channel())
                        .subjectType(row.subjectType())
                        .subjectValue(row.subjectValue())
                        .allowed(true)
                        .origin(origin)
                        .build())
                .switchIfEmpty(Mono.just(
                        WidgetIdentityValidationResult.builder()
                                .allowed(false)
                                .origin(origin)
                                .build()
                ));
    }

    private boolean matchesOrigin(String requestOrigin, String originType, String originValue) {
        if (requestOrigin == null || requestOrigin.isBlank() || originType == null || originValue == null) {
            return false;
        }

        return switch (originType.toUpperCase()) {
            case "EXACT" -> requestOrigin.equalsIgnoreCase(originValue);
            case "HOST" -> matchesHost(requestOrigin, originValue);
            case "PATTERN" -> matchesPattern(requestOrigin, originValue);
            default -> false;
        };
    }

    private boolean matchesHost(String requestOrigin, String expectedHost) {
        try {
            URI uri = URI.create(requestOrigin);
            String host = uri.getHost();
            return host != null && host.equalsIgnoreCase(expectedHost);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean matchesPattern(String requestOrigin, String pattern) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*");
        return requestOrigin.matches(regex);
    }
}
