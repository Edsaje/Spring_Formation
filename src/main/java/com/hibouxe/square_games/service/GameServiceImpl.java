package com.hibouxe.square_games.service;

import com.hibouxe.square_games.GameCreationParams;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    private final List<String> supportedGames = List.of("tictactoe", "Chess");

    private final Map<String, Map<String, Object>> gamesDatabase = new HashMap<>();

    @Override
    public String createNewGame(GameCreationParams params) {
        if (!supportedGames.contains(params.gameType.toLowerCase())) {
            throw new IllegalArgumentException("Type de jeu non supporté : " + params.gameType);
        }

            String newGameId = UUID.randomUUID().toString();

            Map<String, Object> initialGameState = Map.of(
                    "id", newGameId,
                    "type", params.gameType,
                    "maxPlayers", params.numberOfPlayers,
                    "boardSize", params.boardSize,
                    "status", "INITIALIZED"
            );

            gamesDatabase.put(newGameId, initialGameState);

            return newGameId;
        }

        @Override
        public Map<String, Object> getGame (String gameId){
            if (!gamesDatabase.containsKey(gameId)) {
                return Map.of("error", "Aucune partie trouvée avec l'identifiant " + gameId);
            }
            return gamesDatabase.get(gameId);

        }
    }
