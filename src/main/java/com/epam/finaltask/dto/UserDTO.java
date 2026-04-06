package com.epam.finaltask.dto;

import java.util.List;

import com.epam.finaltask.model.Voucher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {

	private String id;

	@NotBlank
	@Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
	private String username;

	@NotBlank
	@Size(min = 6, message = "Password must be at least 6 characters long")
	private String password;

	@NotBlank
	private String role;

	@NotNull
	private List<Voucher> vouchers;

	@NotNull
	private String phoneNumber;

	@NotNull
	@PositiveOrZero
	private Double balance;

	private boolean active;
	
}
