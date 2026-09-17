package com.hibouxe.users.service;

import com.hibouxe.users.dao.UserRepository;
import com.hibouxe.users.dto.CreateUserDto;
import com.hibouxe.users.dto.UserResponseDto;
import com.hibouxe.users.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponseDto createUser(CreateUserDto dto) {
        if (dto.username() == null || dto.username().isBlank()) {
            throw new IllegalArgumentException("Le nom d'utilisateur ne peut pas être vide.");
        }
        if (dto.email() == null || dto.email().isBlank()) {
            throw new IllegalArgumentException("L'adresse email ne peut pas être vide.");
        }

        UserEntity entity = new UserEntity(dto.username(), dto.email());
        UserEntity saved = userRepository.save(entity);
        return new UserResponseDto(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    @Override
    public Optional<UserResponseDto> getUser(UUID id) {
        return userRepository.findById(id)
                .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getEmail()));
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
