package com.noshop.product_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("NoShop Product Service API")
                                .description("REST APIs for Product Service")
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("Akshay")
                                                .email("ak@example.com")

                                )
                );
    }
}