package com.hibouxe.square_games.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Locale;

@RestController
@Tag(name = "Catalogue de Jeux", description = "Consultation des jeux disponibles sur la plateforme")
public class GameCatalogController {

    private final GameCatalog gameCatalog;

    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @GetMapping(value = "/games", headers = "!Authorization")
    @Operation(
            summary = "Lister les types de jeux disponibles (Public)",
            description = "Retourne les noms des jeux de plateau pris en charge par l'application, traduits selon l'entête Accept-Language."
    )
    @ApiResponse(responseCode = "200", description = "Liste des noms de jeux disponibles")
    public Collection<String> getGames(
            @Parameter(hidden = true) Locale locale) {
        return gameCatalog.getGameNames(locale);
    }

    @GetMapping("/games/catalog")
    @Operation(
            summary = "Consulter le catalogue des jeux",
            description = "Route explicite de consultation du catalogue."
    )
    public Collection<String> getCatalog(
            @Parameter(hidden = true) Locale locale) {
        return gameCatalog.getGameNames(locale);
    }
}