package com.hibouxe.square_games.service;

import com.hibouxe.square_games.GameCreationParams;
import fr.le_campus_numerique.square_games.engine.Game;

import java.util.UUID;

public interface GameService {

    Game createNewGame(GameCreationParams params);

    Game getGame(UUID gameId);
}
