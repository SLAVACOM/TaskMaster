package com.slavacom.taskservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "attachment")
class Attachment(
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	var id: UUID? = null,

	@Column(name = "comment_id", nullable = false)
	var commentId: UUID? = null,

	@Column(name = "file_name", nullable = false)
	var fileName: String = "",

	@Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
	var fileUrl: String = "",

	@Column(name = "file_size")
	var fileSize: Long? = null,

	@Column(name = "mime_type")
	var mimeType: String? = null,

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	var createdAt: Instant? = null,
)
