package com.hibouxe.square_games.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Locale;

@RestController
public class GameCatalogController {

    private final GameCatalog gameCatalog;

    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @GetMapping("/games")
    public Collection<String> getGames(Locale locale) {
        // Spring injecte automatiquement la Locale résolue depuis Accept-Language !
        return gameCatalog.getGameNames(locale);
    }
}