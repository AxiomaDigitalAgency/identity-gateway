package com.axioma.aion.identitygateway.domain.port.out;

import java.time.OffsetDateTime;

public interface ClockPort {

    OffsetDateTime now();
}