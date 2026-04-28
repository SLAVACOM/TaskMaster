package com.slavacom.organizationservice.mapper

import com.slavacom.organizationservice.controller.OrganizationResponse
import com.slavacom.organizationservice.entity.Organization
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class OrganizationMapper {
    abstract fun toEntity(organizationResponse: OrganizationResponse): Organization
    abstract fun toOrganizationResponse(organization: Organization): OrganizationResponse
}