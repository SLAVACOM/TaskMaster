package com.slavacom.user_service.service;

import com.slavacom.user_service.client.OrganizationServiceClient;
import com.slavacom.user_service.dto.ExtendedUserInfoDto;
import com.slavacom.user_service.dto.OrganizationInfoDto;
import com.slavacom.user_service.dto.RegisterUserDto;
import com.slavacom.user_service.entity.User;
import com.slavacom.user_service.exception.UserAlreadyExistBy;
import com.slavacom.user_service.exception.UserNotFoundException;
import com.slavacom.user_service.repository.UserRepository;
import com.slavacom.user_service.repository.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final OrganizationServiceClient organizationServiceClient;

	@Override
	public boolean userExists(UUID userId) {
		boolean exists = userRepository.existsById(userId);
		log.info("User existence check for userId {}: {}", userId, exists);
		return exists;
	}


	@Override
	@Transactional
	public User registerUser(RegisterUserDto userDto) {
		log.info("Registering new user with email: {} and username: {}", userDto.email(), userDto.username());

		// Проверяем уникальность в рамках транзакции
		if (userRepository.existsByUsername(userDto.username())) {
			log.warn("User with username {} already exists. Cannot register user.", userDto.username());
			throw new UserAlreadyExistBy("username", userDto.username());
		}

		if (userRepository.existsByEmail(userDto.email())) {
			log.warn("User with email {} already exists. Cannot register user.", userDto.email());
			throw new UserAlreadyExistBy("email", userDto.email());
		}

		try {
			User user = User.builder()
					.username(userDto.username())
					.email(userDto.email())
					.firstName(userDto.firstName())
					.lastName(userDto.lastName())
					.build();

			var saved = userRepository.save(user);
			log.info("User registered successfully with id: {}", saved.getId());

			return saved;
		} catch (Exception e) {
			log.error("Error during user registration with email: {} and username: {}", userDto.email(), userDto.username(), e);

			if (e.getMessage().contains("unique constraint") || e.getMessage().contains("duplicate key")) {
				if (userRepository.existsByUsername(userDto.username())) {
					throw new UserAlreadyExistBy("username", userDto.username());
				}
				if (userRepository.existsByEmail(userDto.email())) {
					throw new UserAlreadyExistBy("email", userDto.email());
				}
			}
			throw e;
		}
	}

	@Override
	public User getUserById(UUID id) {
		return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
	}

	@Override
	public User getUserByUsername(String username) {
		return userRepository
				.findByUsername(username)
				.orElseThrow(UserNotFoundException::new);	}



	@Override
	public User getUserByEmail(String email) {

		return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
	}

	@Override
	public UUID getUserIdByEmail(String email) {

		var user = getUserByEmail(email);

		return user.getId();
	}

	@Override
	public UUID getUserIdByLogin(String login) {

		var user = getUserByUsername(login);

		return user.getId();
	}

	@Override
	public ExtendedUserInfoDto getExtendedUserInfo(UUID userId) {
		log.info("Getting extended user info for userId: {}", userId);

		User user = getUserById(userId);

		// Получаем информацию об организации из Organization Service
		OrganizationInfoDto organizationInfo = organizationServiceClient.getUserOrganizationInfo(userId);

		// Используем lastProfileId из пользователя
		UUID profileId = user.getLastProfileId();
		UUID organizationId = null;

		if (organizationInfo != null) {
			try {
				organizationId = UUID.fromString(organizationInfo.id());
			} catch (IllegalArgumentException e) {
				log.warn("Invalid organization ID format: {}", organizationInfo.id());
			}
		}

		return new ExtendedUserInfoDto(
			user.getId(),
			profileId,
			organizationId,
			user.getFirstName(),
			user.getLastName(),
			user.getEmail(),
			user.getUsername(),
			user.isActive(), // Используем поле active из User entity
			user.isVerifiedEmail(), // Используем поле isVerifiedEmail из сущности User
			organizationInfo
		);
	}

	@Override
	public void updateUserLastProfile(UUID userId, UUID profileId) {
		log.info("Updating last profile for userId: {} to profileId: {}", userId, profileId);

		User user = getUserById(userId);
		user.setLastProfileId(profileId);
		userRepository.save(user);

		log.info("Last profile updated successfully for userId: {}", userId);
	}


}
