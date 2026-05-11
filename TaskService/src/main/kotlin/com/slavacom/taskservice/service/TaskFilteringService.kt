package com.slavacom.taskservice.service

import com.slavacom.taskservice.entity.Task
import com.slavacom.taskservice.repository.ProjectMemberRepository
import com.slavacom.taskservice.repository.TaskRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class TaskFilteringService(
	private val taskRepository: TaskRepository,
	private val projectMemberRepository: ProjectMemberRepository,
) {

	@Transactional(readOnly = true)
	fun getAccessibleTasks(userId: UUID): List<Task> {
		val startTime = System.currentTimeMillis()
		logger.info { "Fetching accessible tasks for user: $userId" }

		try {
			// Get tasks where user has any role (responsible, executor, observer, watcher)
			val tasksAssignedToUser = taskRepository.findByIsActiveTrueOrderByCreatedAtDesc()
				.filter { task ->
					task.responsible == userId ||
					task.executor == userId ||
					task.observers.contains(userId) ||
					task.watchers.contains(userId)
				}

			// Get projects where user is a member
			val userProjectMemberships = projectMemberRepository.findByUserId(userId)
			val userProjectIds = userProjectMemberships.mapNotNull { it.projectId }.toSet()

			// Get tasks in user's projects
			val tasksInUserProjects = if (userProjectIds.isNotEmpty()) {
				taskRepository.findByIsActiveTrueOrderByCreatedAtDesc()
					.filter { task -> task.projectId in userProjectIds }
			} else {
				emptyList()
			}

			// Combine and deduplicate
			val allAccessibleTasks = (tasksAssignedToUser + tasksInUserProjects)
				.distinctBy { it.id }
				.sortedByDescending { it.createdAt }

			val duration = System.currentTimeMillis() - startTime
			logger.info { "Fetched ${allAccessibleTasks.size} accessible tasks for user $userId, duration=${duration}ms" }

			return allAccessibleTasks
		} catch (e: Exception) {
			logger.error(e) { "Failed to fetch accessible tasks for user $userId" }
			throw e
		}
	}
}
