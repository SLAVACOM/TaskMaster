package com.slavacom.organizationservice.controller

import com.slavacom.organizationservice.entity.Organization
import org.springframework.data.jpa.domain.Specification
import java.time.Instant
import java.util.*

data class OrganizationFilter(
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val accountable: UUID? = null,
    val isActive: Boolean? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
) {
    fun toSpecification(): Specification<Organization>? = idSpec()
        .and(nameSpec())
        .and(descriptionSpec())
        .and(accountableSpec())
        .and(isActiveSpec())
        .and(createdAtSpec())
        .and(updatedAtSpec())

    private fun idSpec() = Specification<Organization> { root, _, cb ->
        id?.let {
            cb.equal(root.get<UUID>("id"), it)
        }
    }

    private fun nameSpec() = Specification<Organization> { root, _, cb ->
        name?.takeIf(String::isNotBlank)?.let {
            cb.equal(cb.lower(root.get("name")), it.lowercase())
        }
    }

    private fun descriptionSpec() = Specification<Organization> { root, _, cb ->
        description?.takeIf(String::isNotBlank)?.let {
            cb.equal(cb.lower(root.get("description")), it.lowercase())
        }
    }

    private fun accountableSpec() = Specification<Organization> { root, _, cb ->
        accountable?.let {
            cb.equal(root.get<UUID>("accountable"), it)
        }
    }

    private fun isActiveSpec() = Specification<Organization> { root, _, cb ->
        isActive?.let {
            cb.equal(root.get<Boolean>("isActive"), it)
        }
    }

    private fun createdAtSpec() = Specification<Organization> { root, _, cb ->
        createdAt?.let {
            cb.equal(root.get<Instant>("createdAt"), it)
        }
    }

    private fun updatedAtSpec() = Specification<Organization> { root, _, cb ->
        updatedAt?.let {
            cb.equal(root.get<Instant>("updatedAt"), it)
        }
    }
}