package com.slavacom.userservice.service;

import com.slavacom.userservice.client.OrganizationServiceClient;
import com.slavacom.userservice.dto.CreateProfileRequest;
import com.slavacom.userservice.dto.OrganizationInfoDto;
import com.slavacom.userservice.dto.ProfileResponse;
import com.slavacom.userservice.entity.Profile;
import com.slavacom.userservice.exception.OrganizationNotFoundException;
import com.slavacom.userservice.exception.ServiceUnavailableException;
import com.slavacom.userservice.repository.ProfileRepository;
import com.slavacom.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationServiceClient organizationServiceClient;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId;
    private UUID organizationId;
    private CreateProfileRequest createProfileRequest;
    private OrganizationInfoDto organizationInfoDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        organizationId = UUID.randomUUID();
        createProfileRequest = CreateProfileRequest.builder()
                .userId(userId)
                .organizationId(organizationId)
                .name("Test Profile")
                .description("Test Description")
                .build();

        organizationInfoDto = new OrganizationInfoDto(
                organizationId.toString(),
                "Test Org",
                "Test Description",
                userId.toString(),
                userId.toString(),
                "ADMIN"
        );
    }

    @Test
    void testCreateProfile_WithValidOrganization_CreatesProfile() {
        // Setup
        UUID profileId = UUID.randomUUID();
        when(organizationServiceClient.getOrganizationById(organizationId))
                .thenReturn(organizationInfoDto);

        Profile savedProfile = Profile.builder()
                .id(profileId)
                .userId(userId)
                .organizationId(organizationId)
                .name("Test Profile")
                .description("Test Description")
                .isActive(true)
                .build();

        when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);

        // Execute
        ProfileResponse result = profileService.createProfile(createProfileRequest);

        // Assert
        assertNotNull(result);
        assertEquals(profileId, result.id());
        assertEquals(userId, result.userId());
        assertEquals(organizationId, result.organizationId());
        assertEquals("Test Profile", result.name());

        // Verify organization was checked before saving
        verify(organizationServiceClient, times(1)).getOrganizationById(organizationId);
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    void testCreateProfile_WithInvalidOrganization_ThrowsException() {
        // Setup
        when(organizationServiceClient.getOrganizationById(organizationId))
                .thenThrow(new OrganizationNotFoundException(organizationId.toString()));

        // Execute & Assert
        OrganizationNotFoundException exception = assertThrows(
                OrganizationNotFoundException.class,
                () -> profileService.createProfile(createProfileRequest)
        );

        assertTrue(exception.getMessage().contains(organizationId.toString()));
        verify(organizationServiceClient, times(1)).getOrganizationById(organizationId);
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void testCreateProfile_WithServiceUnavailable_PropagatesException() {
        // Setup
        when(organizationServiceClient.getOrganizationById(organizationId))
                .thenThrow(new ServiceUnavailableException("OrganizationService"));

        // Execute & Assert
        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> profileService.createProfile(createProfileRequest)
        );

        assertTrue(exception.getMessage().contains("OrganizationService"));
        verify(organizationServiceClient, times(1)).getOrganizationById(organizationId);
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void testCreateProfile_ProfileNotSavedIfOrganizationCheckFails() {
        // Setup - simulate organization check failure
        when(organizationServiceClient.getOrganizationById(any(UUID.class)))
                .thenThrow(new OrganizationNotFoundException("unknown-id"));

        // Execute & Assert
        assertThrows(OrganizationNotFoundException.class,
                () -> profileService.createProfile(createProfileRequest));

        // Verify profile was never saved
        verify(profileRepository, never()).save(any(Profile.class));
    }
}
