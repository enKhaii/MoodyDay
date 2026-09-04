package com.example.moodyday.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@Composable
fun AppTopBar(
    streakCount: Int,
    onSettingsClick: () -> Unit,
    onStreakClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding() // Keeps the TopBar below the SYSTEM UI
            .background(Color(0xFFF1F7FB))
            .padding(horizontal = 16.dp, vertical = 4.dp)

    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onStreakClick() }
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = if (streakCount > 0) Color(0xFFEA580C) else Color(0xFF94A3B8),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$streakCount",
                fontWeight = FontWeight.Bold,
                color = if (streakCount > 0) Color(0xFF1E293B) else Color(0xFF94A3B8)
            )
        }

        Text(
            text = "MoodyDay",
            fontSize = 17.sp,
            color = Color(0xFF006494),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}