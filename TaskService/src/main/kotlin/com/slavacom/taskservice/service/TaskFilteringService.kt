package com.slavacom.taskservice.service

import com.slavacom.taskservice.dto.TaskPageResponse
import com.slavacom.taskservice.dto.TaskResponse
import com.slavacom.taskservice.dto.TaskSearchRequest
import com.slavacom.taskservice.mapper.TaskMapper
import com.slavacom.taskservice.repository.ProjectMemberRepository
import com.slavacom.taskservice.repository.TaskRepository
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class TaskFilteringService(
    private val taskRepository: TaskRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val taskMapper: TaskMapper,
) {

    @Transactional(readOnly = true)
    fun getAccessibleTasks(
        userId: UUID,
        organizationId: UUID?,
        filter: TaskSearchRequest,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "createdAt",
        sortDir: String = "desc",
    ): TaskPageResponse {
        val startTime = System.currentTimeMillis()
        logger.info { "Fetching accessible tasks for user=$userId, org=$organizationId, page=$page, size=$size" }

        val sort = if (sortDir.equals("asc", ignoreCase = true)) Sort.by(sortBy).ascending()
                   else Sort.by(sortBy).descending()
        val pageable = PageRequest.of(page, size, sort)

        val userProjectIds = projectMemberRepository.findByUserId(userId)
            .mapNotNull { it.projectId }
            .toSet()

        val effectiveFilter = filter.copy(organizationId = organizationId ?: filter.organizationId)

        val taskPage = taskRepository.searchAccessibleTasks(effectiveFilter, userId, userProjectIds, pageable)

        val duration = System.currentTimeMillis() - startTime
        logger.info { "Fetched ${taskPage.totalElements} accessible tasks for user=$userId in ${duration}ms" }

        return TaskPageResponse(
            content = taskPage.content.map(taskMapper::toResponse),
            page = taskPage.number,
            size = taskPage.size,
            totalElements = taskPage.totalElements,
            totalPages = taskPage.totalPages,
            hasNext = taskPage.hasNext(),
            hasPrevious = taskPage.hasPrevious(),
        )
    }
}
