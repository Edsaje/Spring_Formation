package com.hibouxe.users.controller;

import com.hibouxe.users.dto.CreateUserDto;
import com.hibouxe.users.dto.UserResponseDto;
import com.hibouxe.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Utilisateurs", description = "Gestion des comptes utilisateurs et validation inter-service")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(
            summary = "Créer un utilisateur",
            description = "Enregistre un nouvel utilisateur avec nom, email et mot de passe (haché avec BCrypt). Rôle par défaut : ROLE_USER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Format des données invalide ou identifiant déjà existant")
    })
    public ResponseEntity<?> createUser(@RequestBody CreateUserDto dto) {
        try {
            UserResponseDto created = userService.createUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Lister tous les utilisateurs (Admin uniquement)",
            description = "Retourne la liste complète de tous les utilisateurs enregistrés. Nécessite le rôle ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé (ROLE_ADMIN requis)")
    })
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@userSecurity.canAccessUser(authentication, #id)")
    @Operation(
            summary = "Consulter un utilisateur par son identifiant",
            description = "Retourne le profil d'un utilisateur. Accessible uniquement à l'administrateur ou au propriétaire du compte."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé (doit être le propriétaire ou administrateur)"),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur correspondant")
    })
    public ResponseEntity<?> getUser(
            @Parameter(description = "UUID de l'utilisateur", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        return userService.getUser(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utilisateur non trouvé avec l'id : " + id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Supprimer un utilisateur (Admin uniquement)",
            description = "Supprime définitivement un utilisateur. Nécessite le rôle ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé (ROLE_ADMIN requis)"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "UUID de l'utilisateur à supprimer", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/valid")
    @Operation(
            summary = "Vérifier la validité d'un utilisateur",
            description = "Route légère pour vérifier l'existence d'un joueur en base de données."
    )
    @ApiResponse(responseCode = "200", description = "Booléen indiquant la validité de l'identifiant")
    public ResponseEntity<Boolean> isUserValid(
            @Parameter(description = "UUID de l'utilisateur à vérifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        boolean valid = userService.isUserValid(id);
        return ResponseEntity.ok(valid);
    }
}
