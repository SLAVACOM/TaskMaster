package com.slavacom.organizationservice.project.tags

import com.slavacom.organizationservice.entity.ProjectTags
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ProjectTagsMapper {
    abstract fun toResponse(tag: ProjectTags): ProjectTagResponse
    abstract fun fromCreateRequest(request: CreateProjectTagRequest): ProjectTags
}
