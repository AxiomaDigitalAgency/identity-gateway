package com.axioma.aion.identity.adapter.out.session;

import com.axioma.aion.identity.domain.model.ChannelSession;
import com.axioma.aion.identity.domain.model.ChannelSessionToken;
import com.axioma.aion.identity.domain.port.out.ChannelSessionTokenPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtChannelSessionTokenAdapterIT {

    @Autowired
    private ChannelSessionTokenPort channelSessionTokenPort;

    @Test
    void should_generate_real_jwt_token() {
        ChannelSession session = ChannelSession.builder()
                .sessionId("session-test-001")
                .tenantId("novasmile-dental")
                .channel("web")
                .widgetKey("wk_novasmile_123")
                .origin("http://localhost:5173")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        ChannelSessionToken token = channelSessionTokenPort.generate(session);

        assertNotNull(token);
        assertNotNull(token.token());
        assertEquals("Bearer", token.tokenType());
        assertTrue(token.expiresIn() > 0);

        System.out.println("JWT => " + token.token());
    }
}