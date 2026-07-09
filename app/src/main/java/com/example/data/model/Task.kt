package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val goalId: Int? = null,
    val isDaily: Boolean = false,
    val dailyType: String? = null, // "WEEKDAYS" (Seg-Sex) or "ALL_WEEK" (Seg-Dom)
    val status: String = "TO_DO", // "TO_DO", "IN_PROGRESS", "DONE"
    val createdAt: Long = System.currentTimeMillis()
)
