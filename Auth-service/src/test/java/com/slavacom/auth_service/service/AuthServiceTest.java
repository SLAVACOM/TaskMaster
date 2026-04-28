package com.slavacom.auth_service.service;

import com.slavacom.auth_service.client.UserServiceClient;
import com.slavacom.auth_service.dto.*;
import com.slavacom.auth_service.entity.User;
import com.slavacom.auth_service.enums.Role;
import com.slavacom.auth_service.exception.InvalidCredentialsException;
import com.slavacom.auth_service.exception.UserAlreadyExistsException;
import com.slavacom.auth_service.exception.UserNotFoundException;
import com.slavacom.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private UserServiceClient userServiceClient;

	@InjectMocks
	private AuthService authService;

	private UUID testUserId;
	private RegisterRequest registerRequest;
	private LoginRequest loginRequest;
	private User testUser;
	private UserInfoDto userInfoDto;

//	@BeforeEach
//	void setUp() {
//		testUserId = UUID.randomUUID();
//
//		registerRequest = RegisterRequest.builder()
//				.userId(testUserId)
//				.password("password123")
//				.role(Role.USER)
//				.build();
//
//		loginRequest = LoginRequest.builder()
//				.userId(testUserId)
//				.password("password123")
//				.build();
//
//		testUser = User.builder()
//				.id(UUID.randomUUID())
//				.userId(testUserId)
//				.passwordHash("hashedPassword")
//				.role(Role.USER)
//				.build();
//
//		userInfoDto = UserInfoDto.builder()
//				.id(testUserId)
//				.username("testuser")
//				.email("test@example.com")
//				.active(true)
//				.build();
//	}

