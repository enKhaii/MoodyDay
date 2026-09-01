package com.example.moodyday.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var homeCity by remember { mutableStateOf("Alor Setar, Kedah") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("User Profile", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                modifier = Modifier.padding(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = homeCity,
            onValueChange = { homeCity = it },
            label = { Text("Home City") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* TODO: Save user profile data (name and home city) to database */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { /* TODO: Implement password change logic via Supabase Auth */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Password")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                coroutineScope.launch {
                    try {
                        supabase.auth.signOut()
                        onNavigateToLogin()
                    } catch (e: Exception) {
                    }
                }
            }
        ) {
            Text("Logout", color = MaterialTheme.colorScheme.error)
        }
    }
}