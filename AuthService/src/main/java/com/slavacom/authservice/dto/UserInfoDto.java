package com.slavacom.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для получения информации о пользователе из User Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
	private UUID id;
	private String username;
	private String email;
	private String firstName;
	private String lastName;
	private Boolean active;
	private UUID organizationId;
	private UUID profileId;
}

