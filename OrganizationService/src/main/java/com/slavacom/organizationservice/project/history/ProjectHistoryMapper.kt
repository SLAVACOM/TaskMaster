package com.slavacom.organizationservice.project.history

import com.slavacom.organizationservice.entity.ProjectHistory
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ProjectHistoryMapper {
    abstract fun toResponse(history: ProjectHistory): ProjectHistoryResponse
}
