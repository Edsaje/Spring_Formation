package com.hibouxe.square_games.etienne;

import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
@Profile("memory")
public class InMemoryGameDao implements GameDao {

    private final Map<String, Game> memoryStorage = new HashMap<>();

    @Override
    public Stream<Game> findAll() {
        return memoryStorage.values().stream();
    }

    @Override
    public Optional<Game> findById(String gameId) {
        return Optional.ofNullable(memoryStorage.get(gameId));
    }

    @Override
    public Game upsert(Game game) {
        memoryStorage.put(game.getId().toString(), game);
        return game;
    }

    @Override
    public void delete(String gameId) {
        memoryStorage.remove(gameId);
    }
}