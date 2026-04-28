package com.slavacom.organizationservice.project.kanban

import com.slavacom.organizationservice.entity.KanbanColumns
import com.slavacom.organizationservice.entity.KanbanTaskPositions
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class KanbanColumnsMapper {
    abstract fun toResponse(column: KanbanColumns): KanbanColumnResponse
    abstract fun fromCreateRequest(request: CreateKanbanColumnRequest): KanbanColumns
    abstract fun toPositionResponse(position: KanbanTaskPositions): TaskPositionResponse
}
