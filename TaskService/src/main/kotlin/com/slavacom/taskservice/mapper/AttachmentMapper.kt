package com.slavacom.taskservice.mapper

import com.slavacom.taskservice.dto.AttachmentResponse
import com.slavacom.taskservice.entity.Attachment
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface AttachmentMapper {
	fun toResponse(attachment: Attachment): AttachmentResponse
}
