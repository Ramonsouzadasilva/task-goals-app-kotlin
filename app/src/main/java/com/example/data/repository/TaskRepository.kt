package com.example.data.repository

import com.example.data.db.GoalDao
import com.example.data.db.TaskDao
import com.example.data.model.Goal
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TaskRepository(
    private val taskDao: TaskDao,
    private val goalDao: GoalDao
) {
    fun getTasksForUser(username: String): Flow<List<Task>> = taskDao.getTasksForUser(username)
    fun getGoalsForUser(username: String): Flow<List<Goal>> = goalDao.getGoalsForUser(username)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun insertGoal(goal: Goal): Long = goalDao.insertGoal(goal)

    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)

    suspend fun deleteGoal(goal: Goal) {
        // Cascade delete tasks associated with this goal
        taskDao.deleteTasksByGoal(goal.id)
        goalDao.deleteGoal(goal)
    }

    // Reset daily tasks to incomplete/todo status if a new day has arrived and day matches pattern
    suspend fun generateDailyTasksIfNecessary(username: String) {
        val recurrentTasks = taskDao.getRecurrentTasksForUser(username)
        val todayCalendar = Calendar.getInstance()
        val todayYear = todayCalendar.get(Calendar.YEAR)
        val todayDayOfYear = todayCalendar.get(Calendar.DAY_OF_YEAR)
        val dayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK)

        recurrentTasks.forEach { task ->
            val taskCalendar = Calendar.getInstance().apply {
                timeInMillis = task.lastGeneratedDate
            }
            val taskYear = taskCalendar.get(Calendar.YEAR)
            val taskDayOfYear = taskCalendar.get(Calendar.DAY_OF_YEAR)

            // If it's a new day
            if (todayYear != taskYear || todayDayOfYear != taskDayOfYear) {
                var shouldReset = false
                if (task.recurrence == Task.TaskRecurrence.DAILY) {
                    shouldReset = true
                } else if (task.recurrence == Task.TaskRecurrence.WEEKDAYS) {
                    // Monday to Friday
                    val isWeekday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
                    if (isWeekday) {
                        shouldReset = true
                    }
                }

                if (shouldReset) {
                    taskDao.updateTask(task.copy(
                        isCompleted = false,
                        status = Task.TaskStatus.TODO,
                        lastGeneratedDate = todayCalendar.timeInMillis
                    ))
                }
            }
        }
    }
}
