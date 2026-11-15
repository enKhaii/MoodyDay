package com.example.moodyday.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodyday.data.local.entities.GoalEntity

private val BrandPrimary = Color(0xFF003D61)
private val LightBg = Color(0xFFF3F7FA)
private val CardBorderColor = Color(0xFFE2E8F0)
private val TextDark = Color(0xFF1E293B)
private val TextSubtle = Color(0xFF64748B)

data class CategoryItem(val name: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    initialGoal: GoalEntity? = null,
    onDismiss: () -> Unit,
    onSaveGoal: (title: String, category: String, frequency: String, targetCo2: String, reminderEnabled: Boolean) -> Unit,
    onDeleteGoal: ((GoalEntity) -> Unit)? = null
) {
    val isEditing = initialGoal != null
    var goalName by remember { mutableStateOf(initialGoal?.text ?: "") }
    var selectedCategory by remember { mutableStateOf(initialGoal?.category ?: "Transport") }
    var selectedFrequency by remember { mutableStateOf(initialGoal?.frequency ?: "Daily") }

    // Parse initial targetCo2 into numeric target and unit
    val initialTargetParts = remember(initialGoal) {
        val raw = initialGoal?.targetCo2 ?: "2.0 kg CO₂"
        val parts = raw.split(" ", limit = 2)
        val num = parts.getOrNull(0) ?: "2.0"
        val unit = parts.getOrNull(1) ?: "kg CO₂"
        num to unit
    }
    var co2Target by remember { mutableStateOf(initialTargetParts.first) }
    var selectedUnit by remember { mutableStateOf(initialTargetParts.second) }
    var reminderEnabled by remember { mutableStateOf(initialGoal?.reminderEnabled ?: false) }

    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categoriesRow1 = listOf(
        CategoryItem("Transport", Icons.Outlined.DirectionsBus),
        CategoryItem("Energy", Icons.Outlined.Bolt),
        CategoryItem("Food", Icons.Outlined.Restaurant)
    )
    val categoriesRow2 = listOf(
        CategoryItem("Waste", Icons.Outlined.DeleteOutline),
        CategoryItem("Water", Icons.Outlined.WaterDrop),
        CategoryItem("Nature", Icons.Outlined.Park)
    )

    val frequencies = listOf("Daily", "Weekly", "Monthly")
    val units = listOf("kg CO₂", "g CO₂", "lbs CO₂", "tons CO₂")

    val categorySuggestions = remember(selectedCategory) {
        when (selectedCategory.lowercase()) {
            "transport" -> listOf("Take bus to work", "Bike for short trips", "Carpool with friends")
            "energy" -> listOf("Air-dry laundry today", "Turn off AC when leaving", "Unplug idle appliances")
            "food" -> listOf("Eat a plant-based lunch", "Zero food waste today", "Buy local produce")
            "waste" -> listOf("Bring reusable bag", "Say no to plastic straws", "Compost organic waste")
            "water" -> listOf("Take a 5-minute shower", "Turn off tap while brushing", "Water plants with runoff")
            "nature" -> listOf("Plant a native shrub", "Tend to balcony plants", "Join community cleanup")
            else -> listOf("Take stairs instead of lift", "Turn off standby power", "Use refillable bottle")
        }
    }

    val categoryDescription = remember(selectedCategory) {
        when (selectedCategory.lowercase()) {
            "transport" -> "Estimated greenhouse gas offset by commuting via public transit, walking, or cycling."
            "energy" -> "Estimated emissions saved by conserving electricity and optimizing indoor appliances."
            "food" -> "Estimated carbon footprint avoided through plant-based choices and zero food waste."
            "waste" -> "Estimated emissions prevented by recycling, composting, and ditching single-use items."
            "water" -> "Estimated water-heating energy and municipal treatment emissions conserved."
            "nature" -> "Estimated atmospheric carbon sequestered and biodiversity supported."
            else -> "Estimated greenhouse gas offset by adopting this eco-friendly habit."
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("About Climate Goals") },
            text = {
                Text(
                    "Setting Climate Goals helps you reduce your carbon footprint and align daily habits with weather conditions.\n\n" +
                            "• Outdoor tasks automatically get optimal weather recommendations.\n" +
                            "• Track CO₂ reduction to see your real environmental impact.\n" +
                            "• Maintain your streak by completing at least one goal every day!"
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    if (showDeleteConfirmDialog && initialGoal != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Goal?") },
            text = { Text("Are you sure you want to delete \"${initialGoal.text}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteGoal?.invoke(initialGoal)
                        onDismiss()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = LightBg,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Goal" else "Add New Goal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Close",
                            tint = TextDark
                        )
                    }
                },
                actions = {
                    if (isEditing && onDeleteGoal != null) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete Goal",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Help",
                            tint = TextDark
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBg)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        if (goalName.isBlank()) {
                            errorMessage = "Please enter a goal name"
                            return@Button
                        }
                        val co2Formatted = "$co2Target $selectedUnit".trim()
                        onSaveGoal(
                            goalName.trim(),
                            selectedCategory,
                            selectedFrequency,
                            co2Formatted,
                            reminderEnabled
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Save Changes" else "Add Goal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text(
                    text = "CUSTOM ECO GOAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEditing) "Modify your current goal\nand targets" else "Reduce your footprint, one habit\nat a time",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    lineHeight = 28.sp
                )
            }

            // Category Section
            Column {
                Text(text = "Category", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoriesRow1.forEach { cat ->
                        CategoryPill(
                            item = cat,
                            isSelected = selectedCategory == cat.name,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = cat.name
                                if (!isEditing) {
                                    co2Target = when (cat.name) {
                                        "Transport" -> "2.0"
                                        "Energy" -> "1.5"
                                        "Food" -> "1.8"
                                        else -> "1.0"
                                    }
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoriesRow2.forEach { cat ->
                        CategoryPill(
                            item = cat,
                            isSelected = selectedCategory == cat.name,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = cat.name
                                if (!isEditing) {
                                    co2Target = when (cat.name) {
                                        "Waste" -> "0.8"
                                        "Water" -> "0.5"
                                        "Nature" -> "5.0"
                                        else -> "1.0"
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Goal Name Section
            Column {
                Text(text = "Goal Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(
                            1.dp,
                            if (errorMessage != null) MaterialTheme.colorScheme.error else CardBorderColor,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = if (errorMessage != null) MaterialTheme.colorScheme.error else TextSubtle,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = goalName,
                        onValueChange = {
                            goalName = it
                            if (it.isNotBlank()) errorMessage = null
                        },
                        textStyle = TextStyle(fontSize = 15.sp, color = TextDark),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (goalName.isEmpty()) {
                                Text("e.g. Bike to work or class", color = Color(0xFFA0AEC0), fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    )
                }
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Suggestions for $selectedCategory:", fontSize = 12.sp, color = TextSubtle, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categorySuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = {
                                goalName = suggestion
                                errorMessage = null
                            },
                            label = {
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color.White,
                                labelColor = BrandPrimary
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = Color(0xFFCCE4EC)
                            )
                        )
                    }
                }
            }

            // Frequency Section
            Column {
                Text(text = "Frequency", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(4.dp)
                ) {
                    frequencies.forEach { freq ->
                        val isSelected = selectedFrequency == freq
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) BrandPrimary else Color.Transparent)
                                .clickable { selectedFrequency = freq }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = freq,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSubtle
                            )
                        }
                    }
                }
            }

            // CO2 Target Section
            Column {
                Text(text = "CO₂ Reduction Target", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        BasicTextField(
                            value = co2Target,
                            onValueChange = { co2Target = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                .clickable { unitDropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = selectedUnit, fontSize = 15.sp, color = TextDark, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select unit", tint = TextSubtle)
                        }

                        DropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedUnit = unit
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = categoryDescription,
                    fontSize = 12.sp,
                    color = TextSubtle,
                    lineHeight = 16.sp
                )
            }

            // Daily Reminder Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Reminder",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Nudge me to log this activity at 8:00 PM",
                            fontSize = 12.sp,
                            color = TextSubtle
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPrimary,
                            uncheckedTrackColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CategoryPill(
    item: CategoryItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFFD6EEF3) else Color.White)
            .border(
                1.dp,
                if (isSelected) BrandPrimary else CardBorderColor,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (isSelected) BrandPrimary else TextSubtle,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = item.name,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BrandPrimary else TextDark
        )
    }
}