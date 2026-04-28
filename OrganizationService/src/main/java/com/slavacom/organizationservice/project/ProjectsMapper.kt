package com.slavacom.organizationservice.project

import com.slavacom.organizationservice.entity.Projects
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ProjectsMapper {
    abstract fun toResponse(project: Projects): ProjectResponse
    abstract fun fromCreateRequest(request: CreateProjectRequest): Projects
}
