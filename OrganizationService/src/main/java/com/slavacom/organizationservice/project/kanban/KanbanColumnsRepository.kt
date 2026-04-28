package com.slavacom.organizationservice.project.kanban

import com.slavacom.organizationservice.entity.KanbanColumns
import com.slavacom.organizationservice.entity.KanbanTaskPositions
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface KanbanColumnsRepository : JpaRepository<KanbanColumns, UUID> {
    fun findAllByProjectIdAndIsActiveTrueOrderByOrderIndexAsc(projectId: UUID): List<KanbanColumns>
    fun findByIdAndProjectId(id: UUID, projectId: UUID): Optional<KanbanColumns>
}

interface KanbanTaskPositionsRepository : JpaRepository<KanbanTaskPositions, UUID> {
    fun findAllByProjectId(projectId: UUID): List<KanbanTaskPositions>
    fun findByTaskId(taskId: UUID): Optional<KanbanTaskPositions>
}
