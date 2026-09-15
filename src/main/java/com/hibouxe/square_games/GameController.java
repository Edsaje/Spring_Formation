package com.hibouxe.square_games;

import com.hibouxe.square_games.service.GameService;
import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/games")
    public ResponseEntity<?> createGame(@RequestBody GameCreationParams params) {
        try {
            Game game = gameService.createNewGame(params);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "gameId", game.getId().toString(),
                    "message", "Partie de " + params.gameType() + " créée avec succès !"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
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
            // Dans le cas où l'identifiant n'est pas un format UUID valide
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Format d'identifiant UUID invalide : " + gameId));
        }
    }
}