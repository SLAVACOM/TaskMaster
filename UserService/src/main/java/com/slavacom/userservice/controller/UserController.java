package com.slavacom.userservice.controller;

import com.slavacom.userservice.dto.*;
import com.slavacom.userservice.entity.User;
import com.slavacom.userservice.mapper.UserMapper;
import com.slavacom.userservice.repository.UserRepository;
import com.slavacom.userservice.service.ProfileService;
import com.slavacom.userservice.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final UserServiceImpl userService;
    private final UserMapper userMapper;
    private final ProfileService profileService;


    @PostMapping("/can-register")
    public CanRegisterResponse canRegister(@Valid @RequestBody CanRegisterRequest request) {
        log.info("REST: Checking if user can register with email: {} and username: {}",
                request.email(), request.username());

        boolean emailExists = userRepository.existsByEmail(request.email());
        boolean usernameExists = userRepository.existsByUsername(request.username());

        CanRegisterResponse response;

        if (emailExists) {
            response = new CanRegisterResponse(false, "Email already exists");
            log.warn("Registration check failed: Email {} already exists", request.email());
        } else if (usernameExists) {
            response = new CanRegisterResponse(false, "Username already exists");
            log.warn("Registration check failed: Username {} already exists", request.username());
        } else {
            response = new CanRegisterResponse(true, "");
            log.info("Registration check passed for email: {} and username: {}",
                    request.email(), request.username());
        }

        return response;
    }

    /**
     * Регистрация пользователя с автоматической генерацией ID
     * Возвращает созданного пользователя с ID
     */
    @PostMapping("/register")
    public ResponseEntity<UserInfoDto> registerUser(@Valid @RequestBody RegisterUserDto request) {
        log.info("REST: Registering user with email: {} and username: {}",
                request.email(), request.username());
        log.debug("Register request details - firstName: '{}', lastName: '{}', email: '{}', username: '{}'",
                request.firstName(), request.lastName(), request.email(), request.username());

        // Дополнительная проверка на null значения
        if (request.firstName() == null || request.firstName().isBlank()) {
            log.error("Registration failed: firstName is null or blank");
            return ResponseEntity.badRequest().build();
        }
        if (request.lastName() == null || request.lastName().isBlank()) {
            log.error("Registration failed: lastName is null or blank");
            return ResponseEntity.badRequest().build();
        }
        if (request.email() == null || request.email().isBlank()) {
            log.error("Registration failed: email is null or blank");
            return ResponseEntity.badRequest().build();
        }
        if (request.username() == null || request.username().isBlank()) {
            log.error("Registration failed: username is null or blank");
            return ResponseEntity.badRequest().build();
        }

        try {
            User user = userService.registerUser(request);
            UserInfoDto userInfo = userMapper.toUserInfoDto(user);
            log.info("User registered successfully with userId: {}", user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(userInfo);
        } catch (Exception e) {
            log.error("Error registering user with email: {} and username: {}",
                    request.email(), request.username(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }


    /**
     * Получение информации о пользователе по ID
     * Заменяет gRPC метод GetUserInfo
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDto> getUserInfo(@PathVariable UUID userId) {
        log.info("REST: Getting user info for userId: {}", userId);

        try {
            User user = userService.getUserById(userId);
            UserInfoDto userInfo = userMapper.toUserInfoDto(user);

            log.info("User info found for userId: {}", userId);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.warn("User not found for userId: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Проверка существования пользователя
     * Используется UserServiceClient из Auth-service
     */
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable UUID userId) {
        log.info("REST: Checking if user exists with userId: {}", userId);

        boolean exists = userService.userExists(userId);
        log.info("User existence check for userId {}: {}", userId, exists);

        return ResponseEntity.ok(exists);
    }


	@PostMapping("/findUser/login/{login}")
	public ResponseEntity<UUID> findUserByLogin(@PathVariable String login) {
		log.info("REST: Finding user by login: {}", login);

		try {
			User user = userService.getUserByUsername(login);
			log.info("User found for login: {}", login);
			return ResponseEntity.ok(user.getId());
		} catch (Exception e) {
			log.warn("User not found for login: {}", login);
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/findUser/email/{email}")
	public ResponseEntity<UUID> findUserByEmail(@PathVariable String email) {
		log.info("REST: Finding user by email: {}", email);

		try {
			User user = userService.getUserByEmail(email);
			log.info("User found for email: {}", email);
			return ResponseEntity.ok(user.getId());
		} catch (Exception e) {
			log.warn("User not found for email: {}", email);
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Получение расширенной информации о пользователе для токена
	 * Включает userId, последний profileId, organizationId
	 */
	@GetMapping("/{userId}/extended")
	public ResponseEntity<ExtendedUserInfoDto> getExtendedUserInfo(@PathVariable UUID userId) {
		log.info("REST: Getting extended user info for userId: {}", userId);

		try {
			// TODO: Реализовать получение расширенной информации с профилем и организацией
			ExtendedUserInfoDto extendedInfo = userService.getExtendedUserInfo(userId);
			log.info("Extended user info found for userId: {}", userId);
			return ResponseEntity.ok(extendedInfo);
		} catch (Exception e) {
			log.warn("Extended user info not found for userId: {}", userId);
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Обновление последнего профиля пользователя
	 */
	@PutMapping("/{userId}/last-profile/{profileId}")
	public ResponseEntity<Void> updateUserLastProfile(@PathVariable UUID userId, @PathVariable UUID profileId) {
		log.info("REST: Updating last profile for userId: {} to profileId: {}", userId, profileId);

		try {
			userService.updateUserLastProfile(userId, profileId);
			log.info("Last profile updated successfully for userId: {}", userId);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error("Error updating last profile for userId: {}", userId, e);
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Получение расширенной информации о текущем пользователе.
	 * X-User-Id проставляется Gateway после валидации JWT.
	 */
	@GetMapping("/me/extended")
	public ResponseEntity<ExtendedUserInfoDto> getCurrentUserExtendedInfo(
			@RequestHeader("X-User-Id") UUID userId) {

		try {
			log.info("REST: Getting extended user info for current user: {}", userId);
			ExtendedUserInfoDto extendedInfo = userService.getExtendedUserInfo(userId);
			log.info("Extended user info found for current user: {}", userId);
			return ResponseEntity.ok(extendedInfo);
		} catch (Exception e) {
			log.warn("Error getting extended user info for userId: {}", userId, e);
			return ResponseEntity.badRequest().build();
		}
	}
}
