package com.slavacom.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
	@NotNull(message = "Email cannot be null")
	@Email()
	private String email;

	@NotNull(message = "First name cannot be null")
	@NotBlank(message = "First name cannot be blank")
	private String firstName;

	@NotNull(message = "Last name cannot be null")
	@NotBlank(message = "Last name cannot be blank")
	private String lastName;

	@NotNull(message = "Username cannot be null")
	@NotBlank(message = "Username cannot be blank")
	private String username;

	@NotNull(message = "Password cannot be null")
	@Size(min = 8, message = "Password must be at least 8 characters long")
	private String password;
}

