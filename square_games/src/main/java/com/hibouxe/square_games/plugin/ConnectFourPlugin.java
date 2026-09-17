package com.hibouxe.square_games.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import fr.le_campus_numerique.square_games.engine.connectfour.ConnectFourGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ConnectFourPlugin implements GamePlugin {

    private final GameFactory factory = new ConnectFourGameFactory();
    private final MessageSource messageSource;

    private final int defaultPlayerCount;
    private final int defaultBoardSize;

    public ConnectFourPlugin(
            MessageSource messageSource,
            @Value("${game.connectfour.default-player-count}") int defaultPlayerCount,
            @Value("${game.connectfour.default-board-size}") int defaultBoardSize) {
        this.messageSource = messageSource;
        this.defaultPlayerCount = defaultPlayerCount;
        this.defaultBoardSize = defaultBoardSize;
    }

    @Override
    public String getId() {
        return factory.getGameFactoryId(); // "connect4"
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.connect4.name", null, locale);
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