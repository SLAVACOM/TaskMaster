package com.slavacom.organizationservice.project.kanban

import com.slavacom.organizationservice.entity.KanbanTaskPositions
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class KanbanColumnsService(
    private val columnsRepository: KanbanColumnsRepository,
    private val positionsRepository: KanbanTaskPositionsRepository,
    private val mapper: KanbanColumnsMapper
) {

    fun listColumns(projectId: UUID): List<KanbanColumnResponse> =
        columnsRepository.findAllByProjectIdAndIsActiveTrueOrderByOrderIndexAsc(projectId)
            .map { mapper.toResponse(it) }

    fun createColumn(projectId: UUID, request: CreateKanbanColumnRequest): KanbanColumnResponse {
        val column = mapper.fromCreateRequest(request)
        column.projectId = projectId
        return mapper.toResponse(columnsRepository.save(column))
    }

    fun updateColumn(projectId: UUID, columnId: UUID, request: UpdateKanbanColumnRequest): KanbanColumnResponse {
        val column = columnsRepository.findByIdAndProjectId(columnId, projectId)
            .orElseThrow { NoSuchElementException("Column $columnId not found in project $projectId") }
        request.name?.let { column.name = it }
        request.orderIndex?.let { column.orderIndex = it }
        request.color?.let { column.color = it }
        request.wipLimit?.let { column.wipLimit = it }
        return mapper.toResponse(columnsRepository.save(column))
    }

    fun reorderColumns(projectId: UUID, request: ReorderColumnsRequest) {
        request.columns.forEach { item ->
            columnsRepository.findByIdAndProjectId(item.id, projectId).ifPresent { col ->
                col.orderIndex = item.orderIndex
                columnsRepository.save(col)
            }
        }
    }

    fun deactivateColumn(projectId: UUID, columnId: UUID) {
        val column = columnsRepository.findByIdAndProjectId(columnId, projectId)
            .orElseThrow { NoSuchElementException("Column $columnId not found in project $projectId") }
        column.isActive = false
        columnsRepository.save(column)
    }

    fun getPositions(projectId: UUID): List<TaskPositionResponse> =
        positionsRepository.findAllByProjectId(projectId).map { mapper.toPositionResponse(it) }

    fun bulkUpdatePositions(projectId: UUID, request: BulkUpdatePositionsRequest) {
        request.positions.forEach { item ->
            val existing = positionsRepository.findByTaskId(item.taskId)
            if (existing.isPresent) {
                existing.get().apply {
                    kanbanColumnId = item.kanbanColumnId
                    orderIndex = item.orderIndex
                    positionsRepository.save(this)
                }
            } else {
                val pos = KanbanTaskPositions()
                pos.projectId = projectId
                pos.taskId = item.taskId
                pos.kanbanColumnId = item.kanbanColumnId
                pos.orderIndex = item.orderIndex
                positionsRepository.save(pos)
            }
        }
    }
}
