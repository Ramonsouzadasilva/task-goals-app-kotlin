package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GoalEntity
import com.example.data.TaskEntity
import com.example.viewmodel.BoardViewModel
import com.example.viewmodel.FilterType
import com.example.viewmodel.ViewType
import kotlin.math.roundToInt

@Composable
fun NotionChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    accentColor: Color? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val bg = if (selected) colorScheme.primary else colorScheme.surface
    val textCol = if (selected) colorScheme.onPrimary else colorScheme.primary
    val borderCol = if (selected) colorScheme.primary else colorScheme.outline

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (accentColor != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .border(0.5.dp, colorScheme.outline, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textCol
        )
        if (badge != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (selected) colorScheme.onPrimary.copy(alpha = 0.25f) else colorScheme.outline)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) colorScheme.onPrimary else colorScheme.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: BoardViewModel) {
    val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val selectedGoalFilter by viewModel.selectedGoalFilter.collectAsState()
    val currentViewType by viewModel.currentViewType.collectAsState()
    val filteredTasks by viewModel.filteredTasks.collectAsState()
    val allGoals by viewModel.allGoals.collectAsState()
    val draggedTaskId by viewModel.draggedTaskId.collectAsState()

    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }
    var newTaskGoalId by remember { mutableStateOf<Int?>(null) }
    var newTaskIsDaily by remember { mutableStateOf(false) }
    var newTaskDailyType by remember { mutableStateOf("WEEKDAY") }
    var newTaskStatus by remember { mutableStateOf("TODO") }

    val colorScheme = MaterialTheme.colorScheme

    Row(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        // Retractable Sidebar
        AnimatedVisibility(
            visible = isSidebarOpen,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut()
        ) {
            Sidebar(viewModel = viewModel)
        }

        // Main Workspace Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(colorScheme.background)
        ) {
            // Header bar
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (currentFilter == FilterType.GOALS_ONLY && selectedGoalFilter != null) {
                                "Meta: ${selectedGoalFilter?.title}"
                            } else {
                                when (currentFilter) {
                                    FilterType.ALL -> "Quadro de Atividades"
                                    FilterType.DAILY -> "Tarefas Diárias"
                                    FilterType.NO_GOALS -> "Tarefas Sem Metas"
                                    else -> "Quadro"
                                }
                            },
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.isSidebarOpen.value = !isSidebarOpen },
                            modifier = Modifier.testTag("sidebar_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isSidebarOpen) Icons.Default.MenuOpen else Icons.Default.Menu,
                                contentDescription = "Toggle Sidebar",
                                tint = colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showCreateTaskDialog = true },
                            modifier = Modifier.testTag("quick_add_task_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Nova Tarefa",
                                tint = colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.background,
                        titleContentColor = colorScheme.primary
                    )
                )
                // Bottom divider replacing standard border
                HorizontalDivider(color = colorScheme.outline, thickness = 1.dp)
            }

            // Tags row and View style segment control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quick toggles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    NotionChip(
                        selected = currentFilter == FilterType.ALL,
                        onClick = {
                            viewModel.currentFilter.value = FilterType.ALL
                            viewModel.selectedGoalFilter.value = null
                        },
                        label = "Todas",
                        modifier = Modifier.testTag("filter_all_chip")
                    )

                    NotionChip(
                        selected = currentFilter == FilterType.DAILY,
                        onClick = {
                            viewModel.currentFilter.value = FilterType.DAILY
                            viewModel.selectedGoalFilter.value = null
                        },
                        label = "Diárias",
                        modifier = Modifier.testTag("filter_daily_chip")
                    )

                    NotionChip(
                        selected = currentFilter == FilterType.NO_GOALS,
                        onClick = {
                            viewModel.currentFilter.value = FilterType.NO_GOALS
                            viewModel.selectedGoalFilter.value = null
                        },
                        label = "Sem Metas",
                        modifier = Modifier.testTag("filter_no_goals_chip")
                    )
                }

                // View Toggle: Segment Box
                Row(
                    modifier = Modifier
                        .background(colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentViewType == ViewType.BOARD) colorScheme.outline else Color.Transparent)
                            .clickable { viewModel.currentViewType.value = ViewType.BOARD }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("board_view_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Quadro",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentViewType == ViewType.BOARD) colorScheme.primary else colorScheme.secondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentViewType == ViewType.LIST) colorScheme.outline else Color.Transparent)
                            .clickable { viewModel.currentViewType.value = ViewType.LIST }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("list_view_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Lista",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentViewType == ViewType.LIST) colorScheme.primary else colorScheme.secondary
                        )
                    }
                }
            }

            // Main Contents Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (currentViewType == ViewType.BOARD) {
                    BoardView(
                        tasks = filteredTasks,
                        allGoals = allGoals,
                        draggedTaskId = draggedTaskId,
                        onDragStart = { id -> viewModel.setDraggedTaskId(id) },
                        onDragEnd = { id, targetStatus ->
                            if (id != null && targetStatus != null) {
                                viewModel.updateTaskStatus(id, targetStatus)
                            }
                            viewModel.setDraggedTaskId(null)
                        },
                        onQuickStatusChange = { id, status ->
                            viewModel.updateTaskStatus(id, status)
                        },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                        onAddTask = { status ->
                            newTaskStatus = status
                            showCreateTaskDialog = true
                        }
                    )
                } else {
                    ListView(
                        tasks = filteredTasks,
                        allGoals = allGoals,
                        onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onQuickStatusChange = { id, status ->
                            viewModel.updateTaskStatus(id, status)
                        }
                    )
                }
            }
        }
    }

    // Modal dialog to add a task
    if (showCreateTaskDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTaskDialog = false },
            title = {
                Text(
                    text = "Criar Nova Tarefa",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorScheme.primary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Título da Tarefa") },
                        placeholder = { Text("Ex: Exercício físico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_task_title_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = newTaskDesc,
                        onValueChange = { newTaskDesc = it },
                        label = { Text("Descrição") },
                        placeholder = { Text("Detalhes da tarefa...") },
                        modifier = Modifier.fillMaxWidth().testTag("new_task_desc_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "Associar à Meta",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.secondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NotionChip(
                            selected = newTaskGoalId == null,
                            onClick = { newTaskGoalId = null },
                            label = "Sem Meta"
                        )

                        allGoals.take(3).forEach { goal ->
                            val isSelected = newTaskGoalId == goal.id
                            val goalColor = remember(goal.color) {
                                try { Color(android.graphics.Color.parseColor(goal.color)) } catch (e: Exception) { Color.White }
                            }
                            NotionChip(
                                selected = isSelected,
                                onClick = { newTaskGoalId = goal.id },
                                label = goal.title,
                                accentColor = goalColor
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tarefa Diária?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.primary
                        )
                        Switch(
                            checked = newTaskIsDaily,
                            onCheckedChange = { newTaskIsDaily = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorScheme.onPrimary,
                                checkedTrackColor = colorScheme.primary
                            ),
                            modifier = Modifier.testTag("new_task_daily_switch")
                        )
                    }

                    if (newTaskIsDaily) {
                        Text(
                            text = "Frequência Diária",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.secondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NotionChip(
                                selected = newTaskDailyType == "WEEKDAY",
                                onClick = { newTaskDailyType = "WEEKDAY" },
                                label = "M-F (Segunda a Sexta)"
                            )

                            NotionChip(
                                selected = newTaskDailyType == "ALL_WEEK",
                                onClick = { newTaskDailyType = "ALL_WEEK" },
                                label = "M-S (Segunda a Domingo)"
                            )
                        }
                    }

                    Text(
                        text = "Coluna Inicial",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.secondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("TODO" to "A Fazer", "IN_PROGRESS" to "Em Progresso", "DONE" to "Concluído").forEach { (status, label) ->
                            NotionChip(
                                selected = newTaskStatus == status,
                                onClick = { newTaskStatus = status },
                                label = label
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.createTask(
                                title = newTaskTitle,
                                description = newTaskDesc,
                                goalId = newTaskGoalId,
                                isDaily = newTaskIsDaily,
                                dailyType = if (newTaskIsDaily) newTaskDailyType else null,
                                status = newTaskStatus
                            )
                            newTaskTitle = ""
                            newTaskDesc = ""
                            newTaskGoalId = null
                            newTaskIsDaily = false
                            newTaskDailyType = "WEEKDAY"
                            newTaskStatus = "TODO"
                            showCreateTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_create_task_btn")
                ) {
                    Text("Salvar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateTaskDialog = false },
                    modifier = Modifier.testTag("dismiss_create_task_btn")
                ) {
                    Text("Cancelar", color = colorScheme.secondary)
                }
            },
            containerColor = colorScheme.background,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, colorScheme.outline, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun BoardView(
    tasks: List<TaskEntity>,
    allGoals: List<GoalEntity>,
    draggedTaskId: Int?,
    onDragStart: (Int) -> Unit,
    onDragEnd: (Int?, String?) -> Unit,
    onQuickStatusChange: (Int, String) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onAddTask: (String) -> Unit
) {
    val columns = listOf(
        Triple("TODO", "A Fazer", Color(0xFF3B82F6)),
        Triple("IN_PROGRESS", "Em Progresso", Color(0xFFF59E0B)),
        Triple("DONE", "Concluído", Color(0xFF10B981))
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        columns.forEach { (status, title, accentColor) ->
            val columnTasks = tasks.filter { it.status == status }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Column Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = columnTasks.size.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { onAddTask(status) },
                        modifier = Modifier.size(24.dp).testTag("add_task_$status")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Criar Tarefa",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Tasks Column list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(columnTasks) { task ->
                        TaskCard(
                            task = task,
                            allGoals = allGoals,
                            isDragged = draggedTaskId == task.id,
                            onDragStarted = { onDragStart(task.id) },
                            onDragEnded = { onDragEnd(draggedTaskId, null) },
                            onDelete = { onDeleteTask(task) },
                            onToggle = { onToggleTask(task) },
                            onQuickStatusChange = { newStatus -> onQuickStatusChange(task.id, newStatus) }
                        )
                    }

                    // Hover/Drop zone indicator
                    item {
                        val isAnyTaskDragging = draggedTaskId != null
                        val dropZoneBorderColor = if (isAnyTaskDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        val dropZoneBgColor = if (isAnyTaskDragging) MaterialTheme.colorScheme.surface else Color.Transparent
                        val dropZoneOpacity = if (isAnyTaskDragging) 0.8f else 0.4f

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.5.dp,
                                    color = dropZoneBorderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(dropZoneBgColor, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (draggedTaskId != null) {
                                        onDragEnd(draggedTaskId, status)
                                    } else {
                                        onAddTask(status)
                                    }
                                }
                                .padding(14.dp)
                                .testTag("drop_zone_$status"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isAnyTaskDragging) Icons.Default.ArrowDownward else Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = dropZoneOpacity),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAnyTaskDragging) "Solte aqui" else "Criar tarefa",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = dropZoneOpacity)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    allGoals: List<GoalEntity>,
    isDragged: Boolean,
    onDragStarted: () -> Unit,
    onDragEnded: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onQuickStatusChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val associatedGoal = remember(task.goalId, allGoals) {
        allGoals.find { it.id == task.goalId }
    }

    val goalBadgeColor = remember(associatedGoal?.color) {
        try {
            Color(android.graphics.Color.parseColor(associatedGoal?.color ?: "#FFFFFF"))
        } catch (e: Exception) {
            Color.White
        }
    }

    var showQuickMenu by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
            .pointerInput(task.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStarted() },
                    onDragEnd = {
                        dragOffset = Offset.Zero
                        onDragEnded()
                    },
                    onDragCancel = {
                        dragOffset = Offset.Zero
                        onDragEnded()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += Offset(dragAmount.x, dragAmount.y)
                    }
                )
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isDragged) 1.5.dp else 1.dp,
                    color = if (isDragged) colorScheme.primary else colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
                .testTag("task_card_${task.id}"),
            colors = CardDefaults.cardColors(
                containerColor = if (isDragged) colorScheme.outline else colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.isDaily) {
                        val dailyLabel = if (task.dailyType == "WEEKDAY") "Diária • Seg-Sex" else "Diária • Seg-Dom"
                        Text(
                            text = dailyLabel.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.secondary,
                            modifier = Modifier
                                .background(colorScheme.background, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else if (associatedGoal != null) {
                        Text(
                            text = associatedGoal.title.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = goalBadgeColor,
                            modifier = Modifier
                                .background(goalBadgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }

                    Box {
                        IconButton(
                            onClick = { showQuickMenu = !showQuickMenu },
                            modifier = Modifier.size(20.dp).testTag("task_menu_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Mais opções",
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showQuickMenu,
                            onDismissRequest = { showQuickMenu = false },
                            modifier = Modifier.background(colorScheme.background).border(1.dp, colorScheme.outline)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mover para: A Fazer", fontSize = 12.sp) },
                                onClick = {
                                    onQuickStatusChange("TODO")
                                    showQuickMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mover para: Em Progresso", fontSize = 12.sp) },
                                onClick = {
                                    onQuickStatusChange("IN_PROGRESS")
                                    showQuickMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mover para: Concluído", fontSize = 12.sp) },
                                onClick = {
                                    onQuickStatusChange("DONE")
                                    showQuickMenu = false
                                }
                            )
                            Divider(color = colorScheme.outline)
                            DropdownMenuItem(
                                text = { Text("Excluir Tarefa", fontSize = 12.sp, color = colorScheme.error) },
                                onClick = {
                                    onDelete()
                                    showQuickMenu = false
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier.size(24.dp).padding(top = 2.dp).testTag("task_check_${task.id}")
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (task.isCompleted) "Desmarcar" else "Concluir",
                            tint = if (task.isCompleted) colorScheme.primary else colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colorScheme.primary,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (task.description.isNotBlank()) {
                            Text(
                                text = task.description,
                                fontSize = 11.sp,
                                color = colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListView(
    tasks: List<TaskEntity>,
    allGoals: List<GoalEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onQuickStatusChange: (Int, String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhuma tarefa nesta visualização.",
                fontSize = 13.sp,
                color = colorScheme.secondary
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                val associatedGoal = allGoals.find { it.id == task.goalId }
                val goalColor = remember(associatedGoal?.color) {
                    try { Color(android.graphics.Color.parseColor(associatedGoal?.color ?: "#FFFFFF")) } catch (e: Exception) { Color.White }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colorScheme.outline, RoundedCornerShape(12.dp))
                        .background(colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = { onToggleTask(task) },
                            modifier = Modifier.size(24.dp).testTag("list_task_check_${task.id}")
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (task.isCompleted) colorScheme.primary else colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colorScheme.primary,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (task.status) {
                                        "TODO" -> "A FAZER"
                                        "IN_PROGRESS" -> "EM PROGRESSO"
                                        else -> "CONCLUÍDO"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.secondary
                                )

                                if (task.isDaily) {
                                    Text(
                                        text = "• DIÁRIA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                }

                                if (associatedGoal != null) {
                                    Text(
                                        text = "• ${associatedGoal.title.uppercase()}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = goalColor
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val nextStatus = when (task.status) {
                                    "TODO" -> "IN_PROGRESS"
                                    "IN_PROGRESS" -> "DONE"
                                    else -> "TODO"
                                }
                                onQuickStatusChange(task.id, nextStatus)
                            },
                            modifier = Modifier.size(28.dp).testTag("cycle_status_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cached,
                                contentDescription = "Ciclar Status",
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteTask(task) },
                            modifier = Modifier.size(28.dp).testTag("delete_task_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Tarefa",
                                tint = colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
