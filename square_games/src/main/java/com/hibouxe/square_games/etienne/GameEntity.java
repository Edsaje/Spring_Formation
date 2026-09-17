package com.hibouxe.square_games.etienne;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    public String id;

    public String factoryId;
    public int boardSize;
    public String playerIds;

    // cascade = CascadeType.ALL : sauvegarder la partie sauvegarde automatiquement ses jetons
    // orphanRemoval = true : supprimer un jeton de la liste le supprime de la base
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "game_id")
    public List<GameTokenEntity> tokens = new ArrayList<>();

    public GameEntity() {}
}