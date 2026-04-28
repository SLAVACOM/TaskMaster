package com.slavacom.organizationservice.controller

import com.slavacom.organizationservice.mapper.OrganizationMapper
import com.slavacom.organizationservice.service.OrganizationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Collections.emptyList

@RestController
@RequestMapping("/api/organizations")
class OrganizationController(
    private val service: OrganizationService,
    private val organizationMapper: OrganizationMapper
) {


    @GetMapping
    public fun getOrganizations(): List<OrganizationResponse> {
        val data = service.getAll()

        println("Found ${data.size} organizations")
        organizationMapper.toOrganizationResponse(data[0])

        return emptyList()
    }


}