package com.hibouxe.users.service;

import com.hibouxe.users.dto.CreateUserDto;
import com.hibouxe.users.dto.UserResponseDto;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserResponseDto createUser(CreateUserDto dto);

    Optional<UserResponseDto> getUser(UUID id);

    boolean deleteUser(UUID id);

    boolean isUserValid(UUID id);
}
