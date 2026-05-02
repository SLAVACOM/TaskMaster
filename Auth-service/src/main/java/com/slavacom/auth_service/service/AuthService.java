package com.slavacom.auth_service.service;

import com.slavacom.auth_service.client.UserServiceClient;
import com.slavacom.auth_service.dto.*;
import com.slavacom.auth_service.entity.User;
import com.slavacom.auth_service.enums.Role;
// import com.slavacom.auth_service.event.UserCreatedEvent; // Временно отключено - используем REST
import com.slavacom.auth_service.event.UserCreatedEvent;
import com.slavacom.auth_service.event.UserLoginEvent;
import com.slavacom.auth_service.exception.InvalidCredentialsException;
import com.slavacom.auth_service.exception.UserAlreadyExistsException;
import com.slavacom.auth_service.exception.UserNotFoundException;
import com.slavacom.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserServiceClient userServiceClient;
	private final UserEventProducer userEventProducer;

	/**
	 * Регистрация нового пользователя
	 * Проверяет существование пользователя в User Service через REST API перед созданием
	 * Создает пользователя в User Service через REST API
	 *
	 * @param request запрос на регистрацию
	 * @return ответ с токенами
	 */
	@Transactional
	public AuthResponse register(RegisterRequest request) {
		log.info("Starting registration for email: {}, username: {}", request.getEmail(), request.getUsername());

		// Проверяем через REST API, можно ли зарегистрировать пользователя
		var canRegisterResponse = userServiceClient.canRegister(request.getEmail(), request.getUsername());

		if (!canRegisterResponse.canRegister()) {
			log.warn("Registration failed: {}", canRegisterResponse.reason());
			throw new UserAlreadyExistsException(canRegisterResponse.reason());
		}

		// Дополнительная проверка обязательных полей
		if (request.getFirstName() == null || request.getFirstName().isBlank()) {
			log.error("Registration failed: firstName is null or blank");
			throw new IllegalArgumentException("First name is required");
		}
		if (request.getLastName() == null || request.getLastName().isBlank()) {
			log.error("Registration failed: lastName is null or blank");
			throw new IllegalArgumentException("Last name is required");
		}

		// Регистрируем пользователя в User Service и получаем данные с ID
		UserInfoDto userInfo = userServiceClient.registerUser(
				request.getEmail(),
				request.getUsername(),
				request.getFirstName(),
				request.getLastName()
		);

		if (userInfo == null) {
			log.error("Failed to register user in User Service");
			throw new RuntimeException("Failed to register user in User Service");
		}

		log.info("User registered in User Service with userId: {}", userInfo.getId());

		// Создаем учетные данные пользователя в Auth Service с полученным ID
		User user = User.builder()
				.userId(userInfo.getId())
				.role(Role.USER)
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.build();

		userRepository.save(user);
		log.info("Auth credentials created successfully with userId: {}", userInfo.getId());

		// Генерируем токены
		String accessToken = jwtService.generateAccessToken(user.getUserId(), user.getRole());
		String refreshToken = jwtService.generateRefreshToken(user.getUserId());

		// Получаем расширенную информацию о пользователе для определения состояния
		ExtendedUserInfoDto extendedUserInfo = userServiceClient.getExtendedUserInfo(userInfo.getId());

		log.info("User registered successfully with userId: {}", userInfo.getId());

		// Создаем информацию об организации для ответа
		AuthResponse.OrganizationInfo organizationInfo = null;
		if (extendedUserInfo.getOrganization() != null) {
			organizationInfo = mapOrganizationInfo(extendedUserInfo);
		}

		return AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.userId(extendedUserInfo.getUserId() != null ? extendedUserInfo.getUserId().toString() : null)
				.profileId(extendedUserInfo.getProfileId() != null ? extendedUserInfo.getProfileId().toString() : null)
				.isEmailVerified(extendedUserInfo.getIsEmailVerified() != null && extendedUserInfo.getIsEmailVerified())
				.needsOrganizationSetup(extendedUserInfo.getProfileId() == null) // Если profileId null, значит нужно создать организацию
				.firstName(extendedUserInfo.getFirstName())
				.lastName(extendedUserInfo.getLastName())
				.email(extendedUserInfo.getEmail())
				.username(extendedUserInfo.getUsername())
				.organization(organizationInfo)
				.build();
	}

	/**
	 * Аутентификация пользователя по логину/email
	 *
	 * @param request запрос на вход (логин/email и пароль)
	 * @return ответ с токенами
	 */
	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		log.info("Attempting to login user with login: {}", request.getLogin());

		// Шаг 1: Находим userId по логину/email в User Service
		UUID userId = findUserIdByLogin(request.getLogin());
		if (userId == null) {
			log.warn("User not found for login: {}", request.getLogin());
			throw new InvalidCredentialsException();
		}

		// Шаг 2: Находим пользователя в Auth Service
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> {
					log.warn("User credentials not found for userId: {}", userId);
					return new InvalidCredentialsException();
				});

		// Шаг 3: Проверяем пароль
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			log.warn("Invalid password for userId: {}", userId);
			throw new InvalidCredentialsException();
		}

		// Шаг 4: Получаем расширенную информацию о пользователе из User Service
		ExtendedUserInfoDto extendedUserInfo = userServiceClient.getExtendedUserInfo(userId);
