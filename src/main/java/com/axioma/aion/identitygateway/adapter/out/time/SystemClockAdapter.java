package com.axioma.aion.identitygateway.adapter.out.time;

import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class SystemClockAdapter implements ClockPort {

    @Override
    public OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}