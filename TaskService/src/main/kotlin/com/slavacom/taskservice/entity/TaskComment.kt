package com.slavacom.taskservice.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "task_comment")
@DiscriminatorValue("TASK")
class TaskComment(
	@Column(name = "task_id", nullable = false)
	var taskId: UUID? = null,
) : Comment()
