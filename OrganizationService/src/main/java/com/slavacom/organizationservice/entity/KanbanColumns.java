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
@Table(name = "kanban_columns")
public class KanbanColumns {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "name", nullable = false)
	private String name; // название колонки

	@Column(name = "order_index", nullable = false)
	private Integer orderIndex; // порядок отображения колонок

	@Column(name = "color")
	private String color; // HEX код цвета колонки

	@Column(name = "wip_limit")
	private Integer wipLimit; // лимит задач в колонке (Work In Progress limit)

	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
}
