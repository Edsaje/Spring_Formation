package com.hibouxe.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Représentation d'un utilisateur enregistré")
public record UserResponseDto(
        @Schema(description = "Identifiant UUID unique de l'utilisateur", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Nom d'utilisateur", example = "Alice")
        String username,

        @Schema(description = "Adresse email de l'utilisateur", example = "alice@example.com")
        String email
) {
}
