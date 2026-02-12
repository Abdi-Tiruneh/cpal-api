package com.commercepal.apiservice.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI commercePalOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("CommercePal API Service")
            .description(
                "API documentation for the CommercePal Backend Service. Provides secure endpoints for managing commerce operations, transactions, and business services.")
            .version("v1.0.0")
            .contact(new Contact()
                .name("CommercePal Technical Support")
                .email("support@commercepal.com")
            )
            .license(new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
            )
        )
        .addSecurityItem(new SecurityRequirement().addList("bearer-jwt", List.of("read", "write")))
        .components(new Components()
            .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
            )
        )
        .servers(List.of(
            new Server()
                .url("http://localhost:2060")
                .description("Development Server"),
            new Server()
                .url("http://196.188.172.179:2060")
                .description("Production Server")
        ));
  }
}
