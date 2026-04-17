package com.axioma.aion.identitygateway.adapter.out.security.widget;

import com.axioma.aion.identitygateway.domain.model.WidgetValidationResult;
import com.axioma.aion.identitygateway.domain.port.out.WidgetValidationPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryWidgetValidationAdapter implements WidgetValidationPort {

    @Override
    public Mono<WidgetValidationResult> validate(String widgetKey, String origin) {
        if ("widget-demo".equals(widgetKey) && "http://localhost:3000".equals(origin)) {
            return Mono.just(WidgetValidationResult.builder()
                    .tenantId("tenant-demo")
                    .allowed(true)
                    .widgetKey(widgetKey)
                    .origin(origin)
                    .build());
        }

        return Mono.just(WidgetValidationResult.builder()
                .tenantId(null)
                .allowed(false)
                .widgetKey(widgetKey)
                .origin(origin)
                .build());
    }
}