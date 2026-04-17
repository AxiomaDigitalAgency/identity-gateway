package com.axioma.aion.identitygateway.domain.port.out;

import com.axioma.aion.identitygateway.domain.model.WidgetIdentityValidationResult;
import reactor.core.publisher.Mono;

public interface WidgetIdentityValidationPort {

    Mono<WidgetIdentityValidationResult> validate(String widgetKey, String origin);
}