//	@Test
//	void register_Success() {
//		// Arrange
//		when(userRepository.existsByUserId(testUserId)).thenReturn(false);
//		when(userServiceClient.userExists(testUserId)).thenReturn(true);
//		when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
//		when(userRepository.save(any(User.class))).thenReturn(testUser);
//		when(jwtService.generateAccessToken(any(UUID.class), any(Role.class))).thenReturn("accessToken");
//		when(jwtService.generateRefreshToken(any(UUID.class))).thenReturn("refreshToken");
//
//		// Act
//		AuthResponse response = authService.register(registerRequest);
//
//		// Assert
//		assertNotNull(response);
//		assertEquals("accessToken", response.getAccessToken());
//		assertEquals("refreshToken", response.getRefreshToken());
//		assertEquals(testUserId, response.getUserId());
//		assertEquals(Role.USER, response.getRole());
//
//		verify(userRepository).existsByUserId(testUserId);
//		verify(userServiceClient).userExists(testUserId);
//		verify(userRepository).save(any(User.class));
//	}
//
//	@Test
//	void register_UserAlreadyExists_ThrowsException() {
//		// Arrange
//		when(userRepository.existsByUserId(testUserId)).thenReturn(true);
//
//		// Act & Assert
//		assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
//		verify(userRepository).existsByUserId(testUserId);
//		verify(userServiceClient, never()).userExists(any());
//		verify(userRepository, never()).save(any());
//	}
//
//	@Test
//	void register_UserNotFoundInUserService_ThrowsException() {
//		// Arrange
//		when(userRepository.existsByUserId(testUserId)).thenReturn(false);
//		when(userServiceClient.userExists(testUserId)).thenReturn(false);
//
//		// Act & Assert
//		assertThrows(UserNotFoundException.class, () -> authService.register(registerRequest));
//		verify(userRepository).existsByUserId(testUserId);
//		verify(userServiceClient).userExists(testUserId);
//		verify(userRepository, never()).save(any());
//	}
//
//	@Test
//	void login_Success() {
//		// Arrange
//		when(userRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUser));
//		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
//		when(userServiceClient.getUserInfo(testUserId)).thenReturn(userInfoDto);
//		when(jwtService.generateAccessToken(any(UUID.class), any(Role.class))).thenReturn("accessToken");
//		when(jwtService.generateRefreshToken(any(UUID.class))).thenReturn("refreshToken");
//
//		// Act
//		AuthResponse response = authService.login(loginRequest);
//
//		// Assert
//		assertNotNull(response);
//		assertEquals("accessToken", response.getAccessToken());
//		assertEquals("refreshToken", response.getRefreshToken());
//		assertEquals(testUserId, response.getUserId());
//
//		verify(userRepository).findByUserId(testUserId);
//		verify(passwordEncoder).matches(anyString(), anyString());
//		verify(userServiceClient).getUserInfo(testUserId);
//	}
//
//	@Test
//	void login_InvalidPassword_ThrowsException() {
//		// Arrange
//		when(userRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUser));
//		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
//
//		// Act & Assert
//		assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
//		verify(userRepository).findByUserId(testUserId);
//		verify(passwordEncoder).matches(anyString(), anyString());
//		verify(userServiceClient, never()).getUserInfo(any());
//	}
//
//	@Test
//	void login_UserNotFound_ThrowsException() {
//		// Arrange
//		when(userRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
//
//		// Act & Assert
//		assertThrows(UserNotFoundException.class, () -> authService.login(loginRequest));
//		verify(userRepository).findByUserId(testUserId);
//		verify(passwordEncoder, never()).matches(anyString(), anyString());
//	}
//
//	@Test
//	void changePassword_Success() {
//		// Arrange
//		String oldPassword = "oldPassword";
//		String newPassword = "newPassword";
//		when(userRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUser));
//		when(passwordEncoder.matches(oldPassword, testUser.getPasswordHash())).thenReturn(true);
//		when(passwordEncoder.encode(newPassword)).thenReturn("newHashedPassword");
//		when(userRepository.save(any(User.class))).thenReturn(testUser);
//
//		// Act
//		authService.changePassword(testUserId, oldPassword, newPassword);
//
//		// Assert
//		verify(userRepository).findByUserId(testUserId);
//		verify(passwordEncoder).matches(oldPassword, testUser.getPasswordHash());
//		verify(passwordEncoder).encode(newPassword);
//		verify(userRepository).save(testUser);
//	}
//
//	@Test
//	void changePassword_InvalidOldPassword_ThrowsException() {
//		// Arrange
//		String oldPassword = "wrongPassword";
//		String newPassword = "newPassword";
//		when(userRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUser));
//		when(passwordEncoder.matches(oldPassword, testUser.getPasswordHash())).thenReturn(false);
//
//		// Act & Assert
//		assertThrows(InvalidCredentialsException.class,
//				() -> authService.changePassword(testUserId, oldPassword, newPassword));
//		verify(userRepository).findByUserId(testUserId);
//		verify(passwordEncoder).matches(oldPassword, testUser.getPasswordHash());
//		verify(userRepository, never()).save(any());
//	}
//
//	@Test
//	void updateRole_Success() {
//		// Arrange
//		Role newRole = Role.ADMIN;
//		when(userRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUser));
//		when(userRepository.save(any(User.class))).thenReturn(testUser);
//
//		// Act
//		authService.updateRole(testUserId, newRole);
//
//		// Assert
//		verify(userRepository).findByUserId(testUserId);
//		verify(userRepository).save(testUser);
//		assertEquals(newRole, testUser.getRole());
//	}
//
//	@Test
//	void deleteUser_Success() {
//		// Arrange
//		when(userRepository.existsByUserId(testUserId)).thenReturn(true);
//		doNothing().when(userRepository).deleteByUserId(testUserId);
//
//		// Act
//		authService.deleteUser(testUserId);
//
//		// Assert
//		verify(userRepository).existsByUserId(testUserId);
//		verify(userRepository).deleteByUserId(testUserId);
//	}
//
//	@Test
//	void deleteUser_UserNotFound_ThrowsException() {
//		// Arrange
//		when(userRepository.existsByUserId(testUserId)).thenReturn(false);
//
//		// Act & Assert
//		assertThrows(UserNotFoundException.class, () -> authService.deleteUser(testUserId));
//		verify(userRepository).existsByUserId(testUserId);
//		verify(userRepository, never()).deleteByUserId(any());
//	}
}

