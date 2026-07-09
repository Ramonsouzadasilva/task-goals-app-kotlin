package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Goal
import com.example.data.model.Task
import com.example.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: TaskViewModel,
    onLogout: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isSidebarExpanded by viewModel.isSidebarExpanded.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val currentUsername by viewModel.currentUsername.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var viewingGoalDetails by remember { mutableStateOf<Goal?>(null) }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    // Auto-collapse sidebar on narrow screens
    LaunchedEffect(isWideScreen) {
        if (!isWideScreen && isSidebarExpanded) {
            viewModel.setSidebarExpanded(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Retractable Sidebar (Animated)
            AnimatedVisibility(
                visible = isSidebarExpanded,
                enter = slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn(animationSpec = tween(300)),
                exit = slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut(animationSpec = tween(300))
            ) {
                SidebarContent(
                    username = currentUsername ?: "NotionTask",
                    goals = goals,
                    onAddTaskClick = { showAddTaskDialog = true },
                    onAddGoalClick = { showAddGoalDialog = true },
                    onGoalClick = { viewingGoalDetails = it },
                    onLogout = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(260.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                )
            }

            // Main Content Container
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Header Bar
                HeaderBar(
                    isSidebarExpanded = isSidebarExpanded,
                    onToggleSidebar = { viewModel.toggleSidebar() },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleTheme() }
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Filters panel (Notion style simple capsules)
                FilterTabs(
                    selectedFilter = selectedFilter,
                    onFilterChange = { viewModel.setFilter(it) }
                )

                // Task View Body (Only List View)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    TaskListView(
                        tasks = tasks,
                        goals = goals,
                        selectedFilter = selectedFilter,
                        onToggleComplete = { viewModel.toggleTaskComplete(it) },
                        onUpdateTaskStatus = { task, newStatus -> viewModel.updateTaskStatus(task, newStatus) },
                        onDeleteTask = { viewModel.deleteTask(it) }
                    )
                }
            }
        }

        // Floating Action Button (FAB) for adding tasks
        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("floating_add_task_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nova Tarefa")
        }
    }

    // Dialogs
    if (showAddTaskDialog) {
        AddTaskDialog(
            goals = goals,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, description, recurrence, goalId ->
                viewModel.addTask(title, description, recurrence, goalId)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { name, description ->
                viewModel.addGoal(name, description)
                showAddGoalDialog = false
            }
        )
    }

    if (viewingGoalDetails != null) {
        val goal = viewingGoalDetails!!
        GoalDetailsDialog(
            goal = goal,
            tasks = tasks.filter { it.goalId == goal.id },
            onDismiss = { viewingGoalDetails = null },
            onDeleteGoal = {
                viewModel.deleteGoal(goal)
                viewingGoalDetails = null
            }
        )
    }
}

