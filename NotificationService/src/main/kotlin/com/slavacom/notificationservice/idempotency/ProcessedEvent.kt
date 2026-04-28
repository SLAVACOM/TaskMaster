package com.slavacom.notificationservice.idempotency

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class ProcessedEvent(
    @Id
    val eventId: String,

    val processedAt: Instant = Instant.now()
)