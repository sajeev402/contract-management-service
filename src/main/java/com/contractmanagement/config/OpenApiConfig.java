package com.contractmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI contractManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Contract Management Service API")
                        .description("API documentation for the Contract Management Service, handling lifecycle and pricing governance.")
                        .version("v0.0.1"));
    }
}
