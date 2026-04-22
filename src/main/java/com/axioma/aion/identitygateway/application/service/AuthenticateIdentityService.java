package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.adapter.in.web.dto.AuthenticateResponse;
import com.axioma.aion.identitygateway.application.command.AuthenticateCommand;
import com.axioma.aion.identitygateway.config.SessionProperties;
import com.axioma.aion.identitygateway.domain.model.AuthenticatedPrincipal;
import com.axioma.aion.identitygateway.domain.model.IdentityCredential;
import com.axioma.aion.identitygateway.domain.port.in.AuthenticateIdentityUseCase;
import com.axioma.aion.identitygateway.domain.port.out.AuthenticationStatePort;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.CredentialPort;
import com.axioma.aion.identitygateway.domain.port.out.IdGeneratorPort;
import com.axioma.aion.identitygateway.domain.port.out.OriginRulePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticateIdentityService implements AuthenticateIdentityUseCase {

    private final CredentialPort credentialPort;
    private final OriginRulePort originRulePort;
    private final AuthenticationStatePort authenticationStatePort;
    private final ClockPort clockPort;
    private final IdGeneratorPort idGeneratorPort;
    private final SessionProperties sessionProperties;



    @Override
    public Mono<AuthenticateResponse> authenticate(AuthenticateCommand command) {
        log.info("authenticate_identity_start requestId={} authenticationType={} channel={} credentialId={} credentialKeyPresent={} clientIdPresent={}",
                command.requestId(),
                command.authenticationType(),
                command.channel(),
                command.credentialId(),
                command.credentialKey() != null && !command.credentialKey().isBlank(),
                command.clientId() != null && !command.clientId().isBlank());

        return resolveCredential(command)
                .doOnNext(credential -> log.info(
                        "authenticate_identity_credential_resolved requestId={} credentialId={} tenantId={} credentialType={}",
                        command.requestId(),
                        credential.id(),
                        credential.tenantId(),
                        credential.credentialType()))
                .flatMap(credential -> validateCredential(command, credential)
                        .then(buildAuthenticatedPrincipal(command, credential))
                )
                .doOnNext(principal -> log.info(
                        "authenticate_identity_principal_built requestId={} authenticationId={} expiresAt={}",
                        command.requestId(),
                        principal.authenticationId(),
                        principal.expiresAt()))
                .flatMap(principal -> authenticationStatePort.save(principal)
                        .thenReturn(toResponse(principal, command.requestId()))
                );
    }

    private Mono<IdentityCredential> resolveCredential(AuthenticateCommand command) {
        if (command.credentialId() != null) {
            return credentialPort.findById(command.credentialId());
        }

        if (command.credentialKey() != null && !command.credentialKey().isBlank()) {
            return credentialPort.findByCredentialKey(command.credentialKey());
        }

        if (command.clientId() != null && !command.clientId().isBlank()) {
            return credentialPort.findByClientId(command.clientId());
        }

        return Mono.error(new IllegalArgumentException("No credential identifier provided"));
    }

    private Mono<Void> validateCredential(AuthenticateCommand command, IdentityCredential credential) {
        if (!credential.enabled()) {
            return Mono.error(new IllegalStateException("Credential is disabled"));
        }

        if (!"ACTIVE".equalsIgnoreCase(credential.status())) {
            return Mono.error(new IllegalStateException("Credential is not active"));
        }

        if (command.authenticationType() == null) {
            return Mono.error(new IllegalArgumentException("Authentication type is required"));
        }

        if ("WIDGET".equalsIgnoreCase(command.authenticationType().name())) {
            if (command.origin() == null || command.origin().isBlank()) {
                return Mono.error(new IllegalArgumentException("Origin is required for widget authentication"));
            }

            return originRulePort.isAllowed(credential.id(), command.origin())
                    .flatMap(allowed -> allowed
                            ? Mono.empty()
                            : Mono.error(new IllegalArgumentException("Origin is not allowed")));
        }

        return Mono.empty();
    }

    private Mono<AuthenticatedPrincipal> buildAuthenticatedPrincipal(
            AuthenticateCommand command,
            IdentityCredential credential
    ) {
        OffsetDateTime authenticatedAt = clockPort.now();
        OffsetDateTime expiresAt = authenticatedAt.plusSeconds(sessionProperties.getTtlSeconds());
        UUID authenticationId = idGeneratorPort.generate();
        if (authenticationId == null) {
            return Mono.error(new IllegalStateException("Generated authenticationId is null"));
        }

        Map<String, Object> attributes = new HashMap<>();
        if (command.origin() != null && !command.origin().isBlank()) {
            attributes.put("origin", command.origin());
        }
        if (command.provider() != null && !command.provider().isBlank()) {
            attributes.put("provider", command.provider());
        }
        if (command.ipAddress() != null && !command.ipAddress().isBlank()) {
            attributes.put("ipAddress", command.ipAddress());
        }
        if (command.userAgent() != null && !command.userAgent().isBlank()) {
            attributes.put("userAgent", command.userAgent());
        }
        if (command.requestId() != null && !command.requestId().isBlank()) {
            attributes.put("requestId", command.requestId());
        }
        if (command.providerMessageId() != null && !command.providerMessageId().isBlank()) {
            attributes.put("providerMessageId", command.providerMessageId());
        }

        String subject = (command.subject() == null || command.subject().isBlank())
                ? "anonymous"
                : command.subject();

        return Mono.just(new AuthenticatedPrincipal(
                authenticationId,
                credential.tenantId(),
                credential.id(),
                command.channel(),
                subject,
                command.authenticationType(),
                authenticatedAt,
                expiresAt,
                attributes
        ));
    }

    private AuthenticateResponse toResponse(AuthenticatedPrincipal principal, String requestId) {
        if (principal.authenticationId() == null) {
            log.error("authenticate_identity_response_with_null_authentication_id requestId={} tenantId={} credentialId={}",
                    requestId, principal.tenantId(), principal.credentialId());
        } else {
            log.info("authenticate_identity_success requestId={} authenticationId={} tenantId={} credentialId={}",
                    requestId, principal.authenticationId(), principal.tenantId(), principal.credentialId());
        }

        return new AuthenticateResponse(
                principal.authenticationId(),
                true,
                principal.authenticationType(),
                principal.tenantId(),
                principal.credentialId(),
                principal.channel(),
                principal.subject(),
                principal.authenticatedAt(),
                principal.expiresAt(),
                principal.attributes()
        );
    }
}
