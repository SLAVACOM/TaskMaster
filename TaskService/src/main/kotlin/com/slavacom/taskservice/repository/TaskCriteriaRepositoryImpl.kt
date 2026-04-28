package com.slavacom.taskservice.repository

import com.slavacom.taskservice.dto.TaskSearchRequest
import com.slavacom.taskservice.entity.Task
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class TaskCriteriaRepositoryImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : TaskCriteriaRepository {

    override fun searchTasks(filter: TaskSearchRequest, pageable: Pageable): Page<Task> {
        val cb = entityManager.criteriaBuilder

        val query = cb.createQuery(Task::class.java)
        val root = query.from(Task::class.java)
        val predicates = buildPredicates(filter, cb, root)

        query.select(root).where(*predicates.toTypedArray())
        val orders = pageable.sort.mapNotNull { sortOrder ->
            val expression = sortExpression(root, sortOrder.property) ?: return@mapNotNull null
            if (sortOrder.isAscending) cb.asc(expression) else cb.desc(expression)
        }.toList()
        if (orders.isNotEmpty()) {
            query.orderBy(orders)
        }

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

        // tags/observers хранятся JSON-текстом в БД через AttributeConverter
        filter.tag?.takeIf { it.isNotBlank() }?.let {
            predicates += cb.like(cb.lower(root.get("tags")), "%${it.lowercase()}%")
        }
        filter.observerId?.let {
            predicates += cb.like(root.get("observers"), "%$it%")
        }

        filter.startFrom?.let { predicates += cb.greaterThanOrEqualTo(root.get("start"), it) }
        filter.startTo?.let { predicates += cb.lessThanOrEqualTo(root.get("start"), it) }
        filter.deadlineFrom?.let { predicates += cb.greaterThanOrEqualTo(root.get("deadline"), it) }
        filter.deadlineTo?.let { predicates += cb.lessThanOrEqualTo(root.get("deadline"), it) }

        return predicates
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

