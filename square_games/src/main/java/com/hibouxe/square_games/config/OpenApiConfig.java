package com.hibouxe.square_games.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Square Games API",
                version = "1.0",
                description = "API REST de gestion des parties de jeux de plateau (TicTacToe, Taquin, ConnectFour) avec contrôle d'accès X-UserId et gestion des tours.",
                contact = @Contact(name = "Square Games Team")
        )
)
public class OpenApiConfig {
}
