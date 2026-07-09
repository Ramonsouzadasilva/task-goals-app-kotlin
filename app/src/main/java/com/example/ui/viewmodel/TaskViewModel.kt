package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SessionManager
import com.example.data.db.AppDatabase
import com.example.data.model.Goal
import com.example.data.model.Task
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val sessionManager = SessionManager(application)
    private val repository = TaskRepository(database.taskDao(), database.goalDao())

    // UI state
    private val _isDarkTheme = MutableStateFlow(sessionManager.isDarkTheme())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isSidebarExpanded = MutableStateFlow(true)
    val isSidebarExpanded: StateFlow<Boolean> = _isSidebarExpanded.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.KANBAN)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter.asStateFlow()

    private val _currentUsername = MutableStateFlow<String?>(sessionManager.getUsername())
    val currentUsername: StateFlow<String?> = _currentUsername.asStateFlow()

    // Data streams
    val tasks: StateFlow<List<Task>> = _currentUsername
        .flatMapLatest { username ->
            if (username != null) {
                repository.getTasksForUser(username)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<Goal>> = _currentUsername
        .flatMapLatest { username ->
            if (username != null) {
                repository.getGoalsForUser(username)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    enum class ViewMode {
        KANBAN, LIST
    }

    enum class TaskFilter {
        ALL, DAILY, WITHOUT_GOALS
    }

    init {
        // Automatically check/create daily recurrent tasks on startup if logged in
        viewModelScope.launch {
            _currentUsername.collect { username ->
                if (username != null) {
                    repository.generateDailyTasksIfNecessary(username)
                }
            }
        }
    }

    // Auth actions
    fun login(username: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                onError("Preencha todos os campos.")
                return@launch
            }
            val userExists = sessionManager.userExists(username)
            if (!userExists) {
                onError("Usuário não cadastrado.")
                return@launch
            }
            val success = sessionManager.login(username, password)
            if (success) {
                _currentUsername.value = username
                repository.generateDailyTasksIfNecessary(username)
                onSuccess()
            } else {
                onError("Senha incorreta.")
            }
        }
    }

    fun register(username: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                onError("Preencha todos os campos.")
                return@launch
            }
            val userExists = sessionManager.userExists(username)
            if (userExists) {
                onError("Nome de usuário já cadastrado.")
                return@launch
            }
            val registered = sessionManager.registerUser(username, password)
            if (registered) {
                sessionManager.login(username, password)
                _currentUsername.value = username
                repository.generateDailyTasksIfNecessary(username)
                onSuccess()
            } else {
                onError("Erro ao registrar usuário.")
            }
        }
    }

    fun logout() {
        sessionManager.logout()
        _currentUsername.value = null
    }

    // UI actions
    fun toggleTheme() {
        val nextTheme = !_isDarkTheme.value
        _isDarkTheme.value = nextTheme
        sessionManager.setDarkTheme(nextTheme)
    }

    fun toggleSidebar() {
        _isSidebarExpanded.value = !_isSidebarExpanded.value
    }

    fun setSidebarExpanded(expanded: Boolean) {
        _isSidebarExpanded.value = expanded
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun setFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    // Goal actions
    fun addGoal(name: String, description: String) {
        val username = _currentUsername.value ?: return
        viewModelScope.launch {
            val goal = Goal(
                username = username,
                name = name,
                description = description
            )
            repository.insertGoal(goal)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Task actions
    fun addTask(title: String, description: String, recurrence: Task.TaskRecurrence, goalId: Long?) {
        val username = _currentUsername.value ?: return
        viewModelScope.launch {
            val task = Task(
                username = username,
                title = title,
                description = description,
                status = Task.TaskStatus.TODO,
                recurrence = recurrence,
                goalId = goalId,
                isCompleted = false,
                lastGeneratedDate = System.currentTimeMillis()
            )
            repository.insertTask(task)
        }
    }

    fun updateTaskStatus(task: Task, status: Task.TaskStatus) {
        viewModelScope.launch {
            val isCompleted = status == Task.TaskStatus.DONE
            repository.updateTask(task.copy(status = status, isCompleted = isCompleted))
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            val isNextCompleted = !task.isCompleted
            val nextStatus = if (isNextCompleted) {
                Task.TaskStatus.DONE
            } else {
                if (task.status == Task.TaskStatus.DONE) Task.TaskStatus.TODO else task.status
            }
            repository.updateTask(task.copy(
                isCompleted = isNextCompleted,
                status = nextStatus
            ))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}
