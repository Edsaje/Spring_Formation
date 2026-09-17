package com.hibouxe.square_games.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TicTacToePlugin implements GamePlugin {

    private final GameFactory factory = new TicTacToeGameFactory();
    private final MessageSource messageSource;

    private final int defaultPlayerCount;
    private final int defaultBoardSize;

    public TicTacToePlugin(
            MessageSource messageSource,
            @Value("${game.tictactoe.default-player-count}") int defaultPlayerCount,
            @Value("${game.tictactoe.default-board-size}") int defaultBoardSize) {
        this.messageSource = messageSource;
        this.defaultPlayerCount = defaultPlayerCount;
        this.defaultBoardSize = defaultBoardSize;
    }

    @Override
    public String getId() {
        return factory.getGameFactoryId();
    }

    @Override
    public String getName(Locale locale) {
        // Recherche la clé 'game.tictactoe.name' dans les fichiers messages.properties
        return messageSource.getMessage("game.tictactoe.name", null, locale);
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