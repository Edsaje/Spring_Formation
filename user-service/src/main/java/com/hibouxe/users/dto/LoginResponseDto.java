package com.hibouxe.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Réponse d'authentification contenant le jeton JWT")
public record LoginResponseDto(
        @Schema(description = "Jeton d'accès JWT signé", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Identifiant UUID de l'utilisateur", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,

        @Schema(description = "Nom d'utilisateur", example = "Alice")
        String username,

        @Schema(description = "Rôle de l'utilisateur", example = "ROLE_USER")
        String role
) {
}
