package com.slavacom.organizationservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "project_sprints")
public class ProjectSprints {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "start_date")
	private Instant startDate;

	@Column(name = "end_date")
	private Instant endDate;

	@Column(name = "status", nullable = false)
	private String status = "PLANNED"; // PLANNED, ACTIVE, COMPLETED, CANCELLED

	@Column(name = "is_active", nullable = false)
	private boolean isActive = false; // активный спринт (только один может быть активным)

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
}
