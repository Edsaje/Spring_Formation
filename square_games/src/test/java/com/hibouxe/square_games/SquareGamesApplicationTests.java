package com.hibouxe.square_games;

import com.hibouxe.square_games.client.UserValidationClient;
import com.hibouxe.square_games.exception.NotPlayerTurnException;
import com.hibouxe.square_games.service.GameService;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SquareGamesApplicationTests {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameController gameController;

    @MockitoBean
    private UserValidationClient userValidationClient;

    @Test
    void testGameServiceRules() {
        UUID creatorId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();

        // 1. Création de partie avec joueur créateur et adversaire
        GameCreationParams params = new GameCreationParams("tictactoe", 2, 3, List.of(opponentId));
        Game game = gameService.createNewGame(creatorId, params);

        assertNotNull(game);
        assertTrue(game.getPlayerIds().contains(creatorId));
        assertTrue(game.getPlayerIds().contains(opponentId));

        // 2. Filtrage des parties par joueur
        List<Game> creatorGames = gameService.getGamesForUser(creatorId);
        assertFalse(creatorGames.isEmpty());
        assertTrue(creatorGames.stream().anyMatch(g -> g.getId().equals(game.getId())));

        UUID strangerId = UUID.randomUUID();
        List<Game> strangerGames = gameService.getGamesForUser(strangerId);
        assertTrue(strangerGames.stream().noneMatch(g -> g.getId().equals(game.getId())));

        // 3. Contrôle du tour de jeu
        UUID currentPlayer = game.getCurrentPlayerId();
        UUID otherPlayer = currentPlayer.equals(creatorId) ? opponentId : creatorId;

        // Le joueur qui n'a pas la main doit lever une NotPlayerTurnException (HTTP 403)
        assertThrows(NotPlayerTurnException.class, () -> {
            gameService.playMove(game.getId(), otherPlayer, new CellPosition(0, 0));
        });

        // Le joueur dont c'est le tour joue avec succès
        Game updated = gameService.playMove(game.getId(), currentPlayer, new CellPosition(0, 0));
        assertNotNull(updated);
        assertNotEquals(currentPlayer, updated.getCurrentPlayerId()); // Le tour a changé
    }

    @Test
    void testGameControllerAuthenticationAndTurnEnforcement() {
        UUID validUserId = UUID.randomUUID();
        UUID unknownUserId = UUID.randomUUID();

        // Simuler la validation RestClient
        Mockito.when(userValidationClient.isUserValid(validUserId)).thenReturn(true);
        Mockito.when(userValidationClient.isUserValid(unknownUserId)).thenReturn(false);

        GameCreationParams params = new GameCreationParams("tictactoe", 2, 3);

        // 1. Utilisateur inconnu -> 401 Unauthorized
        ResponseEntity<?> unauthorizedResponse = gameController.createGame(unknownUserId, params);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResponse.getStatusCode());

        // 2. Utilisateur valide -> 201 Created
        ResponseEntity<?> createdResponse = gameController.createGame(validUserId, params);
        assertEquals(HttpStatus.CREATED, createdResponse.getStatusCode());

        // Récupérer la partie
        List<Game> games = gameService.getGamesForUser(validUserId);
        assertFalse(games.isEmpty());
        Game game = games.getFirst();

        UUID currentPlayer = game.getCurrentPlayerId();
        UUID wrongPlayer = UUID.randomUUID();
        Mockito.when(userValidationClient.isUserValid(wrongPlayer)).thenReturn(true);

        // 3. Coup joué par le mauvais joueur -> 403 Forbidden
        ResponseEntity<?> forbiddenResponse = gameController.playMove(wrongPlayer, game.getId().toString(), new CellPosition(1, 1));
        assertEquals(HttpStatus.FORBIDDEN, forbiddenResponse.getStatusCode());

        // 4. Coup joué par le joueur courant -> 200 OK
        Mockito.when(userValidationClient.isUserValid(currentPlayer)).thenReturn(true);
        ResponseEntity<?> successMove = gameController.playMove(currentPlayer, game.getId().toString(), new CellPosition(1, 1));
        assertEquals(HttpStatus.OK, successMove.getStatusCode());
    }
}
