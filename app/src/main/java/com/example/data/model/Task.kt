package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val goalId: Long? = null,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val status: TaskStatus = TaskStatus.TODO,
    val lastGeneratedDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class TaskStatus(val title: String) {
        TODO("A Fazer"),
        IN_PROGRESS("Em Progresso"),
        DONE("Concluído")
    }

    enum class TaskRecurrence(val title: String) {
        NONE("Nenhuma"),
        WEEKDAYS("Seg-Sex"),
        DAILY("Seg-Dom")
    }
}
