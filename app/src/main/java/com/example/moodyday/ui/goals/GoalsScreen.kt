package com.example.moodyday.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodyday.data.local.entities.GoalEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = viewModel(),
    onProfileClick: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var newGoalTitle by remember { mutableStateOf("") }

    val userStreak by viewModel.userStreak.collectAsState()
    val allGoals by viewModel.goals.collectAsState()

    val activeGoals = allGoals.filter { !it.isCompleted }
    val completedGoals = allGoals.filter { it.isCompleted }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Goal") },
                text = { Text("Add Goal") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Climate Goals", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Track your daily environmental impact.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(40.dp), shape = CircleShape) {
                            Icon(Icons.Default.Person, contentDescription = "Avatar", modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Alex River", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Seattle, WA", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("🔥 ${userStreak.currentStreak} Day Streak", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("You're on fire! Keep it up.", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Active Goals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${activeGoals.size} Remaining", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(activeGoals) { goal ->
                    GoalItem(
                        goal = goal,
                        onCheckedChange = { isChecked ->
                            viewModel.toggleGoalCompletion(goal.id, isChecked)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Completed Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(completedGoals) { goal ->
                    GoalItem(
                        goal = goal,
                        onCheckedChange = { isChecked ->
                            viewModel.toggleGoalCompletion(goal.id, isChecked)
                        }
                    )
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Goal") },
                text = {
                    OutlinedTextField(
                        value = newGoalTitle,
                        onValueChange = { newGoalTitle = it },
                        label = { Text("Goal Title") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addGoal(newGoalTitle)
                        newGoalTitle = ""
                        showDialog = false
                    }) { Text("Add") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun GoalItem(goal: GoalEntity, onCheckedChange: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = goal.isCompleted, onCheckedChange = onCheckedChange)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(goal.text, style = MaterialTheme.typography.bodyLarge)
                if (!goal.weatherCondition.isNullOrEmpty()) {
                    Text(
                        text = goal.weatherCondition,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}