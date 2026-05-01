package com.slavacom.organizationservice.controller

import com.slavacom.organizationservice.dto.CreateOrganizationRequest
import com.slavacom.organizationservice.dto.OrganizationResponse
import com.slavacom.organizationservice.dto.UpdateOrganizationRequest
import com.slavacom.organizationservice.dto.UserOrganizationInfoResponse
import com.slavacom.organizationservice.service.OrganizationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
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
    fun create(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody request: CreateOrganizationRequest
    ): OrganizationResponse =
        service.create(request, userId)

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody request: UpdateOrganizationRequest): OrganizationResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deactivate(@PathVariable id: UUID) =
        service.deactivate(id)

    @GetMapping("/user/{userId}/info")
    fun getUserOrganizationInfo(@PathVariable userId: UUID): ResponseEntity<UserOrganizationInfoResponse> {
        val info = service.getUserOrganizationInfo(userId)
        return if (info != null) ResponseEntity.ok(info) else ResponseEntity.noContent().build()
    }
}