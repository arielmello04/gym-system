// src/main/java/com/gymsystem/config/OpenApiConfig.java
package com.gymsystem.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Basic OpenAPI metadata (Swagger UI at /swagger-ui.html). */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(new Info()
                .title("Gym System API")
                .version("v1")
                .description("SaaS for gym: auth, booking, check-in, documents, payments"));
    }
}
