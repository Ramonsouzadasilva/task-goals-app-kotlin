package com.example.data.repository

import com.example.data.db.GoalDao
import com.example.data.db.TaskDao
import com.example.data.model.Goal
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val goalDao: GoalDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()

    fun getTasksByGoal(goalId: Int): Flow<List<Task>> = taskDao.getTasksByGoal(goalId)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun insertGoal(goal: Goal): Long = goalDao.insertGoal(goal)

    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)

    suspend fun deleteGoal(goal: Goal) {
        // First delete all tasks associated with this goal
        taskDao.deleteTasksByGoal(goal.id)
        // Then delete the goal itself
        goalDao.deleteGoal(goal)
    }
}
