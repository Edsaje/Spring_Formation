package com.hibouxe.square_games;

import com.hibouxe.square_games.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class GameController {

    // Déclaration de la dépendance vers l'interface
    private final GameService gameService;

    // INJECTION PAR CONSTRUCTEUR (Recommandé par Spring)
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/games")
    public ResponseEntity<?> createGame(@RequestBody GameCreationParams params) {
        try {
            // Demande au service de créer la partie
            String gameId = gameService.createNewGame(params);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "gameId", gameId,
                    "message", "Partie de " + params.gameType + " créée avec succès !"
            ));
        } catch (IllegalArgumentException e) {
            // Si le type de jeu n'est ni 'tictactoe' ni 'chess', on intercepte l'erreur du service
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<Map<String, Object>> getGameState(@PathVariable String gameId) {
        Map<String, Object> state = gameService.getGame(gameId);

        // Si la map contient une clé 'error', on renvoie un statut 404
        if (state.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(state);
        }

        return ResponseEntity.ok(state);
    }
}
