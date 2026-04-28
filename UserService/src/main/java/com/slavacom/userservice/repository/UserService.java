package com.slavacom.user_service.repository;

import com.slavacom.user_service.dto.ExtendedUserInfoDto;
import com.slavacom.user_service.dto.RegisterUserDto;
import com.slavacom.user_service.entity.User;

import java.util.UUID;

public interface UserService {


	User registerUser(RegisterUserDto userDto);

	User getUserById(UUID id);

	User getUserByUsername(String username);
	User getUserByEmail(String email);

	UUID getUserIdByEmail(String email);
	UUID getUserIdByLogin(String login);

	boolean userExists(UUID userId);

	ExtendedUserInfoDto getExtendedUserInfo(UUID userId);

	void updateUserLastProfile(UUID userId, UUID profileId);
}
