package com.hibouxe.square_games.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class UserValidationClient {

    private final RestClient userRestClient;

    public UserValidationClient(RestClient userRestClient) {
        this.userRestClient = userRestClient;
    }

    public boolean isUserValid(UUID userId) {
        if (userId == null) {
            return false;
        }
        try {
            Boolean isValid = userRestClient.get()
                    .uri("/users/{id}/valid", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        // Statut 4xx (404 par exemple) : utilisateur non trouvé ou inexistant
                    })
                    .body(Boolean.class);

            return Boolean.TRUE.equals(isValid);
        } catch (Exception e) {
            // Service distant non démarré, erreur réseau ou timeout
            return false;
        }
    }
}
