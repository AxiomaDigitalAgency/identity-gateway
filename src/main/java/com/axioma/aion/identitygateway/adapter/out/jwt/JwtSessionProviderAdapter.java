package com.axioma.aion.identitygateway.adapter.out.jwt;

import com.axioma.aion.identitygateway.config.JwtProperties;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.JwtSessionClaims;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionProviderPort;
import com.axioma.aion.securitycore.exception.AuthenticationFailedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtSessionProviderAdapter implements JwtSessionProviderPort {

    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> generate(IdentitySession identitySession) {
        return Mono.fromSupplier(() -> {
            SecretKey key = signingKey();
            SessionMetadata metadata = readMetadata(identitySession.metadataJson());

            return Jwts.builder()
                    .issuer(jwtProperties.getIssuer())
                    .subject(metadata.subject())
                    .id(identitySession.tokenId().value())
                    .claim("sessionId", identitySession.id())
                    .claim("tenantId", identitySession.tenantId())
                    .claim("channel", identitySession.channel())
                    .claim("provider", metadata.provider())
                    .issuedAt(Date.from(identitySession.issuedAt().toInstant()))
                    .expiration(Date.from(identitySession.expiresAt().toInstant()))
                    .signWith(key)
                    .compact();
        });
    }

    @Override
    public Mono<JwtSessionClaims> parse(String sessionToken) {
        return Mono.fromSupplier(() -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey())
                        .requireIssuer(jwtProperties.getIssuer())
                        .build()
                        .parseSignedClaims(sessionToken)
                        .getPayload();

                return JwtSessionClaims.builder()
                        .sessionId(claims.get("sessionId", String.class))
                        .tenantId(claims.get("tenantId", String.class))
                        .subject(claims.getSubject())
                        .channel(claims.get("channel", String.class))
                        .provider(claims.get("provider", String.class))
                        .tokenId(new TokenId(claims.getId()))
                        .issuedAt(toOffsetDateTime(claims.getIssuedAt()))
                        .expiresAt(toOffsetDateTime(claims.getExpiration()))
                        .build();
            } catch (Exception ex) {
                throw new AuthenticationFailedException("Invalid session token", ex);
            }
        });
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    private OffsetDateTime toOffsetDateTime(Date date) {
        return date == null ? null : date.toInstant().atOffset(ZoneOffset.UTC);
    }

    private SessionMetadata readMetadata(String metadataJson) {
        try {
            JsonNode root = objectMapper.readTree(metadataJson);
            String subject = root.path("subject").asText(null);
            String provider = root.path("provider").asText(null);
            return new SessionMetadata(subject, provider);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read session metadata", ex);
        }
    }

    private record SessionMetadata(String subject, String provider) {
    }
}