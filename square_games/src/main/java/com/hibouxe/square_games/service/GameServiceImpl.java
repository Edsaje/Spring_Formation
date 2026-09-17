package com.hibouxe.square_games.service;

import com.hibouxe.square_games.GameCreationParams;
import com.hibouxe.square_games.etienne.GameDao;
import com.hibouxe.square_games.exception.NotPlayerTurnException;
import com.hibouxe.square_games.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    private final GameDao gameDao;
    private final Map<String, GamePlugin> plugins = new HashMap<>();

    public GameServiceImpl(GameDao gameDao, List<GamePlugin> availablePlugins) {
        this.gameDao = gameDao;
        for (GamePlugin plugin : availablePlugins) {
            this.plugins.put(plugin.getId().toLowerCase(), plugin);
        }
    }

    @Override
    public Game createNewGame(UUID creatorId, GameCreationParams params) {
        if (params.gameType() == null || !plugins.containsKey(params.gameType().toLowerCase())) {
            throw new IllegalArgumentException("Type de jeu inconnu : " + params.gameType());
        }

        GamePlugin plugin = plugins.get(params.gameType().toLowerCase());

        Set<UUID> playerIds = new LinkedHashSet<>();
        playerIds.add(creatorId);

        if (params.opponentIds() != null) {
            playerIds.addAll(params.opponentIds());
        }

        // Compléter avec un joueur virtuel si nécessaire pour les jeux à 2 joueurs
        int expectedPlayers = (params.numberOfPlayers() != null && params.numberOfPlayers() > 0)
                ? params.numberOfPlayers()
                : 2;

        if (!"15 puzzle".equalsIgnoreCase(plugin.getId())) {
            while (playerIds.size() < expectedPlayers) {
                playerIds.add(UUID.randomUUID());
            }
        }

        Game newGame = plugin.createGame(params.boardSize(), playerIds);
        return gameDao.upsert(newGame);
    }

    @Override
    public Game getGame(UUID gameId) {
        return gameDao.findById(gameId.toString()).orElse(null);
    }

    @Override
    public List<Game> getGamesForUser(UUID userId) {
        return gameDao.findAll()
                .filter(game -> game.getPlayerIds() != null && game.getPlayerIds().contains(userId))
                .toList();
    }

    @Override
    public Game playMove(UUID gameId, UUID userId, CellPosition position) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new NoSuchElementException("Aucune partie trouvée avec l'identifiant " + gameId);
        }

        if (!userId.equals(game.getCurrentPlayerId())) {
            throw new NotPlayerTurnException("Ce n'est pas votre tour de jouer !");
        }

        Token token = game.getRemainingTokens().stream()
                .filter(t -> t.getOwnerId().map(id -> id.equals(userId)).orElse(false))
                .findFirst()
                .orElseGet(() -> game.getBoard().values().stream()
                        .filter(t -> t.canMove() && t.getOwnerId().map(id -> id.equals(userId)).orElse(true))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Aucun jeton disponible pour jouer")));

        try {
            token.moveTo(position);
        } catch (InvalidPositionException e) {
            throw new IllegalArgumentException("Position invalide pour ce coup : (" + position.x() + ", " + position.y() + ")", e);
        }

        return gameDao.upsert(game);
    }
}