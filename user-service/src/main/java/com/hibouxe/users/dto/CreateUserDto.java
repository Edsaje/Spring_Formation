package com.hibouxe.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Données nécessaires pour la création d'un utilisateur")
public record CreateUserDto(
        @Schema(description = "Nom d'utilisateur", example = "Alice", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "Adresse email unique", example = "alice@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email
) {
}
