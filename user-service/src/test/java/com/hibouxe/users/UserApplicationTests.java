package com.hibouxe.users;

import com.hibouxe.users.dto.CreateUserDto;
import com.hibouxe.users.dto.UserResponseDto;
import com.hibouxe.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    void testUserLifecycle() {
        // 1. Création
        String email = "alice_" + UUID.randomUUID() + "@example.com";
        CreateUserDto dto = new CreateUserDto("Alice", email);
        UserResponseDto created = userService.createUser(dto);

        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals("Alice", created.username());
        assertEquals(email, created.email());

        // 2. Vérification validité (doit être true)
        assertTrue(userService.isUserValid(created.id()));

        // 3. Récupération
        Optional<UserResponseDto> fetched = userService.getUser(created.id());
        assertTrue(fetched.isPresent());
        assertEquals("Alice", fetched.get().username());

        // 4. ID inconnu doit être faux
        assertFalse(userService.isUserValid(UUID.randomUUID()));

        // 5. Suppression
        assertTrue(userService.deleteUser(created.id()));
        assertFalse(userService.isUserValid(created.id()));
        assertTrue(userService.getUser(created.id()).isEmpty());
    }
}
