package com.slavacom.user_service.dto;

import com.slavacom.user_service.entity.User;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link User}
 */
public record UserDto(UUID id, String firstName, String lastName, String email, String username, Instant createdAt,
					  Instant updatedAt) {
}