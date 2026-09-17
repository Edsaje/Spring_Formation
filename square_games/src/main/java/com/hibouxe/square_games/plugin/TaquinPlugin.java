package com.hibouxe.square_games.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TaquinPlugin implements GamePlugin {

    private final GameFactory factory = new TaquinGameFactory();
    private final MessageSource messageSource;

    private final int defaultPlayerCount;
    private final int defaultBoardSize;

    public TaquinPlugin(
            MessageSource messageSource,
            @Value("${game.taquin.default-player-count}") int defaultPlayerCount,
            @Value("${game.taquin.default-board-size}") int defaultBoardSize) {
        this.messageSource = messageSource;
        this.defaultPlayerCount = defaultPlayerCount;
        this.defaultBoardSize = defaultBoardSize;
    }

    @Override
    public String getId() {
        return factory.getGameFactoryId(); // "15 puzzle"
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.15puzzle.name", null, locale);
    }

    @Override
    public Game createGame(Integer playerCount, Integer boardSize) {
        int players = (playerCount != null) ? playerCount : defaultPlayerCount;
        int size = (boardSize != null) ? boardSize : defaultBoardSize;
        return factory.createGame(players, size);
    }

    @Override
    public Game createGame(Integer boardSize, java.util.Set<java.util.UUID> playerIds) {
        int size = (boardSize != null) ? boardSize : defaultBoardSize;
        return factory.createGame(size, playerIds);
    }
}