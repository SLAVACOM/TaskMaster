package com.slavacom.organizationservice.employees

import com.slavacom.organizationservice.exception.EmployeeAlreadyExistsException
import com.slavacom.organizationservice.exception.EmployeeNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/organizations/{orgId}/employees")
class EmployeesController(private val service: EmployeesService) {

    @GetMapping
    fun list(@PathVariable orgId: UUID): List<EmployeeResponse> =
        service.list(orgId)

    @GetMapping("/{employeeId}")
    fun getById(
        @PathVariable orgId: UUID,
        @PathVariable employeeId: UUID
    ): ResponseEntity<EmployeeResponse> {
        return try {
            val employee = service.getById(orgId, employeeId)
            ResponseEntity.ok(employee)
        } catch (e: EmployeeNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @PathVariable orgId: UUID,
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody request: AddEmployeeRequest
    ): ResponseEntity<Any> {
        if (!service.isOrganizationOwner(userId, orgId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf<String, String>("error" to "Only organization owner can add employees"))
        }
        return try {
            val employee = service.add(orgId, request)
            ResponseEntity.status(HttpStatus.CREATED).body(employee)
        } catch (e: EmployeeAlreadyExistsException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf<String, String>("error" to (e.message ?: "Employee already exists")))
        }
    }

    @PutMapping("/{employeeId}")
    fun update(
        @PathVariable orgId: UUID,
        @PathVariable employeeId: UUID,
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody request: UpdateEmployeeRequest
    ): ResponseEntity<Any> {
        if (!service.isOrganizationOwner(userId, orgId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf<String, String>("error" to "Only organization owner can update employees"))
        }
        return try {
            val employee = service.update(orgId, employeeId, request)
            ResponseEntity.ok(employee)
        } catch (e: EmployeeNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{employeeId}")
    fun remove(
        @PathVariable orgId: UUID,
        @PathVariable employeeId: UUID,
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<Any> {
        if (!service.isOrganizationOwner(userId, orgId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf<String, String>("error" to "Only organization owner can remove employees"))
        }
        return try {
            service.remove(orgId, employeeId)
            ResponseEntity.noContent().build()
        } catch (e: EmployeeNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}
