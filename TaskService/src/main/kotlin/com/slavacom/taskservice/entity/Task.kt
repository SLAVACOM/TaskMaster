package com.slavacom.taskservice.entity

import com.slavacom.taskservice.entity.converter.StringListJsonConverter
import com.slavacom.taskservice.entity.converter.UuidListJsonConverter
import com.slavacom.taskservice.entity.enums.TaskPriority
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tasks")
class Task(
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	var id: UUID? = null,

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	var createdAt: Instant? = null,

	@UpdateTimestamp
	@Column(name = "updated_at")
	var updatedAt: Instant? = null,

	@Column(name = "is_active", nullable = false)
	var isActive: Boolean = true,

	@Column(name = "name", nullable = false)
	var name: String = "",

	@Column(name = "description", columnDefinition = "TEXT")
	var description: String? = null,

	@Convert(converter = StringListJsonConverter::class)
	@Column(name = "files", nullable = false, columnDefinition = "TEXT")
	var files: List<String> = emptyList(),

	@Convert(converter = UuidListJsonConverter::class)
	@Column(name = "depends", nullable = false, columnDefinition = "TEXT")
	var depends: List<UUID> = emptyList(),

	@Column(name = "status", nullable = false)
	var status: String = "TODO",

	@Column(name = "responsible")
	var responsible: UUID? = null,

	@Column(name = "executor")
	var executor: UUID? = null,

	@Convert(converter = UuidListJsonConverter::class)
	@Column(name = "observers", nullable = false, columnDefinition = "TEXT")
	var observers: List<UUID> = emptyList(),

	@Convert(converter = UuidListJsonConverter::class)
	@Column(name = "watchers", nullable = false, columnDefinition = "TEXT")
	var watchers: List<UUID> = emptyList(),

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false)
	var priority: TaskPriority = TaskPriority.MEDIUM,

	@Convert(converter = StringListJsonConverter::class)
	@Column(name = "tags", nullable = false, columnDefinition = "TEXT")
	var tags: List<String> = emptyList(),

	@Column(name = "start_at")
	var start: Instant? = null,

	@Column(name = "end_at")
	var end: Instant? = null,

	@Column(name = "deadline_at")
	var deadline: Instant? = null,

	@Column(name = "sprint_id")
	var sprintId: UUID? = null,

	@Column(name = "project_id")
	var projectId: UUID? = null,

	@Column(name = "story_point")
	var storyPoint: Int? = null,
)
