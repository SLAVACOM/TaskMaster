package com.slavacom.userservice.repository;

import com.slavacom.userservice.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    List<Profile> findByUserId(UUID userId);
    List<Profile> findByOrganizationId(UUID organizationId);
    List<Profile> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
    List<Profile> findByUserIdAndIsActiveTrue(UUID userId);
    List<Profile> findByUserIdAndOrganizationIdAndIsActiveTrue(UUID userId, UUID organizationId);
    Optional<Profile> findByIdAndUserIdAndIsActiveTrue(UUID id, UUID userId);
}
