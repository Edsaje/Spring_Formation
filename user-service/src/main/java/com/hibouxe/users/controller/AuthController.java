package com.hibouxe.users.controller;

import com.hibouxe.users.dao.UserRepository;
import com.hibouxe.users.dto.LoginRequestDto;
import com.hibouxe.users.dto.LoginResponseDto;
import com.hibouxe.users.entity.UserEntity;
import com.hibouxe.users.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentification", description = "Endpoints de connexion et d'émission de jetons JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authentifier un utilisateur et obtenir un JWT",
            description = "Valide le nom d'utilisateur et le mot de passe, puis délivre un token JWT signé contenant l'UUID, le nom et le rôle de l'utilisateur."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie, token JWT délivré"),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides (nom d'utilisateur ou mot de passe incorrect)")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserEntity user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

            String token = jwtService.generateToken(
                    user.getUsername(),
                    user.getId(),
                    List.of(user.getRole())
            );

            return ResponseEntity.ok(new LoginResponseDto(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getRole()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nom d'utilisateur ou mot de passe incorrect."));
        }
    }
}
