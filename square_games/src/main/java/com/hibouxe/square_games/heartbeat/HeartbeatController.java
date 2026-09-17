package com.hibouxe.square_games.heartbeat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Santé & Supervision", description = "Sonde de pulsation (Heartbeat)")
public class HeartbeatController {

    @Autowired
    private HeartbeatSensor heartbeatSensor;

    @GetMapping("/heartbeat")
    @Operation(
            summary = "Vérifier la pulsation de l'application",
            description = "Retourne la valeur actuelle de la fréquence cardiaque / pulsation du serveur."
    )
    @ApiResponse(responseCode = "200", description = "Valeur du capteur de battement de coeur")
    public int getHeartbeat() {
        return heartbeatSensor.get();
    }
}
