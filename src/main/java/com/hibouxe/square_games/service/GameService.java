package com.hibouxe.square_games.service;

import com.hibouxe.square_games.GameCreationParams;

import java.util.Map;

public interface GameService {

    String createNewGame(GameCreationParams params);

    Map<String, Object> getGame(String gameId);
}
