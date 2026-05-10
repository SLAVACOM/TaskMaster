package com.slavacom.userservice.repository;

import com.slavacom.userservice.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findUserById(@NotNull UUID id);

	Optional<User> findByUsername(@NotBlank String username);

	Optional<User> findByEmail(@NotBlank String email);

	boolean existsByUsername(@NotBlank String username);

	boolean existsByEmail(@NotBlank String email);

	List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(@NotBlank String firstName, @NotBlank String lastName);

	@Query("SELECT u FROM User u ORDER BY u.lastName ASC, u.firstName ASC")
	List<User> findAllOrderedByName();

}