@Composable
fun SidebarContent(
    username: String,
    goals: List<Goal>,
    onAddTaskClick: () -> Unit,
    onAddGoalClick: () -> Unit,
    onGoalClick: (Goal) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Logo and User Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "NotionTask",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = username,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Navigation / Quick Actions
            SidebarItem(
                icon = Icons.Default.AddCircle,
                label = "Nova Tarefa",
                onClick = onAddTaskClick,
                testTag = "sidebar_add_task"
            )
            SidebarItem(
                icon = Icons.Default.Flag,
                label = "Nova Meta",
                onClick = onAddGoalClick,
                testTag = "sidebar_add_goal"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Goals Section Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "METAS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Criar Meta",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onAddGoalClick() }
                        .testTag("sidebar_create_goal_icon"),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            // Goals list
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (goals.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma meta criada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(goals) { goal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onGoalClick(goal) }
                                .padding(vertical = 8.dp, horizontal = 6.dp)
                                .testTag("sidebar_goal_${goal.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = goal.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Footer / Logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable { onLogout() }
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .testTag("logout_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Sair",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sair da conta",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun SidebarItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun HeaderBar(
    isSidebarExpanded: Boolean,
    onToggleSidebar: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleSidebar,
                modifier = Modifier.testTag("toggle_sidebar_button")
            ) {
                Icon(
                    imageVector = if (isSidebarExpanded) Icons.Default.MenuOpen else Icons.Default.Menu,
                    contentDescription = "Toggle Sidebar",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Meu Espaço",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Dark/Light Theme Toggle Icon
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.testTag("toggle_theme_button")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Alternar Tema",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun FilterTabs(
    selectedFilter: TaskViewModel.TaskFilter,
    onFilterChange: (TaskViewModel.TaskFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterTabItem(
            label = "Todas",
            isSelected = selectedFilter == TaskViewModel.TaskFilter.ALL,
            onClick = { onFilterChange(TaskViewModel.TaskFilter.ALL) },
            testTag = "filter_all"
        )
        FilterTabItem(
            label = "Tarefas Diárias",
            isSelected = selectedFilter == TaskViewModel.TaskFilter.DAILY,
            onClick = { onFilterChange(TaskViewModel.TaskFilter.DAILY) },
            testTag = "filter_daily"
        )
        FilterTabItem(
            label = "Sem Metas",
            isSelected = selectedFilter == TaskViewModel.TaskFilter.WITHOUT_GOALS,
            onClick = { onFilterChange(TaskViewModel.TaskFilter.WITHOUT_GOALS) },
            testTag = "filter_no_goals"
        )
    }
}

@Composable
fun FilterTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.secondary
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
    }
}

@Composable
fun TaskListView(
    tasks: List<Task>,
    goals: List<Goal>,
    selectedFilter: TaskViewModel.TaskFilter,
    onToggleComplete: (Task) -> Unit,
    onUpdateTaskStatus: (Task, Task.TaskStatus) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    val filteredTasks = when (selectedFilter) {
        TaskViewModel.TaskFilter.ALL -> tasks
        TaskViewModel.TaskFilter.DAILY -> tasks.filter { it.recurrence != Task.TaskRecurrence.NONE }
        TaskViewModel.TaskFilter.WITHOUT_GOALS -> tasks.filter { it.goalId == null }
    }

    var isTodoExpanded by remember { mutableStateOf(true) }
    var isInProgressExpanded by remember { mutableStateOf(true) }
    var isDoneExpanded by remember { mutableStateOf(false) }

    if (filteredTasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Nenhuma tarefa encontrada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.TopCenter)
                    .testTag("task_list_view_container"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // TODO Section
                val todoTasks = filteredTasks.filter { it.status == Task.TaskStatus.TODO }
                item {
                    StatusSectionHeader(
                        title = "A Fazer",
                        count = todoTasks.size,
                        isExpanded = isTodoExpanded,
                        onToggle = { isTodoExpanded = !isTodoExpanded },
                        headerColor = MaterialTheme.colorScheme.secondary
                    )
                }
                if (isTodoExpanded) {
                    if (todoTasks.isEmpty()) {
                        item {
                            Text(
                                text = "Sem tarefas a fazer.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                            )
                        }
                    } else {
                        items(todoTasks, key = { it.id }) { task ->
                            TaskListItem(
                                task = task,
                                goals = goals,
                                onToggleComplete = onToggleComplete,
                                onUpdateTaskStatus = onUpdateTaskStatus,
                                onDeleteTask = onDeleteTask
                            )
                        }
                    }
                }

                // IN PROGRESS Section
                val inProgressTasks = filteredTasks.filter { it.status == Task.TaskStatus.IN_PROGRESS }
                item {
                    StatusSectionHeader(
                        title = "Em Progresso",
                        count = inProgressTasks.size,
                        isExpanded = isInProgressExpanded,
                        onToggle = { isInProgressExpanded = !isInProgressExpanded },
                        headerColor = MaterialTheme.colorScheme.primary
                    )
                }
                if (isInProgressExpanded) {
                    if (inProgressTasks.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhuma tarefa em progresso.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                            )
                        }
                    } else {
                        items(inProgressTasks, key = { it.id }) { task ->
                            TaskListItem(
                                task = task,
                                goals = goals,
                                onToggleComplete = onToggleComplete,
                                onUpdateTaskStatus = onUpdateTaskStatus,
                                onDeleteTask = onDeleteTask
                            )
                        }
                    }
                }

                // DONE Section
                val doneTasks = filteredTasks.filter { it.status == Task.TaskStatus.DONE }
                item {
                    StatusSectionHeader(
                        title = "Concluído",
                        count = doneTasks.size,
                        isExpanded = isDoneExpanded,
                        onToggle = { isDoneExpanded = !isDoneExpanded },
                        headerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                }
                if (isDoneExpanded) {
                    if (doneTasks.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhuma tarefa concluída.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                            )
                        }
                    } else {
                        items(doneTasks, key = { it.id }) { task ->
                            TaskListItem(
                                task = task,
                                goals = goals,
                                onToggleComplete = onToggleComplete,
                                onUpdateTaskStatus = onUpdateTaskStatus,
                                onDeleteTask = onDeleteTask
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusSectionHeader(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    headerColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
            contentDescription = if (isExpanded) "Recolher" else "Expandir",
            tint = headerColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = headerColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TaskListItem(
    task: Task,
    goals: List<Goal>,
    onToggleComplete: (Task) -> Unit,
    onUpdateTaskStatus: (Task, Task.TaskStatus) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(12.dp)
            .testTag("task_list_item_${task.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete(task) },
                modifier = Modifier
                    .size(24.dp)
                    .testTag("task_list_checkbox_${task.id}")
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Status dropdown badge button
                    Box {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                .clickable { statusDropdownExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .testTag("task_status_badge_${task.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = task.status.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Mudar Status",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            Task.TaskStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = status.title,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                    },
                                    onClick = {
                                        if (status != task.status) {
                                            onUpdateTaskStatus(task, status)
                                        }
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Meta and Recurrence Row
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val goal = goals.find { it.id == task.goalId }
                    if (goal != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = goal.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (task.recurrence != Task.TaskRecurrence.NONE) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.recurrence.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = { onDeleteTask(task) },
            modifier = Modifier.testTag("task_list_delete_button_${task.id}")
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Excluir Tarefa",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    goals: List<Goal>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Task.TaskRecurrence, Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf(Task.TaskRecurrence.NONE) }
    var selectedGoalId by remember { mutableStateOf<Long?>(null) }
    var goalsDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_task_dialog"),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Criar Tarefa",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Título",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Nome da tarefa") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("add_task_title_input")
                )

                Text(
                    text = "Descrição",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Ex: Detalhes ou notas...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("add_task_desc_input")
                )

                // Recurrence option
                Text(
                    text = "Repetição Diária",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Task.TaskRecurrence.values().forEach { r ->
                        val isSelected = recurrence == r
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.secondary
                        val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(containerColor)
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .clickable { recurrence = r }
                                .padding(vertical = 8.dp)
                                .testTag("recurrence_option_${r.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (r) {
                                    Task.TaskRecurrence.NONE -> "Nenhuma"
                                    Task.TaskRecurrence.WEEKDAYS -> "Seg-Sex"
                                    Task.TaskRecurrence.DAILY -> "Seg-Dom"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = contentColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Goal Dropdown
                Text(
                    text = "Associar à Meta",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    val selectedGoalText = goals.find { it.id == selectedGoalId }?.name ?: "Nenhuma meta"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .clickable { goalsDropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .testTag("associate_goal_dropdown"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedGoalText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Selecionar Meta",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    DropdownMenu(
                        expanded = goalsDropdownExpanded,
                        onDismissRequest = { goalsDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .testTag("goals_dropdown_menu")
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma meta") },
                            onClick = {
                                selectedGoalId = null
                                goalsDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("goal_option_none")
                        )
                        goals.forEach { goal ->
                            DropdownMenuItem(
                                text = { Text(goal.name) },
                                onClick = {
                                    selectedGoalId = goal.id
                                    goalsDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("goal_option_${goal.id}")
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("add_task_cancel")
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, description, recurrence, selectedGoalId)
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("add_task_confirm")
                    ) {
                        Text("Criar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_goal_dialog"),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Criar Nova Meta",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Nome da Meta",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ex: Ler 10 livros") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("add_goal_name_input")
                )

                Text(
                    text = "Descrição / Notas",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Ex: Meta pessoal para este ano...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("add_goal_desc_input")
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("add_goal_cancel")
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name, description)
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("add_goal_confirm")
                    ) {
                        Text("Criar")
                    }
                }
            }
        }
    }
}

@Composable
fun GoalDetailsDialog(
    goal: Goal,
    tasks: List<Task>,
    onDismiss: () -> Unit,
    onDeleteGoal: () -> Unit
) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("goal_details_dialog"),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(
                        onClick = onDeleteGoal,
                        modifier = Modifier.testTag("delete_goal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Meta",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (goal.description.isNotBlank()) {
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Progress Indicator
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progresso",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$completedTasks/$totalTasks (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = progress,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                // Associated Tasks List
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Tarefas Associadas",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                ) {
                    if (tasks.isEmpty()) {
                        Text(
                            text = "Nenhuma tarefa associada a esta meta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(tasks) { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("goal_details_close")
                ) {
                    Text("Fechar")
                }
            }
        }
    }
}
