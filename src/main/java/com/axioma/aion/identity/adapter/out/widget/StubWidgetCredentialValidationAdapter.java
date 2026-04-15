package com.axioma.aion.identity.adapter.out.widget;

import com.axioma.aion.identity.domain.model.WidgetValidationResult;
import com.axioma.aion.identity.domain.port.out.WidgetCredentialValidationPort;


public class StubWidgetCredentialValidationAdapter implements WidgetCredentialValidationPort {

    @Override
    public WidgetValidationResult validate(String widgetKey, String origin, String channel) {
        if ("test-widget-key".equals(widgetKey) && "http://localhost:3000".equals(origin)) {
            return WidgetValidationResult.builder()
                    .valid(true)
                    .tenantId("axioma-agency")
                    .widgetKey(widgetKey)
                    .channel(channel)
                    .build();
        }

        return WidgetValidationResult.builder()
                .valid(false)
                .errorCode("INVALID_WIDGET_CREDENTIALS")
                .errorMessage("Widget key or origin is invalid")
                .build();
    }
}