package com.example.data.db

import androidx.room.*
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE username = :username ORDER BY createdAt DESC")
    fun getTasksForUser(username: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE goalId = :goalId")
    suspend fun deleteTasksByGoal(goalId: Long)

    @Query("SELECT * FROM tasks WHERE username = :username AND recurrence != 'NONE'")
    suspend fun getRecurrentTasksForUser(username: String): List<Task>
}
