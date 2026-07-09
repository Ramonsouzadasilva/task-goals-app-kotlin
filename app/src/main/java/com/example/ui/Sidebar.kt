package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GoalEntity
import com.example.viewmodel.BoardViewModel
import com.example.viewmodel.FilterType

@Composable
fun Sidebar(
    viewModel: BoardViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allGoals by viewModel.allGoals.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val selectedGoalFilter by viewModel.selectedGoalFilter.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var newGoalTitle by remember { mutableStateOf("") }
    var newGoalDesc by remember { mutableStateOf("") }
    var newGoalColor by remember { mutableStateOf("#FFFFFF") }

    val colorScheme = MaterialTheme.colorScheme

    val availableColors = listOf(
        "#FFFFFF" to "Branco",
        "#10B981" to "Verde",
        "#3B82F6" to "Azul",
        "#EF4444" to "Vermelho",
        "#F59E0B" to "Amarelo",
        "#EC4899" to "Rosa",
        "#8B5CF6" to "Roxo"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(colorScheme.background)
            .padding(16.dp)
    ) {
        // Workspace Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser?.name?.take(1)?.uppercase() ?: "N",
                    color = colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Workspace de ${currentUser?.name ?: "Usuário"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Notion Board",
                    fontSize = 11.sp,
                    color = colorScheme.secondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Section: Views
            item {
                SidebarSectionHeader(title = "FILTROS TAREFAS")
                
                SidebarItem(
                    title = "Todas as Tarefas",
                    icon = Icons.Default.AllInbox,
                    isSelected = currentFilter == FilterType.ALL,
                    onClick = {
                        viewModel.currentFilter.value = FilterType.ALL
                        viewModel.selectedGoalFilter.value = null
                    }
                )

                SidebarItem(
                    title = "Tarefas Diárias",
                    icon = Icons.Default.Today,
                    isSelected = currentFilter == FilterType.DAILY,
                    onClick = {
                        viewModel.currentFilter.value = FilterType.DAILY
                        viewModel.selectedGoalFilter.value = null
                    }
                )

                SidebarItem(
                    title = "Tarefas Sem Metas",
                    icon = Icons.Default.TurnedInNot,
                    isSelected = currentFilter == FilterType.NO_GOALS,
                    onClick = {
                        viewModel.currentFilter.value = FilterType.NO_GOALS
                        viewModel.selectedGoalFilter.value = null
                    }
                )
            }

            // Section: Goals (Metas)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SidebarSectionHeader(title = "METAS")
                    IconButton(
                        onClick = { showCreateGoalDialog = true },
                        modifier = Modifier.size(24.dp).testTag("add_goal_sidebar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Criar Meta",
                            tint = colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (allGoals.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma meta criada.",
                        fontSize = 12.sp,
                        color = colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                items(allGoals) { goal ->
                    val isSelected = currentFilter == FilterType.GOALS_ONLY && selectedGoalFilter?.id == goal.id
                    GoalSidebarItem(
                        goal = goal,
                        isSelected = isSelected,
                        onClick = {
                            viewModel.currentFilter.value = FilterType.GOALS_ONLY
                            viewModel.selectedGoalFilter.value = goal
                        },
                        onDelete = {
                            viewModel.deleteGoal(goal)
                        }
                    )
                }
            }
        }

        // Sidebar Footer: Theme Toggle & Logout
        Divider(color = colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))

        // Monochrome Theme Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tema Escuro",
                    fontSize = 13.sp,
                    color = colorScheme.primary
                )
            }
            Switch(
                checked = isDark,
                onCheckedChange = { viewModel.isDarkTheme.value = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.onPrimary,
                    checkedTrackColor = colorScheme.primary,
                    uncheckedThumbColor = colorScheme.primary,
                    uncheckedTrackColor = colorScheme.outline
                ),
                modifier = Modifier.graphicsLayer(scaleX = 0.85f, scaleY = 0.85f).testTag("theme_switch")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logout Item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { viewModel.logout() }
                .padding(8.dp)
                .testTag("logout_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Sair",
                tint = colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sair do Espaço",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.error
            )
        }
    }

    // Inline dialog overlay to create goals in Sidebar
    if (showCreateGoalDialog) {
        AlertDialog(
            onDismissRequest = { showCreateGoalDialog = false },
            title = {
                Text(
                    text = "Criar Nova Meta",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorScheme.primary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newGoalTitle,
                        onValueChange = { newGoalTitle = it },
                        label = { Text("Nome da Meta") },
                        placeholder = { Text("Ex: Focar na Saúde") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_goal_title_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = newGoalDesc,
                        onValueChange = { newGoalDesc = it },
                        label = { Text("Descrição") },
                        placeholder = { Text("Opcional") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_goal_desc_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "Cor de Destaque",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.primary
                    )

                    // Color picker buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableColors.forEach { (hex, name) ->
                            val isColorSelected = newGoalColor == hex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isColorSelected) 2.dp else 1.dp,
                                        color = if (isColorSelected) colorScheme.primary else colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable { newGoalColor = hex }
                                    .testTag("color_picker_$hex")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGoalTitle.isNotBlank()) {
                            viewModel.createGoal(newGoalTitle, newGoalDesc, newGoalColor)
                            newGoalTitle = ""
                            newGoalDesc = ""
                            newGoalColor = "#FFFFFF"
                            showCreateGoalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_create_goal_btn")
                ) {
                    Text("Criar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateGoalDialog = false },
                    modifier = Modifier.testTag("dismiss_create_goal_btn")
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
fun SidebarSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    )
}

@Composable
fun SidebarItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colorScheme.outline else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) colorScheme.primary else colorScheme.secondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colorScheme.primary else colorScheme.primary.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun GoalSidebarItem(
    goal: GoalEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val parsedColor = remember(goal.color) {
        try {
            Color(android.graphics.Color.parseColor(goal.color))
        } catch (e: Exception) {
            Color.White
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colorScheme.outline else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Color indicator dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(parsedColor)
                    .border(1.dp, colorScheme.outline, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = goal.title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colorScheme.primary else colorScheme.primary.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Inline delete meta button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(20.dp).testTag("delete_goal_${goal.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Excluir Meta",
                tint = colorScheme.secondary.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
