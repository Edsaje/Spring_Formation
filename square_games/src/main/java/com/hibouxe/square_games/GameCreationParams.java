package com.hibouxe.square_games;

import java.util.List;
import java.util.UUID;

public record GameCreationParams(
        String gameType,
        Integer numberOfPlayers,
        Integer boardSize,
        List<UUID> opponentIds
) {
    public GameCreationParams(String gameType, int numberOfPlayers, int boardSize) {
        this(gameType, numberOfPlayers, boardSize, null);
    }
}
