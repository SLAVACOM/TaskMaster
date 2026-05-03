package com.slavacom.organizationservice.employees

import com.slavacom.organizationservice.entity.Employees
import com.slavacom.organizationservice.entity.EmployeeRole
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class EmployeesMapper {
    fun toResponse(employee: Employees): EmployeeResponse {
        return EmployeeResponse(
            id = employee.id!!,
            userId = employee.userId!!,
            profileId = employee.profileId,
            organizationId = employee.organizationId!!,
            role = employee.role ?: EmployeeRole.MEMBER,
            permissions = employee.permissions,
            isActive = employee.isActive,
            createdAt = employee.createdAt!!,
            updatedAt = employee.updatedAt
        )
    }

    fun fromAddRequest(request: AddEmployeeRequest): Employees {
        val employee = Employees()
        employee.userId = request.userId
        employee.profileId = request.profileId
        employee.role = request.role
        employee.permissions = request.permissions
        employee.isActive = true
        return employee
    }
}
