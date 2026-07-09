package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FilterType {
    ALL, DAILY, NO_GOALS, GOALS_ONLY
}

enum class ViewType {
    BOARD, LIST
}

class BoardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository

    init {
        val dao = AppDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(dao)
        
        // Seed some initial data if database is empty
        seedInitialData()
    }

    // Auth States
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    val isLoginScreenActive = MutableStateFlow(true) // swaps sides!
    val authError = MutableStateFlow<String?>(null)

    // UI States
    val isDarkTheme = MutableStateFlow(true) // Start with "Sophisticated Dark" as requested
    val isSidebarOpen = MutableStateFlow(false)
    val currentFilter = MutableStateFlow(FilterType.ALL)
    val selectedGoalFilter = MutableStateFlow<GoalEntity?>(null)
    val currentViewType = MutableStateFlow(ViewType.BOARD)

    // Lists of tasks and goals from Room
    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<GoalEntity>> = repository.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered lists
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        currentFilter,
        selectedGoalFilter
    ) { tasks, filter, selectedGoal ->
        when (filter) {
            FilterType.ALL -> tasks
            FilterType.DAILY -> tasks.filter { it.isDaily }
            FilterType.NO_GOALS -> tasks.filter { it.goalId == null }
            FilterType.GOALS_ONLY -> {
                if (selectedGoal != null) {
                    tasks.filter { it.goalId == selectedGoal.id }
                } else {
                    tasks.filter { it.goalId != null }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Drag and Drop States
    private val _draggedTaskId = MutableStateFlow<Int?>(null)
    val draggedTaskId: StateFlow<Int?> = _draggedTaskId.asStateFlow()

    fun setDraggedTaskId(id: Int?) {
        _draggedTaskId.value = id
    }

    // Auth actions
    fun login(email: String, name: String) {
        viewModelScope.launch {
            authError.value = null
            if (email.isBlank()) {
                authError.value = "Email não pode ser vazio."
                return@launch
            }
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _currentUser.value = existing
            } else {
                // If user doesn't exist, register them automatically for a seamless login
                val newUser = UserEntity(email = email, name = name.ifBlank { email.substringBefore("@") }, passwordHash = "")
                repository.registerUser(newUser)
                _currentUser.value = newUser
            }
        }
    }

    fun register(email: String, name: String) {
        viewModelScope.launch {
            authError.value = null
            if (email.isBlank() || name.isBlank()) {
                authError.value = "Preencha todos os campos."
                return@launch
            }
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                authError.value = "Email já cadastrado."
            } else {
                val newUser = UserEntity(email = email, name = name, passwordHash = "")
                repository.registerUser(newUser)
                _currentUser.value = newUser
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        isSidebarOpen.value = false
    }

    // Goal Actions
    fun createGoal(title: String, description: String, color: String) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                repository.insertGoal(GoalEntity(title = title, description = description, color = color))
            }
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            // Clean up tasks associated with this goal
            allTasks.value.filter { it.goalId == goal.id }.forEach { task ->
                repository.insertTask(task.copy(goalId = null))
            }
            if (selectedGoalFilter.value?.id == goal.id) {
                selectedGoalFilter.value = null
                currentFilter.value = FilterType.ALL
            }
        }
    }

    // Task Actions
    fun createTask(
        title: String,
        description: String = "",
        goalId: Int? = null,
        isDaily: Boolean = false,
        dailyType: String? = null,
        status: String = "TODO"
    ) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                repository.insertTask(
                    TaskEntity(
                        title = title,
                        description = description,
                        goalId = goalId,
                        isDaily = isDaily,
                        dailyType = dailyType,
                        status = status
                    )
                )
            }
        }
    }

    fun updateTaskStatus(taskId: Int, newStatus: String) {
        viewModelScope.launch {
            val task = allTasks.value.find { it.id == taskId }
            if (task != null) {
                repository.updateTask(task.copy(status = newStatus, isCompleted = newStatus == "DONE"))
            }
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.isCompleted) "TODO" else "DONE"
            repository.updateTask(task.copy(isCompleted = !task.isCompleted, status = newStatus))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            // Check if seeding is needed
            val initialGoals = repository.getAllGoals().first()
            if (initialGoals.isEmpty()) {
                // Insert default goals
                val goalHealth = GoalEntity(title = "Saúde & Fitness", description = "Metas de exercícios e alimentação", color = "#10B981")
                val goalDev = GoalEntity(title = "Estudos de Kotlin", description = "Desenvolvimento Android e Jetpack Compose", color = "#3B82F6")
                val goalLife = GoalEntity(title = "Organização", description = "Melhoria do dia a dia e Notion Board", color = "#EC4899")
                
                repository.insertGoal(goalHealth)
                repository.insertGoal(goalDev)
                repository.insertGoal(goalLife)

                // Get goals to retrieve generated ids
                val goals = repository.getAllGoals().first()
                val healthId = goals.find { it.title == "Saúde & Fitness" }?.id
                val devId = goals.find { it.title == "Estudos de Kotlin" }?.id

                // Insert tasks
                repository.insertTask(TaskEntity(
                    title = "Gym session",
                    description = "Lower body focus. Treino de pernas na academia.",
                    goalId = healthId,
                    status = "TODO",
                    isDaily = false
                ))
                repository.insertTask(TaskEntity(
                    title = "Morning standup sync",
                    description = "Review blockers with dev team.",
                    goalId = devId,
                    status = "TODO",
                    isDaily = true,
                    dailyType = "WEEKDAY"
                ))
                repository.insertTask(TaskEntity(
                    title = "Beber 3L de Água",
                    description = "Segunda a Domingo.",
                    goalId = healthId,
                    status = "IN_PROGRESS",
                    isDaily = true,
                    dailyType = "ALL_WEEK"
                ))
                repository.insertTask(TaskEntity(
                    title = "Configurar Room Database",
                    description = "Definir as tabelas e DAOs para o app Notion Board.",
                    goalId = devId,
                    status = "DONE",
                    isCompleted = true,
                    isDaily = false
                ))
                repository.insertTask(TaskEntity(
                    title = "Criar UI de drag-and-drop",
                    description = "Desenhar os quadros de tarefas com áreas corretas para soltar.",
                    goalId = devId,
                    status = "TODO",
                    isDaily = false
                ))
            }
        }
    }
}
