package com.slavacom.taskservice.entity

import com.slavacom.taskservice.entity.converter.FieldChangeListConverter
import com.slavacom.taskservice.entity.enums.HistoryAction
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
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "task_history")
class TaskHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    @Column(name = "task_id", nullable = false)
    var taskId: UUID,

    @Column(name = "changed_by", nullable = false)
    var changedBy: UUID,

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    var changedAt: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    var action: HistoryAction,

    @Convert(converter = FieldChangeListConverter::class)
    @Column(name = "changes", columnDefinition = "TEXT")
    var changes: List<FieldChange> = emptyList(),
)

