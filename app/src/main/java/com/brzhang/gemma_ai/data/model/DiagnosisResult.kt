package com.brzhang.gemma_ai.data.model

data class DiagnosisResult(
    val symptoms: String = "",
    val possibleCause: String = "",
    val suggestion: String = "",
    val seeDoctor: Boolean = false,
    val dangerLevel: Int = 1,
    val displayText: String = ""
)
