package com.hibouxe.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Identifiants de connexion")
public record LoginRequestDto(
        @Schema(description = "Nom d'utilisateur", example = "Alice", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "Mot de passe", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
