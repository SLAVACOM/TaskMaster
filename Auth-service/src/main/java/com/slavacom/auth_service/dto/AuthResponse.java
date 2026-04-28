package com.slavacom.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
	private String accessToken;
	private String refreshToken;
	private String userId;
	private String profileId;

	// Дополнительная информация о состоянии пользователя
	private boolean isEmailVerified;
	private boolean needsOrganizationSetup;

	// Данные пользователя
	private String firstName;
	private String lastName;
	private String email;
	private String username;

	// Данные организации
	private OrganizationInfo organization;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OrganizationInfo {
		private String id;
		private String name;
		private String description;
		private String userId;
		private String currentUserId;
		private String role;
	}
}

