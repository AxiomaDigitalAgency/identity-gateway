package com.axioma.aion.identitygateway.domain.port.out;

import com.axioma.aion.identitygateway.domain.model.WidgetValidationResult;
import reactor.core.publisher.Mono;

public interface WidgetValidationPort {

    Mono<WidgetValidationResult> validate(String widgetKey, String origin);
}