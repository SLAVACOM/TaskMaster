package com.slavacom.organizationservice.employees

import com.slavacom.organizationservice.entity.Employees
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class EmployeesMapper {
    abstract fun toResponse(employee: Employees): EmployeeResponse
    abstract fun fromAddRequest(request: AddEmployeeRequest): Employees
}
