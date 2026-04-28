package com.slavacom.organizationservice.employees

import com.slavacom.organizationservice.exception.EmployeeAlreadyExistsException
import com.slavacom.organizationservice.exception.EmployeeNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EmployeesService(
    private val employeesRepository: EmployeesRepository,
    private val employeesMapper: EmployeesMapper
) {

    fun list(orgId: UUID): List<EmployeeResponse> =
        employeesRepository.findAllByOrganizationIdAndIsActiveTrue(orgId)
            .map { employeesMapper.toResponse(it) }

    fun add(orgId: UUID, request: AddEmployeeRequest): EmployeeResponse {
        if (employeesRepository.existsByUserIdAndOrganizationIdAndIsActiveTrue(request.userId, orgId)) {
            throw EmployeeAlreadyExistsException("Employee with userId ${request.userId} already exists in organization $orgId")
        }
        val employee = employeesMapper.fromAddRequest(request)
        employee.organizationId = orgId
        return employeesMapper.toResponse(employeesRepository.save(employee))
    }

    fun update(orgId: UUID, employeeId: UUID, request: UpdateEmployeeRequest): EmployeeResponse {
        val employee = employeesRepository.findByIdAndOrganizationId(employeeId, orgId)
            .orElseThrow { EmployeeNotFoundException("Employee $employeeId not found in organization $orgId") }
        request.role?.let { employee.role = it }
        request.permissions?.let { employee.permissions = it }
        return employeesMapper.toResponse(employeesRepository.save(employee))
    }

    fun remove(orgId: UUID, employeeId: UUID) {
        val employee = employeesRepository.findByIdAndOrganizationId(employeeId, orgId)
            .orElseThrow { EmployeeNotFoundException("Employee $employeeId not found in organization $orgId") }
        employee.isActive = false
        employeesRepository.save(employee)
    }
}
