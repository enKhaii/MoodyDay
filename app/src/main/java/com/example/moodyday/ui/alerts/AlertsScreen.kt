package com.example.moodyday.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodyday.data.local.entities.AlertRuleEntity

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF003D61)
                )
            }
            state.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error loading alerts", fontWeight = FontWeight.Bold, color = Color(0xFF003D61))
                    Text(state.error ?: "Unknown error", textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Weather Alerts", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text("Stay informed and prepared for extreme conditions.", fontSize = 14.sp, color = Color(0xFF475569))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Global Notifications", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                            Switch(
                                checked = state.globalNotificationsEnabled,
                                onCheckedChange = { viewModel.toggleGlobalNotifications(it) },
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }

                    item {
                        Text("Active Alerts (${state.activeAlerts.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }

                    items(state.activeAlerts) { alert ->
                        ActiveAlertCard(alert)
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Custom Alert Rules", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003D61))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Rule", fontSize = 12.sp)
                            }
                        }
                    }

                    items(state.customRules) { rule ->
                        CustomRuleItem(
                            rule = rule,
                            onToggle = { viewModel.toggleRule(rule) },
                            onDelete = { viewModel.deleteRule(rule) }
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp).navigationBarsPadding()) }
                }
            }
        }

        if (showAddDialog) {
            AddRuleDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { condition, threshold ->
                    viewModel.addRule(condition, threshold)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ActiveAlertCard(alert: ActiveAlert) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            if (alert.severity.contains("HIGH")) Color(0xFFFFEBEE) else Color(0xFFFFF3E0),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (alert.severity.contains("HIGH")) Color.Red else Color(0xFFF57C00),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            alert.severity,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alert.severity.contains("HIGH")) Color.Red else Color(0xFFF57C00)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(alert.endsIn, fontSize = 10.sp, color = Color.Gray)
            }
            
            Spacer(Modifier.height(12.dp))
            Text(alert.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(alert.description, fontSize = 13.sp, color = Color.DarkGray)
            
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = { /* View tips */ },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("View Safety Tips", fontSize = 12.sp, color = Color(0xFF003D61))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF003D61))
            }
        }
    }
}

@Composable
fun CustomRuleItem(rule: AlertRuleEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when {
                rule.condition.contains("Temp", true) -> Icons.Default.Thermostat
                rule.condition.contains("Rain", true) -> Icons.Default.WaterDrop
                else -> Icons.Default.Info
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF003D61))
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(rule.condition, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Notify if ${rule.condition.lowercase()} > ${rule.threshold.toInt()}°C", fontSize = 12.sp, color = Color.Gray)
            }
            
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.scale(0.8f)
            )
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AddRuleDialog(onDismiss: () -> Unit, onAdd: (String, Double) -> Unit) {
    var condition by remember { mutableStateOf("Temperature spike") }
    var threshold by remember { mutableStateOf("35") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Alert Rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Condition (e.g. Temperature spike)") }
                )
                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it },
                    label = { Text("Threshold Value") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(condition, threshold.toDoubleOrNull() ?: 0.0) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
