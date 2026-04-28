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
@Table(name = "project_task_statuses")
public class ProjectTaskStatuses {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "name", nullable = false)
	private String name; // TODO, IN_PROGRESS, TESTING, DONE, etc.

	@Column(name = "color")
	private String color; // HEX код цвета статуса

	@Column(name = "order_index", nullable = false)
	private Integer orderIndex; // порядок отображения

	@Column(name = "is_initial", nullable = false)
	private boolean isInitial = false; // начальный статус

	@Column(name = "is_final", nullable = false)
	private boolean isFinal = false; // финальный статус

	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
}
