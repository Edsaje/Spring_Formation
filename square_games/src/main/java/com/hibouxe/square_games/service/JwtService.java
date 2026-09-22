package com.hibouxe.square_games.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret:MaCleSecreteCommunePartageeEntreLesMicroservices123456789!}")
    private String secretKey;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, UUID userId, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId != null ? userId.toString() : "")
                .claim("roles", roles)
                .claim("role", roles != null && !roles.isEmpty() ? roles.getFirst() : "ROLE_USER")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        if (userIdStr != null && !userIdStr.isBlank()) {
            return UUID.fromString(userIdStr);
        }
        // Si le subject est un UUID
        try {
            return UUID.fromString(extractUsername(token));
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        List<String> roles = extractClaim(token, claims -> claims.get("roles", List.class));
        if (roles == null) {
            String role = extractClaim(token, claims -> claims.get("role", String.class));
            return role != null ? List.of(role) : List.of("ROLE_USER");
        }
        return roles;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
