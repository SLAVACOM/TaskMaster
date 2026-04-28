package com.slavacom.organizationservice.project.history

import com.slavacom.organizationservice.entity.ProjectHistory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectHistoryService(
    private val repository: ProjectHistoryRepository,
    private val mapper: ProjectHistoryMapper
) {

    fun list(projectId: UUID): List<ProjectHistoryResponse> =
        repository.findAllByProjectIdOrderByChangedAtDesc(projectId).map { mapper.toResponse(it) }

    fun record(projectId: UUID, action: String, changedBy: UUID?, changes: String? = null) {
        val entry = ProjectHistory()
        entry.projectId = projectId
        entry.action = action
        entry.changedBy = changedBy
        entry.changes = changes
        repository.save(entry)
    }
}
