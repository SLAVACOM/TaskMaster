package com.slavacom.organizationservice.project.statuses

import com.slavacom.organizationservice.entity.ProjectTaskStatuses
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ProjectTaskStatusesMapper {
    abstract fun toResponse(status: ProjectTaskStatuses): TaskStatusResponse
    abstract fun fromCreateRequest(request: CreateTaskStatusRequest): ProjectTaskStatuses
}
