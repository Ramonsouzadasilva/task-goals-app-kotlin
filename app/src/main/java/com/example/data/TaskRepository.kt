package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    suspend fun getUserByEmail(email: String): UserEntity? = taskDao.getUserByEmail(email)
    suspend fun registerUser(user: UserEntity) = taskDao.registerUser(user)

    fun getAllGoals(): Flow<List<GoalEntity>> = taskDao.getAllGoals()
    suspend fun insertGoal(goal: GoalEntity) = taskDao.insertGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = taskDao.deleteGoal(goal)
    suspend fun deleteGoalById(id: Int) = taskDao.deleteGoalById(id)

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Int) = taskDao.deleteTaskById(id)
}
