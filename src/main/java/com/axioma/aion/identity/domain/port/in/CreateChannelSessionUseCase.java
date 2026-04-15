package com.axioma.aion.identity.domain.port.in;

import com.axioma.aion.identity.domain.model.ChannelSessionResult;
import com.axioma.aion.identity.domain.model.CreateChannelSessionRequest;

public interface CreateChannelSessionUseCase {
    ChannelSessionResult create(CreateChannelSessionRequest request);
}