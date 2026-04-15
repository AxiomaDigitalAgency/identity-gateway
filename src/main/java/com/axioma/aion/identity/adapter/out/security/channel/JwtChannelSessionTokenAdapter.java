package com.axioma.aion.identity.adapter.out.security.channel;

import com.axioma.aion.identity.config.JwtSessionProperties;
import com.axioma.aion.identity.domain.model.ChannelSession;
import com.axioma.aion.identity.domain.model.ChannelSessionToken;
import com.axioma.aion.identity.domain.port.out.ChannelSessionTokenPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelSessionTokenAdapter implements ChannelSessionTokenPort {

    private static final String SESSION_TYPE = "CHANNEL_SESSION";

    private final JwtEncoder sessionJwtEncoder;
    private final JwtDecoder sessionJwtDecoder;
    private final JwtSessionProperties properties;

    @Override
    public ChannelSessionToken generate(ChannelSession session) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getTtlSeconds());

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .subject(session.sessionId())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("tenant_id", session.tenantId())
                .claim("channel", session.channel())
                .claim("widget_key", session.widgetKey())
                .claim("origin", session.origin())
                .claim("session_type", SESSION_TYPE)
                .claim("version", 1)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = sessionJwtEncoder.encode(JwtEncoderParameters.from(header, claimsSet))
                .getTokenValue();

        return ChannelSessionToken.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(properties.getTtlSeconds())
                .build();
    }

    @Override
    public ChannelSession validateAndParse(String token) {
        try {
            Jwt jwt = sessionJwtDecoder.decode(token);

            String sessionType = jwt.getClaimAsString("session_type");
            if (!SESSION_TYPE.equals(sessionType)) {
                throw new JwtValidationException(
                        "Invalid session token type",
                        java.util.List.of(
                                new OAuth2Error("invalid_token", "Token is not a channel session token", null)
                        )
                );
            }

            return ChannelSession.builder()
                    .sessionId(jwt.getSubject())
                    .tenantId(jwt.getClaimAsString("tenant_id"))
                    .channel(jwt.getClaimAsString("channel"))
                    .widgetKey(jwt.getClaimAsString("widget_key"))
                    .origin(jwt.getClaimAsString("origin"))
                    .issuedAt(jwt.getIssuedAt())
                    .expiresAt(jwt.getExpiresAt())
                    .build();

        } catch (JwtException ex) {
            log.error("Invalid channel session token", ex);
            throw ex;
        }
    }
}