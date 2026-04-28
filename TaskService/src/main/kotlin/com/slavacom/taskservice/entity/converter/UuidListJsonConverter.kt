package com.slavacom.taskservice.entity.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.util.UUID

@Converter
class UuidListJsonConverter : AttributeConverter<List<UUID>, String> {

    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<UUID>?): String {
        return objectMapper.writeValueAsString(attribute ?: emptyList<UUID>())
    }

    override fun convertToEntityAttribute(dbData: String?): List<UUID> {
        if (dbData.isNullOrBlank()) {
            return emptyList()
        }
        return objectMapper.readValue(dbData, object : TypeReference<List<UUID>>() {})
    }
}

