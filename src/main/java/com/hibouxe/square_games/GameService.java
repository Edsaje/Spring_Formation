package com.hibouxe.square_games;

import java.util.Map;

public interface GameService {

    String createNewGame(GameCreationParams params);

    Map<String, Object> getGame(String gameId);
}
