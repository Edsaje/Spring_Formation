package com.hibouxe.square_games.etienne;

import jakarta.persistence.*;

@Entity
@Table(name = "game_tokens")
public class GameTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String ownerId;
    public String name;
    public boolean removed;
    public Integer x;
    public Integer y;

    public GameTokenEntity() {}
}