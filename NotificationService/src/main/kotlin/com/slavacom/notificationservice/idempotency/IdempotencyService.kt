package com.slavacom.notificationservice.idempotency

import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service


@Service
open class IdempotencyService(
    private val repository: ProcessedEventRepository
) {
    @Transactional
    fun executeOnce(
        eventId: String,
        action: () -> Unit
    ) {

        if (repository.existsProcessedEventByEventId(eventId)) return

        action()

        repository.save(ProcessedEvent(eventId = eventId))
    }
}