//		if (extendedUserInfo.getActive() != null && !extendedUserInfo.getActive()) {
//			throw new InvalidCredentialsException("User account is not active");
//		}

		log.info("User logged in successfully with userId: {}", userId);

		// Шаг 5: Генерируем токены с расширенными данными
		String accessToken = jwtService.generateExtendedAccessToken(
				userId,
				user.getRole(),
				extendedUserInfo.getProfileId(),
				extendedUserInfo.getOrganizationId()
		);
		String refreshToken = jwtService.generateRefreshToken(userId);

		// Создаем информацию об организации для ответа
		AuthResponse.OrganizationInfo organizationInfo = null;
		if (extendedUserInfo.getOrganization() != null) {
			organizationInfo = mapOrganizationInfo(extendedUserInfo);
		}


		userEventProducer.sendUserLoginEvent(
				new UserLoginEvent(
						UUID.randomUUID(),
						userId,
						extendedUserInfo.getEmail()
				)
		);


		return AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.userId(extendedUserInfo.getUserId() != null ? extendedUserInfo.getUserId().toString() : null)
				.profileId(extendedUserInfo.getProfileId() != null ? extendedUserInfo.getProfileId().toString() : null)
				.isEmailVerified(extendedUserInfo.getIsEmailVerified() != null && extendedUserInfo.getIsEmailVerified())
				.needsOrganizationSetup(extendedUserInfo.getProfileId() == null) // Если profileId null, значит нужно создать организацию
				.firstName(extendedUserInfo.getFirstName())
				.lastName(extendedUserInfo.getLastName())
				.email(extendedUserInfo.getEmail())
				.username(extendedUserInfo.getUsername())
				.organization(organizationInfo)
				.build();
	}

	/**
	 * Поиск userId по логину (может быть username или email)
	 */
	private UUID findUserIdByLogin(String login) {
		// Сначала пробуем найти по username
		UUID userId = userServiceClient.getUserIdByLogin(login);

		// Если не найдено, пробуем найти по email
		if (userId == null) {
			userId = userServiceClient.getUserIdByEmail(login);
		}

		return userId;
	}

	private AuthResponse.OrganizationInfo mapOrganizationInfo(ExtendedUserInfoDto extendedUserInfo) {
		return AuthResponse.OrganizationInfo.builder()
				.id(extendedUserInfo.getOrganization().getId())
				.name(extendedUserInfo.getOrganization().getName())
				.description(extendedUserInfo.getOrganization().getDescription())
				.userId(extendedUserInfo.getOrganization().getUserId())
				.currentUserId(extendedUserInfo.getOrganization().getCurrentUserId())
				.role(extendedUserInfo.getOrganization().getRole())
				.build();
	}

	/**
	 * Обновление access токена с помощью refresh токена
	 *
	 * @param request запрос с refresh токеном
	 * @return новая пара токенов
	 */
	@Transactional(readOnly = true)
	public AuthResponse refreshToken(RefreshTokenRequest request) {
		String refreshToken = request.getRefreshToken();

		// Проверяем валидность refresh токена
		if (!jwtService.isTokenValid(refreshToken, true)) {
			throw new InvalidCredentialsException("Invalid or expired refresh token");
		}

		// Извлекаем userId из refresh токена
		UUID userId = jwtService.extractUserId(refreshToken, true);

		// Находим пользователя
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		log.info("Refreshing tokens for userId: {}", userId);

		// Получаем расширенную информацию о пользователе напрямую по userId
		// Не используем токен, чтобы избежать проблем с Spring Security фильтрами
		ExtendedUserInfoDto extendedUserInfo = userServiceClient.getExtendedUserInfo(userId);

		// Генерируем новые токены с расширенными данными
		String newAccessToken = jwtService.generateExtendedAccessToken(
				userId,
				user.getRole(),
				extendedUserInfo.getProfileId(),
				extendedUserInfo.getOrganizationId()
		);
		String newRefreshToken = jwtService.generateRefreshToken(userId);

		// Создаем информацию об организации для ответа
		AuthResponse.OrganizationInfo organizationInfo = null;
		if (extendedUserInfo.getOrganization() != null) {
			organizationInfo = mapOrganizationInfo(extendedUserInfo);
		}

		return AuthResponse.builder()
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken)
				.userId(extendedUserInfo.getUserId() != null ? extendedUserInfo.getUserId().toString() : null)
				.profileId(extendedUserInfo.getProfileId() != null ? extendedUserInfo.getProfileId().toString() : null)
				.isEmailVerified(extendedUserInfo.getIsEmailVerified() != null && extendedUserInfo.getIsEmailVerified())
				.needsOrganizationSetup(extendedUserInfo.getProfileId() == null) // Если profileId null, значит нужно создать организацию
				.firstName(extendedUserInfo.getFirstName())
				.lastName(extendedUserInfo.getLastName())
				.email(extendedUserInfo.getEmail())
				.username(extendedUserInfo.getUsername())
				.organization(organizationInfo)
				.build();
	}

	/**
	 * Валидация access токена
	 *
	 * @param token access токен
	 * @return информация о пользователе если токен валиден
	 */
	@Transactional(readOnly = true)
	public UserInfoDto validateToken(String token) {
		if (!jwtService.isTokenValid(token, false)) {
			throw new InvalidCredentialsException("Invalid or expired access token");
		}

		UUID userId = jwtService.extractUserId(token, false);
		Role role = jwtService.extractRole(token);

		// Проверяем существование пользователя
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		// Получаем полную информацию о пользователе из User Service
		UserInfoDto userInfo = userServiceClient.getUserInfo(userId);

		return userInfo;
	}

	/**
	 * Изменение пароля пользователя
	 *
	 * @param userId      ID пользователя
	 * @param oldPassword старый пароль
	 * @param newPassword новый пароль
	 */
	@Transactional
	public void changePassword(UUID userId, String oldPassword, String newPassword) {
		log.info("Attempting to change password for userId: {}", userId);

		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
			throw new InvalidCredentialsException("Old password is incorrect");
		}

		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		log.info("Password changed successfully for userId: {}", userId);
	}

	/**
	 * Обновление роли пользователя
	 *
	 * @param userId  ID пользователя
	 * @param newRole новая роль
	 */
	@Transactional
	public void updateRole(UUID userId, Role newRole) {
		log.info("Attempting to update role for userId: {} to {}", userId, newRole);

		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		user.setRole(newRole);
		userRepository.save(user);

		log.info("Role updated successfully for userId: {}", userId);
	}

	/**
	 * Обновление профиля пользователя (вызывается из OrganizationService)
	 * Сохраняет latestProfileId и latestOrganizationId для включения в JWT
	 *
	 * @param userId ID пользователя
	 * @param profileId ID профиля в организации
	 * @param organizationId ID организации
	 */
	@Transactional
	public void updateUserProfile(UUID userId, UUID profileId, UUID organizationId) {
		log.info("Updating user profile for userId: {} with profileId: {} and organizationId: {}",
				userId, profileId, organizationId);

		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		user.setLatestProfileId(profileId);
		user.setLatestOrganizationId(organizationId);
		userRepository.save(user);

		log.info("User profile updated successfully for userId: {} with profileId: {}", userId, profileId);
	}

	/**
	 * Удаление учетных данных пользователя
	 *
	 * @param userId ID пользователя
	 */
	@Transactional
	public void deleteUser(UUID userId) {
		log.info("Attempting to delete user credentials for userId: {}", userId);

		if (!userRepository.existsByUserId(userId)) {
			throw new UserNotFoundException(userId);
		}

//		userRepository.deleteByUserId(userId);
		log.info("User credentials deleted successfully for userId: {}", userId);
	}

}

