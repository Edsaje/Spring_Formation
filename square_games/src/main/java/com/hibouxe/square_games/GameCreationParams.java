package com.hibouxe.square_games;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Paramètres requis pour la création d'une nouvelle partie de jeu")
public record GameCreationParams(
        @Schema(description = "Type de jeu (ex: tictactoe, connectfour, 15 puzzle)", example = "tictactoe", requiredMode = Schema.RequiredMode.REQUIRED)
        String gameType,

        @Schema(description = "Nombre de joueurs dans la partie", example = "2")
        Integer numberOfPlayers,

        @Schema(description = "Taille du plateau (côté de la grille)", example = "3")
        Integer boardSize,

        @Schema(description = "Liste optionnelle d'identifiants UUID des adversaires invités")
        List<UUID> opponentIds
) {
    public GameCreationParams(String gameType, int numberOfPlayers, int boardSize) {
        this(gameType, numberOfPlayers, boardSize, null);
    }
}
