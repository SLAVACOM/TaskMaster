package com.slavacom.taskservice.mapper

import com.slavacom.taskservice.dto.TaskResponse
import com.slavacom.taskservice.entity.Task
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
)
interface TaskMapper {
    @Mapping(target = "id", expression = "java(java.util.Objects.requireNonNull(task.getId()))")
    fun toResponse(task: Task): TaskResponse
}


