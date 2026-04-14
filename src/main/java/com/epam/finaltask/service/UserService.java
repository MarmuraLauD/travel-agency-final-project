package com.epam.finaltask.service;

import java.util.List;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.Role;

public interface UserService {
    UserDTO register(UserDTO userDTO);
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(String username, UserDTO userDTO);

    UserDTO getUserByUsername(String username);
    UserDTO changeAccountStatus(UserDTO userDTO);
    UserDTO getUserById(UUID id);
    void deleteUserById(UUID id);
    UserDTO changeUserRole(UUID userId, Role newRole);
    List<UserDTO> findAll();
    boolean existsByUsername(String username);
}
