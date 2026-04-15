package com.axioma.aion.identity.domain.port.out;


import com.axioma.aion.identity.domain.model.ChannelSession;
import com.axioma.aion.identity.domain.model.ChannelSessionToken;

public interface ChannelSessionTokenPort {

    ChannelSessionToken generate(ChannelSession session);
    ChannelSession validateAndParse(String token);
}