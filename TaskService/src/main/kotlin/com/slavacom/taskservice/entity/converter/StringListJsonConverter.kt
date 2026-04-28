package com.slavacom.taskservice.entity.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlin.collections.emptyList

@Converter
class StringListJsonConverter : AttributeConverter<List<String>, String> {

    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<String>?): String {
            return objectMapper.writeValueAsString(attribute ?: emptyList<String>())
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        if (dbData.isNullOrBlank()) {
            return emptyList()
        }
        return objectMapper.readValue(dbData, object : TypeReference<List<String>>() {})
    }
}

