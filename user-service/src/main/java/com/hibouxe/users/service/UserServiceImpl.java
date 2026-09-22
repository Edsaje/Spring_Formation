package com.hibouxe.users.service;

import com.hibouxe.users.dao.UserRepository;
import com.hibouxe.users.dto.CreateUserDto;
import com.hibouxe.users.dto.UserResponseDto;
import com.hibouxe.users.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDto createUser(CreateUserDto dto) {
        if (dto.username() == null || dto.username().isBlank()) {
            throw new IllegalArgumentException("Le nom d'utilisateur ne peut pas être vide.");
        }
        if (dto.email() == null || dto.email().isBlank()) {
            throw new IllegalArgumentException("L'adresse email ne peut pas être vide.");
        }
        if (dto.password() == null || dto.password().isBlank()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }
        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé.");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée.");
        }

        String encodedPassword = passwordEncoder.encode(dto.password());
        String role = (dto.role() != null && !dto.role().isBlank()) ? dto.role().trim() : "ROLE_USER";
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }

        UserEntity entity = new UserEntity(dto.username().trim(), dto.email().trim(), encodedPassword, role);
        UserEntity saved = userRepository.save(entity);
        return new UserResponseDto(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole());
    }

    @Override
    public Optional<UserResponseDto> getUser(UUID id) {
        return userRepository.findById(id)
                .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole()))
                .toList();
    }

    @Override
    public boolean deleteUser(UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean isUserValid(UUID id) {
        return userRepository.existsById(id);
    }
}
