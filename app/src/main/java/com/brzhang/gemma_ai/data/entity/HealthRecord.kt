package com.brzhang.gemma_ai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_record")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "default",
    val timestamp: Long = System.currentTimeMillis(),
    val recordType: String,     // "blood_pressure" / "glucose" / "heart_rate" / "symptom"
    val value: Float,
    val secondaryValue: Float? = null, // e.g. diastolic BP
    val unit: String,
    val aiSuggestion: String = "",
    val dangerLevel: Int = 1    // 1-5
)
