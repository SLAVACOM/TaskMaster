package com.slavacom.taskservice.entity

data class FieldChange(
    val field: String,
    val oldValue: String?,
    val newValue: String?,
)

