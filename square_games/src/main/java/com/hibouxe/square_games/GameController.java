package com.hibouxe.square_games;

import com.hibouxe.square_games.client.UserValidationClient;
import com.hibouxe.square_games.exception.NotPlayerTurnException;
import com.hibouxe.square_games.service.GameService;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
public class GameController {

    private final GameService gameService;
    private final UserValidationClient userValidationClient;

    public GameController(GameService gameService, UserValidationClient userValidationClient) {
        this.gameService = gameService;
        this.userValidationClient = userValidationClient;
    }

    @PostMapping("/games")
    public ResponseEntity<?> createGame(
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
    public ResponseEntity<?> getUserGames(@RequestHeader("X-UserId") UUID userId) {
        if (!userValidationClient.isUserValid(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non authentifié ou inconnu dans le service utilisateur."));
        }

        List<Game> games = gameService.getGamesForUser(userId);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<?> getGameState(@PathVariable String gameId) {
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
    public ResponseEntity<?> playMove(
            @RequestHeader("X-UserId") UUID userId,
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