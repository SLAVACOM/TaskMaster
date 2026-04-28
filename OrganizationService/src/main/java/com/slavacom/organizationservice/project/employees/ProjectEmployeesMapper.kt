package com.slavacom.organizationservice.project.employees

import com.slavacom.organizationservice.entity.ProjectEmployees
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ProjectEmployeesMapper {
    abstract fun toResponse(employee: ProjectEmployees): ProjectEmployeeResponse
    abstract fun fromAddRequest(request: AddProjectEmployeeRequest): ProjectEmployees
}
