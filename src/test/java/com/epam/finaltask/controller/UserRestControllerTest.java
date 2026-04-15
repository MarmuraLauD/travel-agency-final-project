package com.epam.finaltask.controller;

import com.epam.finaltask.dto.UpdateUserDto;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", authorities = {"user:create"})
    void createUser_ValidData_Success() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setPassword("password123");
        userDTO.setPhoneNumber("+380123456789");
        userDTO.setRole("USER");
        userDTO.setBalance(0.0);

        UserDTO createdUser = new UserDTO();
        createdUser.setId(UUID.randomUUID().toString());
        createdUser.setUsername("newuser");

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));

        verify(userService, times(1)).createUser(any(UserDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"user:create"})
    void createUser_DuplicateUsername_ReturnsConflict() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("existinguser");
        userDTO.setPassword("password123");

        when(userService.createUser(any(UserDTO.class)))
                .thenThrow(new com.epam.finaltask.exception.DuplicateRequestException("Username is already in use"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"user:update"})
    void updateUser_ValidData_Success() throws Exception {
        // Given
        String username = "testuser";

        UpdateUserDto updateDTO = new UpdateUserDto();
        updateDTO.setUsername("updateduser");
        updateDTO.setPhoneNumber("+380987654321");
        updateDTO.setBalance(1000.0);
        updateDTO.setActive(true);
        updateDTO.setRole("MANAGER");

        UserDTO mappedDTO = new UserDTO();
        mappedDTO.setUsername("updateduser");

        UserDTO updatedUser = new UserDTO();
        updatedUser.setUsername("updateduser");

        when(userMapper.toUserDTO(any(UpdateUserDto.class))).thenReturn(mappedDTO);
        when(userService.updateUser(eq(username), any(UserDTO.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(patch("/api/users/{username}", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"));

        verify(userService, times(1)).updateUser(eq(username), any(UserDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"user:read"})
    void getUserByUsername_ExistingUser_Success() throws Exception {
        // Given
        String username = "testuser";

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setId(UUID.randomUUID().toString());

        when(userService.getUserByUsername(username)).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(get("/api/users/username/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        verify(userService, times(1)).getUserByUsername(username);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"user:read"})
    void getUserById_ExistingUser_Success() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId.toString());
        userDTO.setUsername("testuser");

        when(userService.getUserById(userId)).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(get("/api/users/id/{id}", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"user:delete"})
    void deleteUser_ExistingUser_Success() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();

        doNothing().when(userService).deleteUserById(userId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId.toString()))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUserById(userId);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"user:update"})
    void changeAccountStatus_ValidData_Success() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId.toString());
        userDTO.setActive(false);

        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(userId.toString());
        updatedUser.setActive(false);

        when(userService.changeAccountStatus(any(UserDTO.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(patch("/api/users/{id}/status", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));

        verify(userService, times(1)).changeAccountStatus(any(UserDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"user:update"})
    void changeUserRole_ValidRole_Success() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Role newRole = Role.MANAGER;

        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(userId.toString());
        updatedUser.setRole("MANAGER");

        when(userService.changeUserRole(userId, newRole)).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(patch("/api/users/{id}/role", userId.toString())
                        .param("role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));

        verify(userService, times(1)).changeUserRole(userId, newRole);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUser_Authenticated_Success() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setId(UUID.randomUUID().toString());

        when(userService.getUserByUsername("testuser")).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).getUserByUsername("testuser");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"user:update"})
    void getAllUsers_AsAdmin_ReturnsAll() throws Exception {
        // Given
        UserDTO user1 = new UserDTO();
        user1.setUsername("user1");
        user1.setId(UUID.randomUUID().toString());

        UserDTO user2 = new UserDTO();
        user2.setUsername("user2");
        user2.setId(UUID.randomUUID().toString());

        when(userService.findAll()).thenReturn(List.of(user1, user2));

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));

        verify(userService, times(1)).findAll();
    }
}
