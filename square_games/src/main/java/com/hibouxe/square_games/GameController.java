package com.hibouxe.square_games;

import com.hibouxe.square_games.client.UserValidationClient;
import com.hibouxe.square_games.exception.NotPlayerTurnException;
import com.hibouxe.square_games.service.GameService;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@Tag(name = "Parties de Jeu", description = "Gestion du cycle de vie des parties (création, consultation) et exécution des coups")
public class GameController {

    private final GameService gameService;
    private final UserValidationClient userValidationClient;

    public GameController(GameService gameService, UserValidationClient userValidationClient) {
        this.gameService = gameService;
        this.userValidationClient = userValidationClient;
    }

    @PostMapping("/games")
    @Operation(
            summary = "Créer une nouvelle partie",
            description = "Initialise une nouvelle partie pour l'utilisateur identifié par l'entête X-UserId. L'existence de l'utilisateur est validée auprès du microservice user-service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Partie créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Type de jeu inconnu ou paramètres invalides"),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié ou inconnu dans user-service")
    })
    public ResponseEntity<?> createGame(
            @Parameter(description = "Identifiant UUID de l'utilisateur créateur", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestHeader("X-UserId") UUID userId,
            @RequestBody GameCreationParams params) {

        if (!userValidationClient.isUserValid(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non authentifié ou inconnu dans le service utilisateur."));
        }

        try {
            Game game = gameService.createNewGame(userId, params);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "gameId", game.getId().toString(),
                    "message", "Partie de " + params.gameType() + " créée avec succès !",
                    "playerIds", game.getPlayerIds(),
                    "currentPlayerId", game.getCurrentPlayerId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping(value = "/games", headers = "X-UserId")
    @Operation(
            summary = "Lister les parties de l'utilisateur",
            description = "Récupère la liste de toutes les parties (en cours ou terminées) auxquelles participe l'utilisateur spécifié dans X-UserId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des parties récupérée"),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié ou inconnu dans user-service")
    })
    public ResponseEntity<?> getUserGames(
            @Parameter(description = "Identifiant UUID de l'utilisateur", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestHeader("X-UserId") UUID userId) {

        if (!userValidationClient.isUserValid(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non authentifié ou inconnu dans le service utilisateur."));
        }

        List<Game> games = gameService.getGamesForUser(userId);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/games/{gameId}")
    @Operation(
            summary = "Consulter l'état d'une partie",
            description = "Retourne l'état complet d'une partie (identifiant, état du plateau, joueur courant, jetons restants)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "État de la partie trouvé"),
            @ApiResponse(responseCode = "400", description = "Format de l'identifiant gameId invalide"),
            @ApiResponse(responseCode = "404", description = "Partie introuvable")
    })
    public ResponseEntity<?> getGameState(
            @Parameter(description = "Identifiant UUID de la partie", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String gameId) {
        try {
            UUID uuid = UUID.fromString(gameId);
            Game game = gameService.getGame(uuid);

            if (game == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Aucune partie trouvée avec l'identifiant " + gameId));
            }

            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Format d'identifiant UUID invalide : " + gameId));
        }
    }

    @PostMapping("/games/{gameId}/moves")
    @Operation(
            summary = "Jouer un coup",
            description = "Effectue le déplacement ou le placement d'un jeton à la position spécifiée. Si le joueur spécifié dans l'entête X-UserId n'est pas le joueur dont c'est le tour (currentPlayerId), la requête est rejetée avec un code HTTP 403 Forbidden."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coup joué avec succès, état mis à jour"),
            @ApiResponse(responseCode = "400", description = "Position de coup invalide"),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié ou inconnu dans user-service"),
            @ApiResponse(responseCode = "403", description = "Ce n'est pas le tour de ce joueur"),
            @ApiResponse(responseCode = "404", description = "Partie introuvable")
    })
    public ResponseEntity<?> playMove(
            @Parameter(description = "Identifiant UUID du joueur qui effectue le coup", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestHeader("X-UserId") UUID userId,
            @Parameter(description = "Identifiant UUID de la partie", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String gameId,
            @RequestBody CellPosition move) {

        if (!userValidationClient.isUserValid(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non authentifié ou inconnu dans le service utilisateur."));
        }

        try {
            UUID uuid = UUID.fromString(gameId);
            Game updatedGame = gameService.playMove(uuid, userId, move);
            return ResponseEntity.ok(updatedGame);
        } catch (NotPlayerTurnException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}