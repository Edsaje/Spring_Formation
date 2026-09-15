package com.hibouxe.square_games.etienne;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEntityRepository extends JpaRepository<GameEntity, String> {
    // Aucune méthode à écrire ! Spring génère save(), findById(), delete()... à la volée.
}