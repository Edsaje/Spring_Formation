package com.hibouxe.square_games.service;

import com.hibouxe.square_games.GameCreationParams;
import com.hibouxe.square_games.etienne.GameDao;
import com.hibouxe.square_games.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    private final GameDao gameDao; // Prise universelle
    private final Map<String, GamePlugin> plugins = new HashMap<>();

    public GameServiceImpl(GameDao gameDao, List<GamePlugin> availablePlugins) {
        this.gameDao = gameDao;
        for (GamePlugin plugin : availablePlugins) {
            this.plugins.put(plugin.getId().toLowerCase(), plugin);
        }
    }

    @Override
    public Game createNewGame(GameCreationParams params) {
        GamePlugin plugin = plugins.get(params.gameType().toLowerCase());
        Game newGame = plugin.createGame(params.numberOfPlayers(), params.boardSize());

        // La sauvegarde est 100% déléguée au DAO !
        return gameDao.upsert(newGame);
    }

    @Override
    public Game getGame(UUID gameId) {
        return gameDao.findById(gameId.toString()).orElse(null);
    }
}