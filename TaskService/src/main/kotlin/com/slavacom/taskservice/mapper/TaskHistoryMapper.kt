package com.slavacom.taskservice.mapper

import com.slavacom.taskservice.dto.TaskHistoryResponse
import com.slavacom.taskservice.entity.TaskHistory
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface TaskHistoryMapper {
    @Mapping(target = "id", expression = "java(java.util.Objects.requireNonNull(h.getId()))")
    fun toResponse(h: TaskHistory): TaskHistoryResponse
}

