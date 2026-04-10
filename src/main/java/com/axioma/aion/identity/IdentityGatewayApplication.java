package com.axioma.aion.identity;

import com.axioma.aion.identity.config.IdentityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IdentityProperties.class)
public class IdentityGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityGatewayApplication.class, args);
    }

}
