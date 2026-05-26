package com.slavacom.taskservice.repository

import com.slavacom.taskservice.dto.TaskSearchRequest
import com.slavacom.taskservice.entity.Task
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Order
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.UUID
import org.springframework.stereotype.Repository

@Repository
class TaskCriteriaRepositoryImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : TaskCriteriaRepository {

    override fun searchAccessibleTasks(
        filter: TaskSearchRequest,
        userId: UUID,
        userProjectIds: Set<UUID>,
        pageable: Pageable,
    ): Page<Task> {
        val cb = entityManager.criteriaBuilder

        val query = cb.createQuery(Task::class.java)
        val root = query.from(Task::class.java)
        val predicates = buildPredicates(filter, cb, root)
        if (filter.assignedToMe == true) {
            predicates += buildAssignedToMePredicate(cb, root, userId)
        } else {
            predicates += buildAccessPredicate(cb, root, userId, userProjectIds, filter.organizationId)
        }

        query.select(root).where(*predicates.toTypedArray())
        val orders = buildOrders(cb, root, pageable)
        if (orders.isNotEmpty()) query.orderBy(orders)

        val typedQuery = entityManager.createQuery(query)
        typedQuery.firstResult = pageable.offset.toInt()
        typedQuery.maxResults = pageable.pageSize
        val result = typedQuery.resultList

        val countQuery = cb.createQuery(Long::class.java)
        val countRoot = countQuery.from(Task::class.java)
        val countPredicates = buildPredicates(filter, cb, countRoot)
        if (filter.assignedToMe == true) {
            countPredicates += buildAssignedToMePredicate(cb, countRoot, userId)
        } else {
            countPredicates += buildAccessPredicate(cb, countRoot, userId, userProjectIds, filter.organizationId)
        }
        countQuery.select(cb.count(countRoot)).where(*countPredicates.toTypedArray())
        val total = entityManager.createQuery(countQuery).singleResult

        return PageImpl(result, pageable, total)
    }

    private fun buildAccessPredicate(
        cb: CriteriaBuilder,
        root: Root<Task>,
        userId: UUID,
        userProjectIds: Set<UUID>,
        organizationId: UUID?,
    ): Predicate {
        val userIdStr = userId.toString()
        val conditions = mutableListOf<Predicate>()

        // Role-based access: always visible when user is explicitly assigned
        conditions += cb.equal(root.get<UUID>("responsible"), userId)
        conditions += cb.equal(root.get<UUID>("executor"), userId)
        conditions += cb.like(root.get("watchers"), "%$userIdStr%")
        conditions += cb.like(root.get("observers"), "%$userIdStr%")

        // Project-based access: scoped by organization when org filter is present
        if (userProjectIds.isNotEmpty()) {
            val projectIn = root.get<UUID>("projectId").`in`(userProjectIds)
            val projectPredicate = if (organizationId != null)
                cb.and(projectIn, cb.equal(root.get<UUID>("organizationId"), organizationId))
            else projectIn
            conditions += projectPredicate
        }

        return cb.or(*conditions.toTypedArray())
    }

    override fun searchTasks(filter: TaskSearchRequest, pageable: Pageable): Page<Task> {
        val cb = entityManager.criteriaBuilder

        val query = cb.createQuery(Task::class.java)
        val root = query.from(Task::class.java)
        val predicates = buildPredicates(filter, cb, root)

        query.select(root).where(*predicates.toTypedArray())
        val orders = buildOrders(cb, root, pageable)
        if (orders.isNotEmpty()) query.orderBy(orders)

        val typedQuery = entityManager.createQuery(query)
        typedQuery.firstResult = pageable.offset.toInt()
        typedQuery.maxResults = pageable.pageSize
        val result = typedQuery.resultList

        val countQuery = cb.createQuery(Long::class.java)
        val countRoot = countQuery.from(Task::class.java)
        val countPredicates = buildPredicates(filter, cb, countRoot)
        countQuery.select(cb.count(countRoot)).where(*countPredicates.toTypedArray())
        val total = entityManager.createQuery(countQuery).singleResult

        return PageImpl(result, pageable, total)
    }

    private fun buildPredicates(
        filter: TaskSearchRequest,
        cb: CriteriaBuilder,
        root: Root<Task>,
    ): MutableList<Predicate> {
        val predicates = mutableListOf<Predicate>()

        filter.isActive?.let { predicates += cb.equal(root.get<Boolean>("isActive"), it) }
        filter.name?.takeIf { it.isNotBlank() }?.let {
            predicates += cb.like(cb.lower(root.get("name")), "%${it.lowercase()}%")
        }
        filter.description?.takeIf { it.isNotBlank() }?.let {
            predicates += cb.like(cb.lower(root.get("description")), "%${it.lowercase()}%")
        }
        filter.status?.let { predicates += cb.equal(root.get<Any>("status"), it) }
        filter.priority?.let { predicates += cb.equal(root.get<Any>("priority"), it) }
        filter.responsible?.let { predicates += cb.equal(root.get<Any>("responsible"), it) }
        filter.executor?.let { predicates += cb.equal(root.get<Any>("executor"), it) }
        filter.sprintId?.let { predicates += cb.equal(root.get<Any>("sprintId"), it) }
        filter.projectId?.let { predicates += cb.equal(root.get<Any>("projectId"), it) }

        // tags/observers хранятся JSON-текстом в БД через AttributeConverter
        filter.tag?.takeIf { it.isNotBlank() }?.let {
            predicates += cb.like(cb.lower(root.get("tags")), "%${it.lowercase()}%")
        }
        filter.observerId?.let {
            predicates += cb.like(root.get("observers"), "%$it%")
        }
        filter.watcherId?.let {
            predicates += cb.like(root.get("watchers"), "%$it%")
        }

        filter.startFrom?.let { predicates += cb.greaterThanOrEqualTo(root.get("start"), it) }
        filter.startTo?.let { predicates += cb.lessThanOrEqualTo(root.get("start"), it) }
        filter.deadlineFrom?.let { predicates += cb.greaterThanOrEqualTo(root.get("deadline"), it) }
        filter.deadlineTo?.let { predicates += cb.lessThanOrEqualTo(root.get("deadline"), it) }

        return predicates
    }

    private fun buildAssignedToMePredicate(cb: CriteriaBuilder, root: Root<Task>, userId: UUID): Predicate =
        cb.or(
            cb.equal(root.get<UUID>("responsible"), userId),
            cb.equal(root.get<UUID>("executor"), userId),
        )

    private fun buildOrders(cb: CriteriaBuilder, root: Root<Task>, pageable: Pageable): List<Order> =
        pageable.sort.mapNotNull { sortOrder ->
            val expression = sortExpression(root, sortOrder.property) ?: return@mapNotNull null
            if (sortOrder.isAscending) cb.asc(expression) else cb.desc(expression)
        }

    private fun sortExpression(root: Root<Task>, property: String) = when (property) {
        "createdAt" -> root.get<Comparable<Any>>("createdAt")
        "updatedAt" -> root.get<Comparable<Any>>("updatedAt")
        "name" -> root.get<Comparable<Any>>("name")
        "priority" -> root.get<Comparable<Any>>("priority")
        "status" -> root.get<Comparable<Any>>("status")
        "deadline" -> root.get<Comparable<Any>>("deadline")
        "start" -> root.get<Comparable<Any>>("start")
        else -> null
    }
}

