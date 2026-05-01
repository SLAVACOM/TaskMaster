package com.slavacom.userservice.service;

import com.slavacom.userservice.dto.CreateProfileRequest;
import com.slavacom.userservice.dto.ProfileResponse;
import com.slavacom.userservice.dto.UpdateProfileRequest;
import com.slavacom.userservice.entity.Profile;
import com.slavacom.userservice.entity.User;
import com.slavacom.userservice.repository.ProfileRepository;
import com.slavacom.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProfileResponse createProfile(CreateProfileRequest request) {
        log.info("Creating profile for user: {} in organization: {}", request.getUserId(), request.getOrganizationId());

        Profile profile = Profile.builder()
                .userId(request.getUserId())
                .organizationId(request.getOrganizationId())
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();

        Profile savedProfile = profileRepository.save(profile);
        log.info("Profile created successfully: {} for user: {} in organization: {}",
                 savedProfile.getId(), request.getUserId(), request.getOrganizationId());

        return mapToResponse(savedProfile);
    }

    public List<ProfileResponse> getUserProfiles(UUID userId) {
        return profileRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProfileResponse> getUserProfilesInOrganization(UUID userId, UUID organizationId) {
        return profileRepository.findByUserIdAndOrganizationIdAndIsActiveTrue(userId, organizationId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

	public List<ProfileResponse> getAllProfiles(UUID userId) {
		return profileRepository.findByUserIdAndIsActiveTrue(userId)
				.stream()
				.map(this::mapToResponse)
				.toList();
	}

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UUID profileId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByIdAndUserIdAndIsActiveTrue(profileId, userId)
                .orElseThrow(() -> new RuntimeException("Active profile not found for current user: " + profileId));

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new RuntimeException("Profile name cannot be blank");
            }
            profile.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription().trim());
        }

        if (request.getName() == null && request.getDescription() == null) {
            throw new RuntimeException("Nothing to update. Provide name and/or description");
        }

        Profile saved = profileRepository.save(profile);
        log.info("Profile {} updated for user {}", profileId, userId);
        return mapToResponse(saved);
    }

    @Transactional
    public void activateProfile(UUID userId, UUID profileId) {
        Profile profile = profileRepository.findByIdAndUserIdAndIsActiveTrue(profileId, userId)
                .orElseThrow(() -> new RuntimeException("Active profile not found for current user: " + profileId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setLastProfileId(profile.getId());
        userRepository.save(user);

        log.info("Current profile switched for user {} to profile {}", userId, profileId);
    }

    private ProfileResponse mapToResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getOrganizationId(),
                profile.getName(),
                profile.getDescription(),
                profile.getIsActive(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
