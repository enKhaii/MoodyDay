package com.example.moodyday.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen() {
    var showDialog by remember { mutableStateOf(false) }
    var newGoalTitle by remember { mutableStateOf("") }

    val goals = remember { mutableStateOf(listOf("Reduce Plastic Usage", "Plant a Tree", "Conserve Water")) }

    val recommendedActionTime = suggestBestTime(currentWeatherCondition = "Sunny", goalType = "Outdoor")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Climate Action Goals") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Streak: 🔥 5 Days", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("AI Weather Suggestion: $recommendedActionTime", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Your Active Goals", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(8.dp))

            // 目标列表
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(goals.value) { goal ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = goal, style = MaterialTheme.typography.bodyLarge)
                            Checkbox(checked = false, onCheckedChange = { /* TODO: Mark goal as completed */ })
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add New Climate Goal") },
                text = {
                    OutlinedTextField(
                        value = newGoalTitle,
                        onValueChange = { newGoalTitle = it },
                        label = { Text("Goal Description") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newGoalTitle.isNotBlank()) {
                            goals.value = goals.value + newGoalTitle
                            newGoalTitle = ""
                            showDialog = false
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

fun suggestBestTime(currentWeatherCondition: String, goalType: String): String {
    return when {
        currentWeatherCondition.contains("Sunny", ignoreCase = true) ->
            "Best time for outdoor tree planting: Early morning (7:00 AM - 9:00 AM)"
        currentWeatherCondition.contains("Rain", ignoreCase = true) ->
            "Great time for indoor energy saving and recycling sorting!"
        else ->
            "Optimal time for climate action: Today between 4:00 PM - 6:00 PM"
    }
}