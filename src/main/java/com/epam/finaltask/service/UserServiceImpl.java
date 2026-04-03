package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.DuplicateRequestException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final UserRepository userRepository;

	@Override
	public UserDTO register(UserDTO userDTO) {
		if(userRepository.existsByUsername(userDTO.getUsername())) {
			throw new DuplicateRequestException("Username is already in use");
		}
		User user = userMapper.toUser(userDTO);
		user.setRole(Role.USER);
		user.setBalance(BigDecimal.ZERO);
		user.setAccountStatus(true);
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
		user.setAccountStatus(userDTO.isActive());
		userRepository.save(user);
		return userMapper.toUserDTO(user);
	}

	@Override
	public UserDTO getUserByUsername(String username) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("No such username"));
		return userMapper.toUserDTO(user);
	}

	@Override
	public UserDTO changeAccountStatus(UserDTO userDTO) {
		User user = userRepository.findUserByUsername(userDTO.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("No such username"));
		user.setAccountStatus(userDTO.isActive());
		userRepository.save(user);
		return userMapper.toUserDTO(user);
	}

	@Override
	public UserDTO getUserById(UUID id) {
		return userMapper.toUserDTO(userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("No such ID")));
	}

}
