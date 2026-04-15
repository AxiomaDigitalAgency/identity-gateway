package com.axioma.aion.identity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "clients.tenant-service")
public class TenantServiceProperties {

    private String baseUrl;
    private String validateWidgetPath = "/tenants/validate-widget-key";
}