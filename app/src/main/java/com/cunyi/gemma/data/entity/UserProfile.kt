package com.cunyi.gemma.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val userId: String = "default",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val phone: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val chronicDiseases: String = "",
    val chronicConditions: String = "",
    val currentMedications: String = "",
    val emergencyContact: String = "",
    val emergencyContacts: String = "",
    val emergencyPhone: String = "",
    val village: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)