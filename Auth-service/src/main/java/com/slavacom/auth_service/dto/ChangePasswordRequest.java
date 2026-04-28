package com.slavacom.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
	@NotBlank(message = "Old password cannot be blank")
	private String oldPassword;

	@NotBlank(message = "New password cannot be blank")
	@Size(min = 8, message = "New password must be at least 8 characters long")
	private String newPassword;
}

