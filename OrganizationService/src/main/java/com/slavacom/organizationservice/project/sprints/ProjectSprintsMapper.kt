package com.slavacom.organizationservice.project.sprints

import com.slavacom.organizationservice.entity.ProjectSprints
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ProjectSprintsMapper {
    abstract fun toResponse(sprint: ProjectSprints): SprintResponse
    abstract fun fromCreateRequest(request: CreateSprintRequest): ProjectSprints
}
