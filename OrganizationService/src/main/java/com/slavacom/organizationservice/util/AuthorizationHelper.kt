package com.slavacom.organizationservice.util

import com.slavacom.organizationservice.entity.EmployeeRole
import com.slavacom.organizationservice.employees.EmployeesRepository
import com.slavacom.organizationservice.project.employees.ProjectEmployeesRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Component
class AuthorizationHelper(
    private val employeesRepository: EmployeesRepository,
    private val projectEmployeesRepository: ProjectEmployeesRepository
) {

    private val authorizedOrgRoles = setOf(EmployeeRole.OWNER, EmployeeRole.ADMIN)
    private val authorizedProjectRoles = setOf("OWNER", "ADMIN", "MANAGER")

    fun checkOrgTagCreatePermission(userId: UUID, orgId: UUID) {
        val employee = employeesRepository.findByUserIdAndOrganizationIdAndIsActiveTrue(userId, orgId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User does not have permission to create tags in this organization"
                )
            }

        if (employee.role !in authorizedOrgRoles) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "User role does not have permission to create tags"
            )
        }
    }

    fun checkProjectTagCreatePermission(userId: UUID, projectId: UUID) {
        val projectEmployee = projectEmployeesRepository.findByUserIdAndProjectIdAndIsActiveTrue(userId, projectId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User does not have permission to create tags in this project"
                )
            }

        if (projectEmployee.role !in authorizedProjectRoles) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "User role does not have permission to create tags"
            )
        }
    }
}
