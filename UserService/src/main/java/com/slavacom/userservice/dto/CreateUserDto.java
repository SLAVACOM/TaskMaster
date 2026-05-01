package com.slavacom.userservice.dto;

import com.slavacom.userservice.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for {@link User}
 */
public record CreateUserDto(@NotNull UUID id, @NotNull @NotBlank String firstName, @NotNull @NotBlank String lastName,
							@NotNull @Email String email, @NotBlank String username) {
}