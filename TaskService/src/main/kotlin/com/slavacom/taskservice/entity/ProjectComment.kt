package com.slavacom.taskservice.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "project_comment")
@DiscriminatorValue("PROJECT")
class ProjectComment(
	@Column(name = "project_id", nullable = false)
	var projectId: UUID? = null,
) : Comment()
