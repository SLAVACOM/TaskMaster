package com.slavacom.auth_service.repository;

import com.slavacom.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByUserId(UUID userId);
	boolean existsByUserId(UUID userId);
	void deleteByUserId(UUID userId);
}

