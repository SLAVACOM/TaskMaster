package com.slavacom.userservice.repository;

import com.slavacom.userservice.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findUserById(@NotNull UUID id);

	Optional<User> findByUsername(@NotBlank String username);

	Optional<User> findByEmail(@NotBlank String email);

	boolean existsByUsername(@NotBlank String username);

	boolean existsByEmail(@NotBlank String email);

}
