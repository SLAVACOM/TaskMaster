package com.slavacom.user_service.controller;

import com.slavacom.user_service.dto.CreateProfileRequest;
import com.slavacom.user_service.dto.ProfileResponse;
import com.slavacom.user_service.dto.UpdateProfileRequest;
import com.slavacom.user_service.security.JwtTokenProvider;
import com.slavacom.user_service.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Создание нового профиля в организации
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse createProfile(@Valid @RequestBody CreateProfileRequest request) {
        log.info("REST: Creating profile for user: {} in organization: {}",
                 request.getUserId(), request.getOrganizationId());
        return profileService.createProfile(request);
    }

	/**
	 * Получение всех профилей текущего пользователя
	 */
	@GetMapping
	public List<ProfileResponse> getAllProfiles(@RequestHeader("Authorization") String authorizationHeader) {
		UUID userId = jwtTokenProvider.extractUserId(authorizationHeader);
		log.info("REST: Getting all profiles for current user: {}", userId);
		return profileService.getAllProfiles(userId);
	}

    /**
     * Получение всех активных профилей пользователя
     */
    @GetMapping("/user/{userId}")
    public List<ProfileResponse> getUserProfiles(@PathVariable UUID userId) {
        log.info("REST: Getting all profiles for user: {}", userId);
        return profileService.getUserProfiles(userId);
    }

    /**
     * Обновление профиля текущего пользователя (имя и описание)
     */
    @PutMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable UUID profileId,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UpdateProfileRequest request) {
        UUID userId = jwtTokenProvider.extractUserId(authorizationHeader);
        log.info("REST: Updating profile {} for current user {}", profileId, userId);
        ProfileResponse response = profileService.updateProfile(userId, profileId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Переключение текущего профиля пользователя (обновляет lastProfileId)
     */
    @PutMapping("/{profileId}/activate")
    public ResponseEntity<Void> activateProfile(
            @PathVariable UUID profileId,
            @RequestHeader("Authorization") String authorizationHeader) {
        UUID userId = jwtTokenProvider.extractUserId(authorizationHeader);
        log.info("REST: Activating profile {} for current user {}", profileId, userId);
        profileService.activateProfile(userId, profileId);
        return ResponseEntity.noContent().build();
    }

}
