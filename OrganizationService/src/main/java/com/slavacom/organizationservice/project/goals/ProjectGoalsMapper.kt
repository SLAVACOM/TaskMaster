package com.slavacom.organizationservice.project.goals

import com.slavacom.organizationservice.entity.ProjectGoals
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ProjectGoalsMapper {
    abstract fun toResponse(goal: ProjectGoals): ProjectGoalResponse
    abstract fun fromCreateRequest(request: CreateProjectGoalRequest): ProjectGoals
}
