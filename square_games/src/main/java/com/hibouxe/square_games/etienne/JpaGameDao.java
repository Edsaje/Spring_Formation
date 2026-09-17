package com.hibouxe.square_games.etienne;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import fr.le_campus_numerique.square_games.engine.TokenPosition;
import fr.le_campus_numerique.square_games.engine.connectfour.ConnectFourGameFactory;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Repository
@Profile("jpa")
@Transactional
public class JpaGameDao implements GameDao {

    private final GameEntityRepository repository;
    private final Map<String, GameFactory> factories = new HashMap<>();

    public JpaGameDao(GameEntityRepository repository) {
        this.repository = repository;
        GameFactory tictactoe = new TicTacToeGameFactory();
        GameFactory connect4 = new ConnectFourGameFactory();
        GameFactory taquin = new TaquinGameFactory();

        factories.put(tictactoe.getGameFactoryId(), tictactoe);
        factories.put(connect4.getGameFactoryId(), connect4);
        factories.put(taquin.getGameFactoryId(), taquin);
    }

    @Override
    public Stream<Game> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .filter(Objects::nonNull);
    }

    @Override
    public Optional<Game> findById(String gameId) {
        return repository.findById(gameId).map(this::toDomain);
    }

    @Override
    public Game upsert(Game game) {
        GameEntity entity = toEntity(game);
        repository.save(entity);
        return game;
    }

    @Override
    public void delete(String gameId) {
        repository.deleteById(gameId);
    }

    // --- Méthodes de conversion ---

    private GameEntity toEntity(Game game) {
        GameEntity entity = new GameEntity();
        entity.id = game.getId().toString();
        entity.factoryId = game.getFactoryId();
        entity.boardSize = game.getBoardSize();
        entity.playerIds = String.join(",", game.getPlayerIds().stream().map(UUID::toString).toList());

        // Jetons sur le plateau
        game.getBoard().forEach((position, token) -> {
            GameTokenEntity t = new GameTokenEntity();
            t.ownerId = token.getOwnerId().map(UUID::toString).orElse(null);
            t.name = token.getName();
            t.removed = false;
            t.x = position.x();
            t.y = position.y();
            entity.tokens.add(t);
        });

        // Jetons retirés
        game.getRemovedTokens().forEach(token -> {
            GameTokenEntity t = new GameTokenEntity();
            t.ownerId = token.getOwnerId().map(UUID::toString).orElse(null);
            t.name = token.getName();
            t.removed = true;
            entity.tokens.add(t);
        });

        return entity;
    }

    private Game toDomain(GameEntity entity) {
        GameFactory factory = factories.get(entity.factoryId);
        if (factory == null) return null;

        List<UUID> players = Arrays.stream(entity.playerIds.split(","))
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .toList();

        List<TokenPosition<UUID>> boardTokens = new ArrayList<>();
        List<TokenPosition<UUID>> removedTokens = new ArrayList<>();

        for (GameTokenEntity token : entity.tokens) {
            UUID owner = (token.ownerId != null) ? UUID.fromString(token.ownerId) : null;
            if (token.removed) {
                removedTokens.add(new TokenPosition<>(owner, token.name, 0, 0));
            } else if (token.x != null && token.y != null) {
                boardTokens.add(new TokenPosition<>(owner, token.name, token.x, token.y));
            }
        }

        try {
            return factory.createGameWithIds(
                    UUID.fromString(entity.id),
                    entity.boardSize,
                    players,
                    boardTokens,
                    removedTokens
            );
        } catch (Exception e) {
            return null;
        }
    }
}