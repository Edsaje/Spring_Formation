package com.hibouxe.square_games;

import com.hibouxe.square_games.client.UserValidationClient;
import com.hibouxe.square_games.exception.NotPlayerTurnException;
import com.hibouxe.square_games.service.GameService;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class SquareGamesApplicationTests {

    private MockMvc mockMvc;

    @Autowired
    private GameController gameController;

    @Autowired
    private GameService gameService;

    @MockitoBean
    private UserValidationClient userValidationClient;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(gameController).build();
    }

    @Test
    @DisplayName("Service : Validation de l'isolation des données et des règles de tour")
    void testGameServiceRules() {
        UUID creatorId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();

        // 1. Création de partie avec joueur créateur et adversaire
        GameCreationParams params = new GameCreationParams("tictactoe", 2, 3, List.of(opponentId));
        Game game = gameService.createNewGame(creatorId, params);

        assertNotNull(game);
        assertTrue(game.getPlayerIds().contains(creatorId));
        assertTrue(game.getPlayerIds().contains(opponentId));

        // 2. Isolation des données : Le créateur voit sa partie
        List<Game> creatorGames = gameService.getGamesForUser(creatorId);
        assertFalse(creatorGames.isEmpty());
        assertTrue(creatorGames.stream().anyMatch(g -> g.getId().equals(game.getId())));

        // 3. Protection des données : Un tiers étranger ne voit JAMAIS cette partie
        UUID strangerId = UUID.randomUUID();
        List<Game> strangerGames = gameService.getGamesForUser(strangerId);
        assertTrue(strangerGames.stream().noneMatch(g -> g.getId().equals(game.getId())));

        // 4. Contrôle du tour de jeu : le mauvais joueur lève NotPlayerTurnException
        UUID currentPlayer = game.getCurrentPlayerId();
        UUID otherPlayer = currentPlayer.equals(creatorId) ? opponentId : creatorId;

        assertThrows(NotPlayerTurnException.class, () -> {
            gameService.playMove(game.getId(), otherPlayer, new CellPosition(0, 0));
        });

        // 5. Le joueur actif joue avec succès
        Game updated = gameService.playMove(game.getId(), currentPlayer, new CellPosition(0, 0));
        assertNotNull(updated);
        assertNotEquals(currentPlayer, updated.getCurrentPlayerId());
    }

    @Test
    @DisplayName("Sanction HTTP 401 : Rejet lors de la création si X-UserId inconnu")
    void testHttpSanction401_CreateGame_UnknownUser() throws Exception {
        UUID unknownUserId = UUID.randomUUID();
        Mockito.when(userValidationClient.isUserValid(unknownUserId)).thenReturn(false);

        mockMvc.perform(post("/games")
                        .header("X-UserId", unknownUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gameType": "tictactoe",
                                  "numberOfPlayers": 2,
                                  "boardSize": 3
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Sanction HTTP 201 : Création réussie si X-UserId valide et authentifié")
    void testHttpSanction201_CreateGame_AuthenticatedUser() throws Exception {
        UUID validUserId = UUID.randomUUID();
        Mockito.when(userValidationClient.isUserValid(validUserId)).thenReturn(true);

        mockMvc.perform(post("/games")
                        .header("X-UserId", validUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gameType": "tictactoe",
                                  "numberOfPlayers": 2,
                                  "boardSize": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").isString())
                .andExpect(jsonPath("$.currentPlayerId").isString());
    }

    @Test
    @DisplayName("Protection des données (HTTP 200) : Seules les parties de l'utilisateur X-UserId sont renvoyées")
    void testHttpSanction200_DataProtection_GetUserGames() throws Exception {
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        Mockito.when(userValidationClient.isUserValid(aliceId)).thenReturn(true);
        Mockito.when(userValidationClient.isUserValid(bobId)).thenReturn(true);

        // Alice crée une partie solo contre un joueur virtuel
        GameCreationParams params = new GameCreationParams("tictactoe", 2, 3);
        Game aliceGame = gameService.createNewGame(aliceId, params);

        // Requête d'Alice avec son X-UserId -> 200 OK et sa partie est présente
        mockMvc.perform(get("/games")
                        .header("X-UserId", aliceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + aliceGame.getId() + "')]").exists());

        // Requête de Bob avec son X-UserId -> 200 OK mais la partie d'Alice N'EST PAS présente (protection des données)
        mockMvc.perform(get("/games")
                        .header("X-UserId", bobId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + aliceGame.getId() + "')]").doesNotExist());
    }

    @Test
    @DisplayName("Sanction HTTP 401 : Rejet lors de la consultation des parties si X-UserId inconnu")
    void testHttpSanction401_GetUserGames_UnknownUser() throws Exception {
        UUID unknownUserId = UUID.randomUUID();
        Mockito.when(userValidationClient.isUserValid(unknownUserId)).thenReturn(false);

        mockMvc.perform(get("/games")
                        .header("X-UserId", unknownUserId.toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Sanction HTTP 403 : Rejet lorsque le joueur X-UserId tente de jouer hors de son tour")
    void testHttpSanction403_PlayMove_WrongTurn() throws Exception {
        UUID creatorId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();
        Mockito.when(userValidationClient.isUserValid(creatorId)).thenReturn(true);
        Mockito.when(userValidationClient.isUserValid(opponentId)).thenReturn(true);

        GameCreationParams params = new GameCreationParams("tictactoe", 2, 3, List.of(opponentId));
        Game game = gameService.createNewGame(creatorId, params);

        UUID currentPlayer = game.getCurrentPlayerId();
        // Identifier le joueur qui n'a PAS la main
        UUID waitingPlayer = currentPlayer.equals(creatorId) ? opponentId : creatorId;

        mockMvc.perform(post("/games/" + game.getId() + "/moves")
                        .header("X-UserId", waitingPlayer.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "x": 0,
                                  "y": 0
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Sanction HTTP 200 : Coup validé avec succès lorsque X-UserId est le joueur actif")
    void testHttpSanction200_PlayMove_CurrentPlayerSuccess() throws Exception {
        UUID creatorId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();
        Mockito.when(userValidationClient.isUserValid(creatorId)).thenReturn(true);
        Mockito.when(userValidationClient.isUserValid(opponentId)).thenReturn(true);

        GameCreationParams params = new GameCreationParams("tictactoe", 2, 3, List.of(opponentId));
        Game game = gameService.createNewGame(creatorId, params);

        UUID currentPlayer = game.getCurrentPlayerId();

        mockMvc.perform(post("/games/" + game.getId() + "/moves")
                        .header("X-UserId", currentPlayer.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "x": 1,
                                  "y": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(game.getId().toString()));
    }

    @Test
    @DisplayName("Sanction HTTP 404 : Partie introuvable")
    void testHttpSanction404_GameNotFound() throws Exception {
        UUID randomGameId = UUID.randomUUID();

        mockMvc.perform(get("/games/" + randomGameId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Sanction HTTP 400 : Paramètres invalides ou UUID malformé")
    void testHttpSanction400_BadRequest() throws Exception {
        mockMvc.perform(get("/games/ceci-n-est-pas-un-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
