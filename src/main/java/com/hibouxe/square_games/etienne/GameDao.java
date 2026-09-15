package com.hibouxe.square_games.etienne;

import fr.le_campus_numerique.square_games.engine.Game;

import java.util.Optional;
import java.util.stream.Stream;

public interface GameDao {

    /**
     * Récupère l'ensemble de toutes les parties enregistrées sous forme de flux.
     */
    Stream<Game> findAll();

    /**
     * Recherche une partie par son identifiant unique.
     * Utilise Optional pour forcer le code appelant à traiter l'absence éventuelle de la partie.
     */
    Optional<Game> findById(String gameId);

    /**
     * Enregistre une partie (création si inexistante, mise à jour si déjà présente).
     * @return la partie enregistrée à jour.
     */
    Game upsert(Game game);

    /**
     * Supprime définitivement une partie par son identifiant.
     */
    void delete(String gameId);
}