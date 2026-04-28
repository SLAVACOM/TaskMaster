package com.slavacom.auth_service.dto;

import com.slavacom.auth_service.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
	@NotNull(message = "Role cannot be null")
	private Role role;
}

