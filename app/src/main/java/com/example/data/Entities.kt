package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val color: String = "#FFFFFF",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val goalId: Int? = null,
    val isCompleted: Boolean = false,
    val status: String = "TODO", // "TODO", "IN_PROGRESS", "DONE"
    val isDaily: Boolean = false,
    val dailyType: String? = null, // "WEEKDAY" or "ALL_WEEK"
    val createdAt: Long = System.currentTimeMillis()
)
