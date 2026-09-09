package com.rupeek.maidbooking.shared.api;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI maidBookingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maid Booking API")
                        .version("v1")
                        .description("Backend API for maid discovery, booking, payment, and cancellation."))
                .externalDocs(new ExternalDocumentation()
                        .description("Assignment requirements")
                        .url("/requirements.md"));
    }
}
