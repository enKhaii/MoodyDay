package com.example.moodyday.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodyday.data.local.entities.GoalEntity
import java.util.Locale

private val EcoPrimary = Color(0xFF003D61)
private val EcoBackground = Color(0xFFF7F9FB)
private val LightBlueIconBg = Color(0xFFD6EAF8)
private val ChipDailyBg = Color(0xFFD1E8F7)
private val ChipDailyText = Color(0xFF245F85)
private val ChipWeeklyBg = Color(0xFF635274)
private val ChipWeeklyText = Color.White
private val ChipCo2Bg = Color(0xFFECEFF1)
private val ChipCo2Text = Color(0xFF455A64)
private val PillBackground = Color(0xFFE9ECEF)

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel
) {
    var showAddScreen by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<GoalEntity?>(null) }
    var goalToDelete by remember { mutableStateOf<GoalEntity?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val userStreak by viewModel.userStreak.collectAsState()
    val allGoals by viewModel.goals.collectAsState()

    val activeGoals = remember(allGoals) { allGoals.filter { !it.isCompleted } }
    val completedGoals = remember(allGoals) { allGoals.filter { it.isCompleted } }

    val filteredActiveGoals = remember(activeGoals, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") activeGoals
        else activeGoals.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    val totalCo2SavedKg = remember(completedGoals) {
        completedGoals.mapNotNull { goal ->
            val num = goal.targetCo2.substringBefore(" ").toDoubleOrNull() ?: 0.0
            if (goal.targetCo2.contains("g CO₂", ignoreCase = true) && !goal.targetCo2.contains("kg", ignoreCase = true)) {
                num / 1000.0
            } else {
                num
            }
        }.sum()
    }

    if (goalToDelete != null) {
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = { Text("Delete Goal?") },
            text = { Text(
                "Are you sure you want to delete this goal?",
                fontSize = 16.sp
            )
                   },
            confirmButton = {
                TextButton(
                    onClick = {
                        goalToDelete?.id?.let { viewModel.deleteGoal(it) }
                        goalToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddScreen || goalToEdit != null) {
        AddGoalScreen(
            initialGoal = goalToEdit,
            onDismiss = {
                showAddScreen = false
                goalToEdit = null
            },
            onSaveGoal = { title, category, frequency, targetCo2, reminderEnabled ->
                if (goalToEdit != null) {
                    viewModel.updateGoal(
                        goalId = goalToEdit!!.id,
                        newText = title,
                        category = category,
                        frequency = frequency,
                        targetCo2 = targetCo2,
                        reminderEnabled = reminderEnabled
                    )
                } else {
                    viewModel.addGoal(
                        title = title,
                        category = category,
                        frequency = frequency,
                        targetCo2 = targetCo2,
                        reminderEnabled = reminderEnabled
                    )
                }
                showAddScreen = false
                goalToEdit = null
            },
            onDeleteGoal = { goal ->
                viewModel.deleteGoal(goal.id)
                showAddScreen = false
                goalToEdit = null
            }
        )
    } else {
        Scaffold(
            containerColor = EcoBackground,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        goalToEdit = null
                        showAddScreen = true
                    },
                    containerColor = EcoPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Goal",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Section
                item {
                    Text(
                        text = "Climate Goals",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E232A)
                    )
                    Text(
                        text = "Track your daily environmental impact & align with weather.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Streak Card Section
                item {
                    StreakCard(
                        streakCount = userStreak.currentStreak
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Category Filter Chips Row
                item {
                    val categories = listOf("All", "Transport", "Energy", "Food", "Waste", "Water", "Nature")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategoryFilter.equals(cat, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) EcoPrimary else Color.White)
                                    .clickable { selectedCategoryFilter = cat }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Active Goals Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategoryFilter == "All") "Active Goals" else "$selectedCategoryFilter Goals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E232A)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PillBackground)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${filteredActiveGoals.size} Remaining",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF555555),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Empty State for Active Goals
                if (filteredActiveGoals.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (completedGoals.isNotEmpty()) Icons.Outlined.CheckCircle else Icons.Outlined.Eco,
                                    contentDescription = null,
                                    tint = if (completedGoals.isNotEmpty()) Color(0xFF16A34A) else EcoPrimary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (completedGoals.isNotEmpty()) "All goals completed today!" else "No active goals yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1E232A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (completedGoals.isNotEmpty())
                                        "Awesome work taking climate action today! 🎉"
                                    else
                                        "Tap the '+' button below to set your first climate goal.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                } else {
                    // Active Items with Pen/Edit & Delete Buttons
                    items(
                        items = filteredActiveGoals,
                        key = { goal: GoalEntity -> goal.id }
                    ) { goal: GoalEntity ->
                        GoalItem(
                            goal = goal,
                            onCheckedChange = { isChecked: Boolean ->
                                viewModel.toggleGoalCompletion(goal.id, isChecked)
                            },
                            onEditClick = {
                                goalToEdit = goal
                            },
                            onDeleteClick = {
                                goalToDelete = goal
                            }
                        )
                    }
                }

                // Completed Goals Section Header & Items
                if (completedGoals.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Completed Today (${completedGoals.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E232A)
                            )
                            if (totalCo2SavedKg > 0.0) {
                                Text(
                                    text = "~${String.format(Locale.US, "%.1f", totalCo2SavedKg)} kg CO₂ saved",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    items(
                        items = completedGoals,
                        key = { goal: GoalEntity -> goal.id }
                    ) { goal: GoalEntity ->
                        GoalItem(
                            goal = goal,
                            onCheckedChange = { isChecked: Boolean ->
                                viewModel.toggleGoalCompletion(goal.id, isChecked)
                            },
                            onDeleteClick = {
                                goalToDelete = goal
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreakCard(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(LightBlueIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = EcoPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (streakCount == 0) "Start Your Streak" else "$streakCount Day Streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E232A)
                )
                Text(
                    text = if (streakCount == 0) "Complete a goal today to start your streak!" else "You're on fire! Keep it up.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
                val progressValue = (streakCount.coerceIn(0, 7) / 7f)

                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFE5E7EB),
                    strokeWidth = 5.dp
                )
                CircularProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier.fillMaxSize(),
                    color = EcoPrimary,
                    strokeWidth = 5.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "$streakCount/7",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EcoPrimary
                )
            }
        }
    }
}

@Composable
fun GoalItem(
    goal: GoalEntity,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val (catBg, catText) = getCategoryStyle(goal.category)
    val (freqBg, freqText) = getFrequencyStyle(goal.frequency)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!goal.isCompleted) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = goal.isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = EcoPrimary,
                    uncheckedColor = Color(0xFFB0BEC5)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.text,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (goal.isCompleted) Color(0xFF9E9E9E) else Color(0xFF263238)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Tag
                    GoalTag(
                        label = goal.category,
                        bgColor = catBg,
                        textColor = catText
                    )

                    // Frequency Tag
                    GoalTag(
                        label = goal.frequency,
                        bgColor = freqBg,
                        textColor = freqText
                    )

                    // CO2 Target Tag
                    if (goal.targetCo2.isNotBlank()) {
                        GoalTag(
                            label = "🌱 ${goal.targetCo2}",
                            bgColor = ChipCo2Bg,
                            textColor = ChipCo2Text
                        )
                    }
                }

                // Weather Recommendation Tag
                if (!goal.isCompleted && !goal.weatherCondition.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    GoalTag(
                        label = goal.weatherCondition,
                        bgColor = Color(0xFFF0FDF4),
                        textColor = Color(0xFF15803D)
                    )
                }
            }

            // Edit & Delete Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onEditClick != null && !goal.isCompleted) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Goal",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (onDeleteClick != null) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete Goal",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryStyle(category: String): Pair<Color, Color> = when (category.lowercase()) {
    "transport" -> Color(0xFFD1E8F7) to Color(0xFF1E5275)
    "energy" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
    "food" -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
    "waste" -> Color(0xFFF1F5F9) to Color(0xFF475569)
    "water" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
    "nature" -> Color(0xFFDCFCE7) to Color(0xFF166534)
    else -> Color(0xFFE2E8F0) to Color(0xFF334155)
}

private fun getFrequencyStyle(frequency: String): Pair<Color, Color> = when (frequency.lowercase()) {
    "weekly" -> ChipWeeklyBg to ChipWeeklyText
    "monthly" -> Color(0xFF475569) to Color.White
    else -> ChipDailyBg to ChipDailyText
}

@Composable
fun GoalTag(
    label: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
