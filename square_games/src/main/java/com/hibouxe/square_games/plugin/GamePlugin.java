package com.hibouxe.square_games.plugin;

import fr.le_campus_numerique.square_games.engine.Game;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public interface GamePlugin {
    String getId();

    String getName(Locale locale);

    Game createGame(Integer playerCount, Integer boardSize);

    Game createGame(Integer boardSize, Set<UUID> playerIds);
}
