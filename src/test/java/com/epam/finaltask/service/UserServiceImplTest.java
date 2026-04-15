package com.epam.finaltask.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.epam.finaltask.exception.DuplicateRequestException;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.service.security.RefreshTokenService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserMapper userMapper;

  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  private UserServiceImpl userService;

  @Test
  void getUserByUsername_UserExists_Success() {
    // Given
    String username = "existingUser";
    User user = new User();
    user.setUsername(username);

    UserDTO expectedUserDTO = new UserDTO();
    expectedUserDTO.setUsername(username);

    when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
    when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedUserDTO);

    // When
    UserDTO result = userService.getUserByUsername(username);

    // Then
    assertNotNull(result, "The returned UserDTO should not be null");
    assertEquals(expectedUserDTO.getUsername(), result.getUsername(),
        "The username should match the expected value");

    verify(userRepository, times(1)).findUserByUsername(username);
    verify(userMapper, times(1)).toUserDTO(any(User.class));
  }

  @Test
  void changeAccountStatus_UserExist_Success() {
    // Given
    String userId = UUID.randomUUID().toString();
    UserDTO userDTO = new UserDTO();
    userDTO.setId(userId);
    userDTO.setActive(true);

    User user = new User();
    user.setId(UUID.fromString(userId));
    user.setActive(false);

    User updatedUser = new User();
    updatedUser.setId(UUID.fromString(userId));
    updatedUser.setActive(true);

    when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));
    when(userMapper.toUser(any(UserDTO.class))).thenReturn(updatedUser);
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);
    when(userMapper.toUserDTO(any(User.class))).thenReturn(userDTO);

    // When
    UserDTO resultDTO = userService.changeAccountStatus(userDTO);

    // Then
    assertNotNull(resultDTO, "The returned UserDTO should not be null");
    assertTrue(resultDTO.isActive(), "The account status should be updated to true");

    verify(userRepository, times(1)).findById(UUID.fromString(userId));
    verify(userRepository, times(1)).save(any(User.class));
  }


  @Test
  void getUserById_UserExist_Success() {
    // Given
    UUID id = UUID.randomUUID();
    User user = new User();
    user.setId(id);

    UserDTO expectedUserDTO = new UserDTO();
    expectedUserDTO.setId(id.toString());

    when(userRepository.findById(id)).thenReturn(Optional.of(user));
    when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedUserDTO);

    // When
    UserDTO resultDTO = userService.getUserById(id);

    // Then
    assertNotNull(resultDTO, "The returned UserDTO should not be null");
    assertEquals(expectedUserDTO.getId(), resultDTO.getId(),
        "The user ID should match the expected value");

    verify(userRepository, times(1)).findById(id);
    verify(userMapper, times(1)).toUserDTO(any(User.class));
  }

  @Test
  void register_NewUser_Success() {
    // Given
    UserDTO userDTO = new UserDTO();
    userDTO.setUsername("newuser");
    userDTO.setPassword("password123");

    User user = new User();
    user.setUsername("newuser");

    User savedUser = new User();
    savedUser.setId(UUID.randomUUID());
    savedUser.setUsername("newuser");
    savedUser.setRole(Role.USER);

    UserDTO expectedDTO = new UserDTO();
    expectedDTO.setUsername("newuser");

    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userMapper.toUser(any(UserDTO.class))).thenReturn(user);
    when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-password");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedDTO);

    // When
    UserDTO result = userService.register(userDTO);

    // Then
    assertNotNull(result);
    assertEquals("newuser", result.getUsername());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void register_DuplicateUsername_ThrowsDuplicateException() {
    // Given
    UserDTO userDTO = new UserDTO();
    userDTO.setUsername("existinguser");

    when(userRepository.existsByUsername("existinguser")).thenReturn(true);

    // When & Then
    assertThrows(DuplicateRequestException.class, () ->
            userService.register(userDTO)
    );
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void createUser_ValidData_Success() {
    // Given
    UserDTO userDTO = new UserDTO();
    userDTO.setUsername("adminuser");
    userDTO.setPassword("adminpass");
    userDTO.setRole("ADMIN");
    userDTO.setBalance(1000.0);

    User user = new User();
    user.setUsername("adminuser");

    User savedUser = new User();
    savedUser.setId(UUID.randomUUID());
    savedUser.setUsername("adminuser");
    savedUser.setRole(Role.ADMIN);

    UserDTO expectedDTO = new UserDTO();
    expectedDTO.setUsername("adminuser");
    expectedDTO.setRole("ADMIN");

    when(userRepository.existsByUsername("adminuser")).thenReturn(false);
    when(userMapper.toUser(any(UserDTO.class))).thenReturn(user);
    when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-password");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedDTO);

    // When
    UserDTO result = userService.createUser(userDTO);

    // Then
    assertNotNull(result);
    assertEquals("adminuser", result.getUsername());
    assertEquals("ADMIN", result.getRole());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void updateUser_ExistingUser_Success() {
    // Given
    String username = "existinguser";
    UserDTO updateDTO = new UserDTO();
    updateDTO.setUsername("updateduser");
    updateDTO.setRole("MANAGER");
    updateDTO.setBalance(500.0);
    updateDTO.setActive(true);

    User existingUser = new User();
    existingUser.setUsername(username);

    UserDTO expectedDTO = new UserDTO();
    expectedDTO.setUsername("updateduser");

    when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenReturn(existingUser);
    when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedDTO);

    // When
    UserDTO result = userService.updateUser(username, updateDTO);

    // Then
    assertNotNull(result);
    assertEquals("updateduser", result.getUsername());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void deleteUserById_ExistingUser_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = new User();
    user.setId(userId);

    when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));
    doNothing().when(refreshTokenService).deleteByUserId(userId);
    doNothing().when(userRepository).delete(any(User.class));

    // When
    userService.deleteUserById(userId);

    // Then
    verify(refreshTokenService, times(1)).deleteByUserId(userId);
    verify(userRepository, times(1)).delete(any(User.class));
  }

  @Test
  void changeUserRole_ValidUser_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    Role newRole = Role.MANAGER;

    User user = new User();
    user.setId(userId);
    user.setUsername("testuser");
    user.setRole(Role.USER);

    UserDTO expectedDTO = new UserDTO();
    expectedDTO.setRole("MANAGER");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toUserDTO(any(User.class))).thenReturn(expectedDTO);

    // When
    UserDTO result = userService.changeUserRole(userId, newRole);

    // Then
    assertNotNull(result);
    assertEquals("MANAGER", result.getRole());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void findAll_MultipleUsers_ReturnsList() {
    // Given
    User user1 = new User();
    user1.setUsername("user1");

    User user2 = new User();
    user2.setUsername("user2");

    UserDTO dto1 = new UserDTO();
    dto1.setUsername("user1");

    UserDTO dto2 = new UserDTO();
    dto2.setUsername("user2");

    when(userRepository.findAll()).thenReturn(List.of(user1, user2));
    when(userMapper.toUserDTO(user1)).thenReturn(dto1);
    when(userMapper.toUserDTO(user2)).thenReturn(dto2);

    // When
    List<UserDTO> result = userService.findAll();

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  void getUserByUsername_NonExistent_ThrowsEntityNotFoundException() {
    // Given
    String username = "nonexistent";

    when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(EntityNotFoundException.class, () ->
            userService.getUserByUsername(username)
    );
  }

}
