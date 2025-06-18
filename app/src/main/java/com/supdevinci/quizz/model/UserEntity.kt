package com.supdevinci.quizz.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pseudo: String,
    val score: Int = 0,
    val maxScore: Int = 0,
    val createdAt: Date,
    val updatedAt: Date? = null
)