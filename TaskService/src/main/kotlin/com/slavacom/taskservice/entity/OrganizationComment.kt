package com.slavacom.taskservice.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "organization_comment")
@DiscriminatorValue("ORGANIZATION")
class OrganizationComment(
	@Column(name = "organization_id", nullable = false)
	var organizationId: UUID? = null,
) : Comment()
