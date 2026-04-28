package com.slavacom.organizationservice.invitation

import com.slavacom.organizationservice.entity.OrganizationInvitation
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class InvitationMapper {
    abstract fun toResponse(invitation: OrganizationInvitation): InvitationResponse
    abstract fun fromCreateRequest(request: CreateInvitationRequest): OrganizationInvitation
}
