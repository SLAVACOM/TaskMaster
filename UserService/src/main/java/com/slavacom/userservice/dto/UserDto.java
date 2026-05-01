package com.slavacom.userservice.dto;

import com.slavacom.userservice.entity.User;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link User}
 */
public record UserDto(UUID id, String firstName, String lastName, String email, String username, Instant createdAt,
					  Instant updatedAt) {
}