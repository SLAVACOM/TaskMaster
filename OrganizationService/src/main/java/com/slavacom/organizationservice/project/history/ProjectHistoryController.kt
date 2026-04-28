package com.slavacom.organizationservice.project.history

import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/projects/{projectId}/history")
class ProjectHistoryController(private val service: ProjectHistoryService) {

    @GetMapping
    fun list(@PathVariable projectId: UUID): List<ProjectHistoryResponse> = service.list(projectId)
}
