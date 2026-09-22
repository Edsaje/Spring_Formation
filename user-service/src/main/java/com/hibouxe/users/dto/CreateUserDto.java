package com.hibouxe.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Données nécessaires pour la création d'un utilisateur")
public record CreateUserDto(
        @Schema(description = "Nom d'utilisateur unique", example = "Alice", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "Adresse email unique", example = "alice@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @Schema(description = "Mot de passe en clair (sera haché avec BCrypt)", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,

        @Schema(description = "Rôle de l'utilisateur (ROLE_USER ou ROLE_ADMIN)", example = "ROLE_USER")
        String role
) {
    public CreateUserDto(String username, String email) {
        this(username, email, "Password123!", "ROLE_USER");
    }

    public CreateUserDto(String username, String email, String password) {
        this(username, email, password, "ROLE_USER");
    }
}
