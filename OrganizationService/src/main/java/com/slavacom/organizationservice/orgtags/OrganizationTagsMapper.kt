package com.slavacom.organizationservice.orgtags

import com.slavacom.organizationservice.entity.OrganizationTags
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class OrganizationTagsMapper {
    abstract fun toResponse(tag: OrganizationTags): OrgTagResponse
    abstract fun fromCreateRequest(request: CreateOrgTagRequest): OrganizationTags
}
