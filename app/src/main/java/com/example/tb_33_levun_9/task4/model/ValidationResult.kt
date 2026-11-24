package com.example.tb_33_levun_9.task4.model

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
)