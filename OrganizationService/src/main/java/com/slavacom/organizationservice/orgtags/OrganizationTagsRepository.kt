package com.slavacom.organizationservice.orgtags

import com.slavacom.organizationservice.entity.OrganizationTags
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface OrganizationTagsRepository : JpaRepository<OrganizationTags, UUID> {
    fun findAllByOrganizationId(organizationId: UUID): List<OrganizationTags>
    fun findByIdAndOrganizationId(id: UUID, organizationId: UUID): Optional<OrganizationTags>
}
