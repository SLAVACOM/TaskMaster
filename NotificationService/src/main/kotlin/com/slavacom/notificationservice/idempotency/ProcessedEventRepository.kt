package com.slavacom.notificationservice.idempotency

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedEventRepository : JpaRepository<ProcessedEvent, String> {
    fun existsProcessedEventByEventId(eventId: String): Boolean

}