package com.hibouxe.square_games.service;

import com.hibouxe.square_games.GameCreationParams;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;

import java.util.List;
import java.util.UUID;

public interface GameService {

    Game createNewGame(UUID creatorId, GameCreationParams params);

    Game getGame(UUID gameId);

    List<Game> getGamesForUser(UUID userId);

    Game playMove(UUID gameId, UUID userId, CellPosition position);
}
