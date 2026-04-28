package com.slavacom.organizationservice.exception

import java.util.UUID

class OrganizationNotFoundException(id: UUID?) : RuntimeException("Organization with id $id not found")

