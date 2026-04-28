package com.slavacom.organizationservice.employees

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/organizations/{orgId}/employees")
class EmployeesController(private val service: EmployeesService) {

    @GetMapping
    fun list(@PathVariable orgId: UUID): List<EmployeeResponse> =
        service.list(orgId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@PathVariable orgId: UUID, @RequestBody request: AddEmployeeRequest): EmployeeResponse =
        service.add(orgId, request)

    @PutMapping("/{employeeId}")
    fun update(
        @PathVariable orgId: UUID,
        @PathVariable employeeId: UUID,
        @RequestBody request: UpdateEmployeeRequest
    ): EmployeeResponse = service.update(orgId, employeeId, request)

    @DeleteMapping("/{employeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@PathVariable orgId: UUID, @PathVariable employeeId: UUID) =
        service.remove(orgId, employeeId)
}
