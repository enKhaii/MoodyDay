package com.example.moodyday.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodyday.R
import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo Container
            Image(
                painter = painterResource(id = R.drawable.ic_weather_logo),
                contentDescription = "MoodyDay Logo",
                modifier = Modifier.size(92.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to access your weather tips",
                fontSize = 15.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = ""
                        },
                        placeholder = { Text("Email Address", color = Color(0xFF94A3B8)) },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF1E293B),
                            focusedBorderColor = Color(0xFF003D61),
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF1F5F9)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        placeholder = { Text("Password", color = Color(0xFF94A3B8)) },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF1E293B),
                            focusedBorderColor = Color(0xFF003D61),
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF1F5F9)
                        )
                    )

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    supabase.auth.signInWith(Email) {
                                        this.email = email
                                        this.password = password
                                    }
                                    onNavigateToHome()
                                } catch (e: Exception) {
                                    errorMessage = e.localizedMessage ?: "Invalid credentials"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003D61))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Footer Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Don't have an account?", color = Color(0xFF64748B))
                TextButton(onClick = onNavigateToRegister) {
                    Text("Sign Up", color = Color(0xFF003D61), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}