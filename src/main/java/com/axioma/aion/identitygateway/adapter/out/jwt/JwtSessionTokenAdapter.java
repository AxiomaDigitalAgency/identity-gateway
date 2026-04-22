package com.axioma.aion.identitygateway.adapter.out.jwt;

import com.axioma.aion.identitygateway.config.JwtProperties;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionTokenPort;
import com.axioma.aion.securitycore.model.AuthContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtSessionTokenAdapter implements JwtSessionTokenPort {

    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> generate(AuthContext authContext) {
        return Mono.fromSupplier(() -> {
            Map<String, Object> source = objectMapper.convertValue(
                    authContext,
                    new TypeReference<Map<String, Object>>() {}
            );

            String sessionId = readString(source, "sessionId", "session_id");
            String tenantId = readString(source, "tenantId", "tenant_id");
            String credentialId = readString(source, "credentialId", "credential_id");
            String subject = readString(source, "subject");
            String channel = readString(source, "channel");
            String authenticationType = readString(
                    source,
                    "authenticationType",
                    "authentication_type"
            );

            OffsetDateTime issuedAt = readOffsetDateTime(
                    source,
                    "authenticatedAt",
                    "authenticated_at",
                    OffsetDateTime.now(ZoneOffset.UTC)
            );
            OffsetDateTime expiresAt = readOffsetDateTime(
                    source,
                    "expiresAt",
                    "expires_at",
                    issuedAt.plusSeconds(jwtProperties.getTtlSeconds())
            );

            String tokenId = readString(source, "tokenId", "token_id");
            if (tokenId == null || tokenId.isBlank()) {
                tokenId = UUID.randomUUID().toString();
            }

            log.info("jwt_session_token_generate_start sessionId={} tenantId={} subject={} channel={} tokenId={} expiresAt={}",
                    sessionId, tenantId, subject, channel, tokenId, expiresAt);

            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));

            String token = Jwts.builder()
                    .issuer(jwtProperties.getIssuer())
                    .subject(subject)
                    .id(tokenId)
                    .issuedAt(Date.from(issuedAt.toInstant()))
                    .expiration(Date.from(expiresAt.toInstant()))
                    .claim("sessionId", sessionId)
                    .claim("tenantId", tenantId)
                    .claim("credentialId", credentialId)
                    .claim("channel", channel)
                    .claim("authenticationType", authenticationType)
                    .signWith(key)
                    .compact();

            log.info("jwt_session_token_generate_complete sessionId={} tokenId={} tokenLength={}",
                    sessionId, tokenId, token.length());

            return token;
        });
    }

    private String readString(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private OffsetDateTime readOffsetDateTime(
            Map<String, Object> source,
            String key1,
            String key2,
            OffsetDateTime fallback
    ) {
        Object value = source.getOrDefault(key1, source.get(key2));
        if (value == null) {
            return fallback;
        }

        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime;
        }

        if (value instanceof Date date) {
            return date.toInstant().atOffset(ZoneOffset.UTC);
        }

        try {
            return OffsetDateTime.parse(String.valueOf(value));
        } catch (DateTimeParseException ex) {
            return fallback;
        }
    }
}
