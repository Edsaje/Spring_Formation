package com.hibouxe.square_games.plugin;

import fr.le_campus_numerique.square_games.engine.Game;

import java.util.Locale;

public interface GamePlugin {
    String getId();

    String getName(Locale locale);

    Game createGame(Integer playerCount, Integer boardSize);
}
