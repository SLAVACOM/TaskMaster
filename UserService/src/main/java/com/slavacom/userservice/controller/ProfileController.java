package com.slavacom.userservice.controller;

import com.slavacom.userservice.dto.CreateProfileRequest;
import com.slavacom.userservice.dto.ProfileResponse;
import com.slavacom.userservice.dto.ProfileDetailDto;
import com.slavacom.userservice.dto.UpdateProfileRequest;
import com.slavacom.userservice.mapper.UserMapper;
import com.slavacom.userservice.repository.ProfileRepository;
import com.slavacom.userservice.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;import jakarta.validation.Valid;
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
    private final ProfileRepository profileRepository;
    private final UserMapper userMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse createProfile(@Valid @RequestBody CreateProfileRequest request) {
        log.info("REST: Creating profile for user: {} in organization: {}",
                 request.getUserId(), request.getOrganizationId());
        return profileService.createProfile(request);
    }

    @GetMapping
    public List<ProfileResponse> getAllProfiles(@RequestHeader("X-User-Id") UUID userId) {
		log.info("REST: Getting all profiles for current user: {}", userId);
        return profileService.getAllProfiles(userId);
    }

    @GetMapping("/user/{userId}")
    public List<ProfileResponse> getUserProfiles(@PathVariable UUID userId) {
        log.info("REST: Getting all profiles for user: {}", userId);
        return profileService.getUserProfiles(userId);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ProfileResponse> getActiveProfile(@PathVariable UUID userId) {
        log.info("REST: Getting active profile for userId: {}", userId);
        try {
            return ResponseEntity.ok(profileService.getActiveProfile(userId));
        } catch (Exception e) {
            log.warn("Active profile not found for userId: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody UpdateProfileRequest request) {
        log.info("REST: Updating profile {} for current user {}", profileId, userId);
        return ResponseEntity.ok(profileService.updateProfile(userId, profileId, request));
    }

    @PutMapping("/{profileId}/activate")
    public ResponseEntity<Void> activateProfile(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("REST: Activating profile {} for current user {}", profileId, userId);
        profileService.activateProfile(userId, profileId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получение деталей профиля по ID
     * Используется для фронтенда при отображении информации о профиле
     */
    @GetMapping("/{profileId}/detail")
    public ResponseEntity<ProfileDetailDto> getProfileDetail(@PathVariable UUID profileId) {
        log.info("REST: Getting profile detail for profileId: {}", profileId);

        try {
            var profile = profileRepository.findById(profileId)
                    .orElseThrow(() -> new RuntimeException("Profile not found: " + profileId));
            ProfileDetailDto detailDto = userMapper.toProfileDetailDto(profile);
            log.info("Profile detail found for profileId: {}", profileId);
            return ResponseEntity.ok(detailDto);
        } catch (Exception e) {
            log.warn("Profile not found for profileId: {}", profileId);
            return ResponseEntity.notFound().build();
        }
    }

}
