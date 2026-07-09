package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
