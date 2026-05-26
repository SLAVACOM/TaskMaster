package com.slavacom.organizationservice.project.goals

import com.slavacom.organizationservice.entity.ProjectGoals
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ProjectGoalsMapper {

    abstract fun toResponse(goal: ProjectGoals): ProjectGoalResponse
    abstract fun fromCreateRequest(request: CreateProjectGoalRequest): ProjectGoals

    fun localDateToInstant(date: LocalDate?): Instant? =
        date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()

    fun instantToLocalDate(instant: Instant?): LocalDate? =
        instant?.atZone(ZoneOffset.UTC)?.toLocalDate()
}
