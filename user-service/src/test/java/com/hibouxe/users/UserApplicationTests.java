package com.hibouxe.users;

import com.hibouxe.users.dto.CreateUserDto;
import com.hibouxe.users.dto.UserResponseDto;
import com.hibouxe.users.service.JwtService;
import com.hibouxe.users.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    @DisplayName("Cycle de vie complet : Création avec mot de passe et rôle, consultation, validité, suppression")
    void testUserLifecycle() {
        // 1. Création avec rôle et mot de passe
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String username = "Alice_" + unique;
        String email = "alice_" + unique + "@example.com";
        CreateUserDto dto = new CreateUserDto(username, email, "SuperSecret123!", "ROLE_USER");
        UserResponseDto created = userService.createUser(dto);

        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals(username, created.username());
        assertEquals(email, created.email());
        assertEquals("ROLE_USER", created.role());

        // 2. Vérification validité (doit être true)
        assertTrue(userService.isUserValid(created.id()));

        // 3. Récupération
        Optional<UserResponseDto> fetched = userService.getUser(created.id());
        assertTrue(fetched.isPresent());
        assertEquals(username, fetched.get().username());
        assertEquals("ROLE_USER", fetched.get().role());

        // 4. ID inconnu doit être faux
        assertFalse(userService.isUserValid(UUID.randomUUID()));

        // 5. Suppression
        assertTrue(userService.deleteUser(created.id()));
        assertFalse(userService.isUserValid(created.id()));
        assertTrue(userService.getUser(created.id()).isEmpty());
    }

    @Test
    @DisplayName("JWT : Génération, extraction des claims (username, userId, roles) et validation cryptographique")
    void testJwtGenerationAndValidation() {
        UUID userId = UUID.randomUUID();
        String username = "TestUser";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        // 1. Génération
        String token = jwtService.generateToken(username, userId, roles);
        assertNotNull(token);
        assertFalse(token.isBlank());

        // 2. Extraction du sujet (username)
        assertEquals(username, jwtService.extractUsername(token));

        // 3. Extraction du userId
        assertEquals(userId, jwtService.extractUserId(token));

        // 4. Extraction des rôles
        List<String> extractedRoles = jwtService.extractRoles(token);
        assertNotNull(extractedRoles);
        assertTrue(extractedRoles.contains("ROLE_USER"));
        assertTrue(extractedRoles.contains("ROLE_ADMIN"));

        // 5. Validation
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("Sécurité : Authentification via AuthenticationManager avec mot de passe BCrypt")
    void testAuthenticationManagerLogin() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String username = "AuthUser_" + unique;
        String email = "auth_" + unique + "@example.com";
        String rawPassword = "ValidPassword123!";

        userService.createUser(new CreateUserDto(username, email, rawPassword, "ROLE_USER"));

        // Authentification réussie
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, rawPassword)
        );
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());

        // Mauvais mot de passe -> BadCredentialsException
        assertThrows(BadCredentialsException.class, () -> {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, "WrongPassword!")
            );
        });
    }
}
