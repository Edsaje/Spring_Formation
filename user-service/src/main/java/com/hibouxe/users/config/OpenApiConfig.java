package com.hibouxe.users.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User Service API",
                version = "1.0",
                description = "Microservice de gestion des utilisateurs (CRUD et vérification d'existence inter-service pour Square Games).",
                contact = @Contact(name = "User Service Team")
        )
)
public class OpenApiConfig {
}
