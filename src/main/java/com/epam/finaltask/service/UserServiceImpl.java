package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.DuplicateRequestException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.security.RefreshTokenService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenService refreshTokenService;

	@Override
	public UserDTO register(UserDTO userDTO) {
		log.info("Attempting to register a new user with username: {}", userDTO.getUsername());
		if(userRepository.existsByUsername(userDTO.getUsername())) {
			throw new DuplicateRequestException("Username is already in use");
		}
		User user = userMapper.toUser(userDTO);
		user.setRole(Role.USER);
		user.setBalance(BigDecimal.ZERO);
		user.setActive(true);
		user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		log.info("User {} was successfully registered with role: {}", user.getUsername(), user.getRole());
		return userMapper.toUserDTO(userRepository.save(user));
	}

	@Override
	public UserDTO updateUser(String username, UserDTO userDTO) {
		User user = userRepository.findUserByUsername(username).orElseThrow(() -> new EntityNotFoundException("No such username"));
		user.setUsername(userDTO.getUsername());
		user.setPassword(userDTO.getPassword());
		user.setRole(Role.valueOf(userDTO.getRole()));
		user.setVouchers(userDTO.getVouchers());
		user.setPhoneNumber(userDTO.getPhoneNumber());
		user.setBalance(BigDecimal.valueOf(userDTO.getBalance()));
		user.setActive(userDTO.isActive());
		userRepository.save(user);
		return userMapper.toUserDTO(user);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDTO getUserByUsername(String username) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("No such username"));
		return userMapper.toUserDTO(user);
	}

	@Override
	public UserDTO changeAccountStatus(UserDTO userDTO) {
		log.info("Admin is changing account status for user ID: {} to {}", userDTO.getId(), userDTO.isActive());
		User user = userRepository.findById(UUID.fromString(userDTO.getId()))
				.orElseThrow(() -> new EntityNotFoundException("No such ID"));
		User mappedUser = userMapper.toUser(userDTO);
		user.setActive(mappedUser.isActive());
		log.info("Account status for user ID: {} successfully updated.", userDTO.getId());
		return userMapper.toUserDTO(userRepository.save(user));
	}

	@Override
	public UserDTO getUserById(UUID id) {
		return userMapper.toUserDTO(userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("No such ID")));
	}

	@Override
	public void deleteUserById(UUID id) {
		refreshTokenService.deleteByUserId(id);
		userRepository.delete(userRepository.findUserById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found with id " + id)));
	}

	@Override
	@Transactional
	public UserDTO changeUserRole(UUID userId, Role newRole) {
		log.info("Admin is changing role for user ID {} to {}", userId, newRole);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));

		user.setRole(newRole);
		User savedUser = userRepository.save(user);

		log.info("User {} role successfully updated to {}", user.getUsername(), newRole);
		return userMapper.toUserDTO(savedUser);
	}

}
