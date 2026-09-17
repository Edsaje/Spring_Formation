package com.hibouxe.square_games.catalog;

import com.hibouxe.square_games.plugin.GamePlugin;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Service
public class GameCatalogImpl implements GameCatalog {

    private final List<GamePlugin> plugins;

    // Spring injecte automatiquement tous les plugins disponibles (Morpion, Puissance 4, Taquin)
    public GameCatalogImpl(List<GamePlugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public Collection<String> getGameNames(Locale locale) {
        return plugins.stream()
                .map(plugin -> plugin.getName(locale))
                .toList();
    }
}