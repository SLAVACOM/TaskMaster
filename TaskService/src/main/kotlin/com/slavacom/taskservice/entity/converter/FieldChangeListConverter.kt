package com.slavacom.taskservice.entity.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.slavacom.taskservice.entity.FieldChange
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class FieldChangeListConverter : AttributeConverter<List<FieldChange>, String> {
    private val mapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<FieldChange>): String =
        mapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String?): List<FieldChange> {
        if (dbData.isNullOrBlank()) return emptyList()
        return runCatching { mapper.readValue<List<FieldChange>>(dbData) }.getOrDefault(emptyList())
    }
}

