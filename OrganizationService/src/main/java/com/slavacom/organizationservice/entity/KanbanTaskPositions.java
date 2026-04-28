package com.slavacom.organizationservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "kanban_task_positions")
public class KanbanTaskPositions {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "kanban_column_id", nullable = false)
	private UUID kanbanColumnId;

	@Column(name = "task_id", nullable = false)
	private UUID taskId; // ID задачи из Task Service

	@Column(name = "order_index", nullable = false)
	private Integer orderIndex; // порядок задач в колонке

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
}
