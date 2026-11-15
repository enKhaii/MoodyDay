package com.example.moodyday.ui.alerts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodyday.R
import com.example.moodyday.data.local.entities.AlertRuleEntity
import com.example.moodyday.ui.weather.SelectedCityViewModel

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel(),
    selectedCityViewModel: SelectedCityViewModel,
    onNavigateToTips: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val selectedCity by selectedCityViewModel.selectedCity.collectAsState()

    LaunchedEffect(key1 = selectedCity) {
        viewModel.loadAlertsForCity(selectedCity.lat, selectedCity.lon)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
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
                        ActiveAlertCard(
                            alert = alert,
                            onViewTipsClick = onNavigateToTips
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Custom Alert Rules", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003D61))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Add Rule",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (state.customRules.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    state.customRules.forEachIndexed { index, rule ->
                                        CustomRuleRow(
                                            rule = rule,
                                            onToggle = { viewModel.toggleRule(rule) },
                                            onDelete = { viewModel.deleteRule(rule) }
                                        )
                                        if (index < state.customRules.lastIndex) {
                                            androidx.compose.material3.HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                thickness = 1.dp,
                                                color = Color(0xFFF1F5F9)
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
fun ActiveAlertCard(alert: ActiveAlert, onViewTipsClick: () -> Unit) {
    val isHighSeverity = alert.severity.contains("HIGH", ignoreCase = true)

    val stripeColor = if (isHighSeverity) Color(0xFFD32F2F) else Color(0xFFF57C00)
    val pillBgColor = if (isHighSeverity) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
    val textColor = Color(0xFF1E293B)
    val bodyColor = Color(0xFF475569)

    // The dynamic icon based on the alert type
    val imagePainter: Painter = when {
        alert.title.contains("Extreme Heat", ignoreCase = true) ||
                alert.severity.contains("HIGH", ignoreCase = true) ->
            painterResource(id = R.drawable.outline_local_fire_department_24)
        alert.title.contains("Heat Advisory", ignoreCase = true) ->
            painterResource(id = R.drawable.baseline_sunny_24)
        alert.title.contains("Wind", ignoreCase = true) ->
            painterResource(id = R.drawable.outline_air_24)
        alert.title.contains("Rain", ignoreCase = true) ->
            painterResource(id = R.drawable.outline_water_drop_24)
        else -> painterResource(id = R.drawable.baseline_warning_amber_24)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 18.dp, y = (-12).dp)
                    .size(90.dp)
                    .alpha(0.08f),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(stripeColor)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                // Left severity stripe
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(stripeColor)
                )

                // Main Content Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp, top = 14.dp, end = 16.dp, bottom = 14.dp)
                ) {

                    // 1. Header Row (Icon + Pill + Ends In text)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Icon outside the pill
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = stripeColor,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Rounded Severity Pill
                        Box(
                            modifier = Modifier
                                .background(color = pillBgColor, shape = RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = alert.severity.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = stripeColor
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = alert.endsIn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = bodyColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Title
                    Text(
                        text = alert.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Description
                    Text(
                        text = alert.description,
                        fontSize = 14.sp,
                        color = bodyColor,
                        lineHeight = 20.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Action Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onViewTipsClick() }
                    ) {
                        Text(
                            text = "View Safety Tips",
                            fontSize = 13.sp,
                            color = Color(0xFF006494),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF006494),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomRuleRow(
    rule: AlertRuleEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isRain = rule.condition.contains("Rain", ignoreCase = true) || rule.condition.contains("Precip", ignoreCase = true)
    val isWind = rule.condition.contains("Wind", ignoreCase = true)
    val isTemp = rule.condition.contains("Temp", ignoreCase = true)

    // Dynamic icon selection
    val icon = when {
        isRain -> Icons.Default.WaterDrop
        isWind -> Icons.Default.Info
        else -> Icons.Default.Thermostat
    }

    // Dynamic description with correct units
    val subtitle = when {
        isRain -> "Notify if precip prob > ${rule.threshold.toInt()}%"
        isWind -> "Notify if wind speed > ${rule.threshold.toInt()} km/h"
        isTemp -> "Notify if temp > ${rule.threshold.toInt()}°C"
        else -> "Notify if condition > ${rule.threshold.toInt()}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Icon Badge
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFDDF1F8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF004D70),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title and formatted subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rule.condition,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }

        // Switch styled to match the dark blue toggle
        Switch(
            checked = rule.isEnabled,
            onCheckedChange = { onToggle() },
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF003D61),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1),
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.scale(0.85f)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Trash action icon
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Rule",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun AddRuleDialog(onDismiss: () -> Unit, onAdd: (String, Double) -> Unit) {
    var condition by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Custom Rule",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = "Get notified when the weather hits your specific limits.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Condition (e.g. Temp, Rain)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E293B),
                        unfocusedTextColor = Color(0xFF1E293B),
                        focusedBorderColor = Color(0xFF003D61),
                        focusedLabelColor = Color(0xFF003D61),
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it },
                    label = { Text("Threshold Value") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E293B),
                        unfocusedTextColor = Color(0xFF1E293B),
                        focusedBorderColor = Color(0xFF003D61),
                        focusedLabelColor = Color(0xFF003D61),
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (condition.isNotBlank() && threshold.isNotBlank()) {
                                onAdd(condition, threshold.toDoubleOrNull() ?: 0.0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003D61)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Rule", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}