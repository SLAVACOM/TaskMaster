package com.slavacom.auth_service.controller;

import com.slavacom.auth_service.dto.*;
import com.slavacom.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
	@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * Регистрация нового пользователя
	 *
	 * @param request данные для регистрации
	 * @return токены доступа
	 */
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		log.info("POST /api/auth/register - Registering user with: {}", request);

		return authService.register(request);
	}

	/**
	 * Вход пользователя
	 *
	 * @param request данные для входа (логин/email и пароль)
	 * @return токены доступа
	 */
	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		log.info("POST /api/auth/login - User login attempt for login: {}", request.getLogin());
		return authService.login(request);
	}

	/**
	 * Обновление токенов
	 *
	 * @param request refresh токен
	 * @return новые токены
	 */
	@PostMapping("/refresh")
	public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		log.info("POST /api/auth/refresh - Refreshing tokens");
		return authService.refreshToken(request);
	}

	/**
	 * Валидация токена
	 *
	 * @param token access токен
	 * @return информация о пользователе
	 */
	@GetMapping("/validate")
	public UserInfoDto validateToken(@RequestParam String token) {
		log.info("GET /api/auth/validate - Validating token");
		return authService.validateToken(token);
	}

	/**
	 * Изменение пароля
	 *
	 * @param userId  ID пользователя
	 * @param request старый и новый пароль
	 */
	@PutMapping("/users/{userId}/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(
			@PathVariable UUID userId,
			@Valid @RequestBody ChangePasswordRequest request) {
		log.info("PUT /api/auth/users/{}/password - Changing password", userId);
		authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
	}

	/**
	 * Обновление роли пользователя (только для администраторов)
	 *
	 * @param userId  ID пользователя
	 * @param request новая роль
	 */
	@PutMapping("/users/{userId}/role")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateRole(
			@PathVariable UUID userId,
			@Valid @RequestBody UpdateRoleRequest request) {
		log.info("PUT /api/auth/users/{}/role - Updating role to {}", userId, request.getRole());
		authService.updateRole(userId, request.getRole());
	}

	/**
	 * Удаление учетных данных пользователя
	 *
	 * @param userId ID пользователя
	 */
	@DeleteMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable UUID userId) {
		log.info("DELETE /api/auth/users/{} - Deleting user credentials", userId);
		authService.deleteUser(userId);
	}
}

