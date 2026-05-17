package com.brzhang.gemma_ai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_history")
data class ChatHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "default",
    val timestamp: Long = System.currentTimeMillis(),
    val role: String,        // "user" / "assistant"
    val content: String,
    val inputType: String    // "voice" / "image" / "text"
)
