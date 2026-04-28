package com.slavacom.organizationservice.project.kanban

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/projects/{projectId}/kanban")
class KanbanColumnsController(private val service: KanbanColumnsService) {

    @GetMapping("/columns")
    fun listColumns(@PathVariable projectId: UUID): List<KanbanColumnResponse> =
        service.listColumns(projectId)

    @PostMapping("/columns")
    @ResponseStatus(HttpStatus.CREATED)
    fun createColumn(
        @PathVariable projectId: UUID,
        @RequestBody request: CreateKanbanColumnRequest
    ): KanbanColumnResponse = service.createColumn(projectId, request)

    @PutMapping("/columns/{columnId}")
    fun updateColumn(
        @PathVariable projectId: UUID,
        @PathVariable columnId: UUID,
        @RequestBody request: UpdateKanbanColumnRequest
    ): KanbanColumnResponse = service.updateColumn(projectId, columnId, request)

    @PutMapping("/columns/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reorderColumns(
        @PathVariable projectId: UUID,
        @RequestBody request: ReorderColumnsRequest
    ) = service.reorderColumns(projectId, request)

    @DeleteMapping("/columns/{columnId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deactivateColumn(@PathVariable projectId: UUID, @PathVariable columnId: UUID) =
        service.deactivateColumn(projectId, columnId)

    @GetMapping("/positions")
    fun getPositions(@PathVariable projectId: UUID): List<TaskPositionResponse> =
        service.getPositions(projectId)

    @PutMapping("/positions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun bulkUpdatePositions(
        @PathVariable projectId: UUID,
        @RequestBody request: BulkUpdatePositionsRequest
    ) = service.bulkUpdatePositions(projectId, request)
}
