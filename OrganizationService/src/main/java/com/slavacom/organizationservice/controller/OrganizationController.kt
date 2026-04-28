package com.slavacom.organizationservice.controller

import com.slavacom.organizationservice.service.OrganizationService
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/organizations")
class OrganizationController(
    private val service: OrganizationService
) {

    @GetMapping
    fun getOrganizations(@RequestParam(defaultValue = "true") isActive: Boolean): List<OrganizationResponse> =
        service.getAll(isActive)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): OrganizationResponse =
        service.getById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateOrganizationRequest): OrganizationResponse =
        service.create(request, currentUserId())

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody request: UpdateOrganizationRequest): OrganizationResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deactivate(@PathVariable id: UUID) =
        service.deactivate(id)

    private fun currentUserId(): UUID =
        UUID.fromString(SecurityContextHolder.getContext().authentication.principal as String)
}
