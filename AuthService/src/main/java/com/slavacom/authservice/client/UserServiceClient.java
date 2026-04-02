package com.slavacom.authservice.client;

import com.slavacom.authservice.dto.*;
import com.slavacom.authservice.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * Клиент для взаимодействия с User Service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

	@Value("${user-service.url:http://localhost:8082}")
	private String userServiceUrl;

	private final RestClient restClient;

	/**
	 * Проверить возможность регистрации пользователя
	 *
	 * @param email    email пользователя
	 * @param username username пользователя
	 * @return ответ о возможности регистрации
	 */
	public CanRegisterResponse canRegister(String email, String username) {
		try {
			log.info("Checking if user can register with email: {} and username: {}", email, username);

			CanRegisterRequest request = CanRegisterRequest.builder()
					.email(email)
					.username(username)
					.build();

			CanRegisterResponse response = restClient.post()
					.uri(userServiceUrl + "/api/users/can-register")
					.body(request)
					.retrieve()
					.body(CanRegisterResponse.class);

			if (response == null) {
				response = new CanRegisterResponse(false, "Unable to check registration availability");
			}

			log.info("Registration check result for email {}, username {}: canRegister={}, reason={}",
					email, username, response.canRegister(), response.reason());

			return response;
		} catch (RestClientException e) {
			log.error("Error checking registration availability for email: {} and username: {}", email, username, e);
			return new CanRegisterResponse(false, "Unable to check registration availability");
		}
	}

	/**
	 * Получить информацию о пользователе из User Service
	 *
	 * @param userId ID пользователя
	 * @return информация о пользователе
	 * @throws UserNotFoundException если пользователь не найден в User Service
	 */
	public UserInfoDto getUserInfo(UUID userId) {
		try {
			log.info("Fetching user info from User Service for userId: {}", userId);

			UserInfoDto userInfo = restClient.get()
					.uri(userServiceUrl + "/api/users/{userId}", userId)
					.retrieve()
					.body(UserInfoDto.class);

			if (userInfo == null) {
				throw new UserNotFoundException("User not found in User Service: " + userId);
			}

			return userInfo;
		} catch (RestClientException e) {
			log.error("Error fetching user info from User Service for userId: {}", userId, e);
			throw new UserNotFoundException("Unable to fetch user from User Service: " + userId);
		}
	}

	/**
	 * Проверить существует ли пользователь в User Service
	 *
	 * @param userId ID пользователя
	 * @return true если пользователь существует
	 */
	public boolean userExists(UUID userId) {
		try {
			log.info("Checking if user exists in User Service for userId: {}", userId);

			Boolean exists = restClient.get()
					.uri(userServiceUrl + "/api/users/{userId}/exists", userId)
					.retrieve()
					.body(Boolean.class);

			return exists != null && exists;
		} catch (RestClientException e) {
			log.error("Error checking user existence in User Service for userId: {}", userId, e);
			return false;
		}
	}

	/**
	 * Зарегистрировать пользователя в User Service
	 * Отправляет запрос на регистрацию и получает созданного пользователя с ID
	 *
	 * @param email     email пользователя
	 * @param username  username пользователя
	 * @param firstName имя пользователя
	 * @param lastName  фамилия пользователя
	 * @return информация о созданном пользователе или null если регистрация не удалась
	 */
	public UserInfoDto registerUser(String email, String username, String firstName, String lastName) {
		try {
			log.info("Registering user in User Service with email: {}, username: {}", email, username);

			RegisterUserRequest request = RegisterUserRequest.builder()
					.email(email)
					.username(username)
					.firstName(firstName)
					.lastName(lastName)
					.build();

			UserInfoDto result = restClient.post()
					.uri(userServiceUrl + "/api/users/register")
					.body(request)
					.retrieve()
					.body(UserInfoDto.class);

			if (result != null) {
				log.info("User registration successful. User ID: {}", result.getId());
			} else {
				log.error("User registration failed - no user data returned");
			}

			return result;
		} catch (RestClientException e) {
			log.error("Error registering user in User Service for email: {}, username: {}", email, username, e);
			return null;
		}
	}

	/**
	 * Создать пользователя в User Service с заданным ID
	 *
	 * @param userId    ID пользователя
	 * @param email     email пользователя
	 * @param username  username пользователя
	 * @param firstName имя пользователя
	 * @param lastName  фамилия пользователя
	 * @return true если пользователь создан успешно
	 */
	public boolean createUser(UUID userId, String email, String username, String firstName, String lastName) {
		try {
			log.info("Creating user in User Service with userId: {}, email: {}, username: {}", userId, email, username);

			CreateUserRequest request = CreateUserRequest.builder()
					.id(userId)
					.email(email)
					.username(username)
					.firstName(firstName)
					.lastName(lastName)
					.build();

			UserInfoDto result = restClient.post()
					.uri(userServiceUrl + "/api/users")
					.body(request)
					.retrieve()
					.body(UserInfoDto.class);

			boolean success = result != null;
			log.info("User creation result for userId {}: {}", userId, success);
			return success;
		} catch (RestClientException e) {
			log.error("Error creating user in User Service for userId: {}", userId, e);
			return false;
		}
	}

	/**
	 * Получить ID пользователя по логину (username)
	 *
	 * @param login логин пользователя
	 * @return UUID пользователя или null если не найден
	 */
	public UUID getUserIdByLogin(String login) {
		try {
			log.info("Getting user ID by login: {}", login);

			UUID userId = restClient.post()
					.uri(userServiceUrl + "/api/users/findUser/login/{login}", login)
					.retrieve()
					.body(UUID.class);

			log.info("User ID found for login {}: {}", login, userId);
			return userId;
		} catch (RestClientException e) {
			log.warn("User not found for login: {}", login);
			return null;
		}
	}

	/**
	 * Получить ID пользователя по email
	 *
	 * @param email email пользователя
	 * @return UUID пользователя или null если не найден
	 */
	public UUID getUserIdByEmail(String email) {
		try {
			log.info("Getting user ID by email: {}", email);

			UUID userId = restClient.post()
					.uri(userServiceUrl + "/api/users/findUser/email/{email}", email)
					.retrieve()
					.body(UUID.class);

			log.info("User ID found for email {}: {}", email, userId);
			return userId;
		} catch (RestClientException e) {
			log.warn("User not found for email: {}", email);
			return null;
		}
	}

	/**
	 * Получить расширенную информацию о пользователе
	 * Включает userId, profileId, organizationId и другие данные для токена
	 *
	 * @param userId ID пользователя
	 * @return расширенная информация о пользователе
	 */
	public ExtendedUserInfoDto getExtendedUserInfo(UUID userId) {
		try {
			log.info("Getting extended user info for userId: {}", userId);

			ExtendedUserInfoDto extendedInfo = restClient.get()
					.uri(userServiceUrl + "/api/users/{userId}/extended", userId)
					.retrieve()
					.body(ExtendedUserInfoDto.class);

			if (extendedInfo == null) {
				throw new UserNotFoundException("Extended user info not found for userId: " + userId);
			}

			return extendedInfo;
		} catch (RestClientException e) {
			log.error("Error fetching extended user info from User Service for userId: {}", userId, e);
			throw new UserNotFoundException("Unable to fetch extended user info: " + userId);
		}
	}

	/**
	 * Получить расширенную информацию о пользователе используя JWT токен
	 * Токен передается в заголовке Authorization
	 *
	 * @param jwtToken JWT токен пользователя
	 * @return расширенная информация о пользователе
	 */
	public ExtendedUserInfoDto getExtendedUserInfoFromToken(String jwtToken) {
		try {
			log.info("Getting extended user info using JWT token");

			ExtendedUserInfoDto extendedInfo = restClient.get()
					.uri(userServiceUrl + "/api/users/me/extended")
					.header("Authorization", jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken)
					.retrieve()
					.body(ExtendedUserInfoDto.class);

			if (extendedInfo == null) {
				throw new UserNotFoundException("Extended user info not found from JWT token");
			}

			return extendedInfo;
		} catch (RestClientException e) {
			log.error("Error fetching extended user info using JWT token", e);
			throw new UserNotFoundException("Unable to fetch extended user info from JWT token");
		}
	}
}

