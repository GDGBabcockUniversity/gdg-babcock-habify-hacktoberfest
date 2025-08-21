package com.example.habify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@Composable
fun CreateHabitScreen(onHabitCreated: () -> Unit) {
    var habitName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Form validation
    val isFormValid = habitName.isNotBlank() && habitName.trim().length >= 2

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Create Your First Habit",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "What habit would you like to build?",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("Habit name", color = Color.White) },
                placeholder = { Text("e.g., Drink 8 glasses of water", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors(),
                shape = MaterialTheme.shapes.medium
            )

            if (habitName.isNotBlank() && habitName.trim().length < 2) {
                Text(
                    "Habit name must be at least 2 characters",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        isLoading = true
                        val auth = FirebaseAuth.getInstance()
                        val db = FirebaseFirestore.getInstance()

                        auth.currentUser?.let { user ->
                            // Create the habit document
                            val habitData = hashMapOf(
                                "name" to habitName.trim(),
                                "userId" to user.uid,
                                "createdAt" to Timestamp.now(),
                                "isActive" to true,
                                "streak" to 0,
                                "totalCompletions" to 0
                            )

                            db.collection("habits")
                                .add(habitData)
                                .addOnSuccessListener { habitDoc ->
                                    // Update user's onboarding status
                                    db.collection("users").document(user.uid)
                                        .update("hasOnboarded", true)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            onHabitCreated() // Navigate to main app
                                        }
                                        .addOnFailureListener { exception ->
                                            isLoading = false
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Failed to complete setup: ${exception.message}"
                                                )
                                            }
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    isLoading = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Failed to create habit: ${exception.message}"
                                        )
                                    }
                                }
                        } ?: run {
                            isLoading = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("User not authenticated")
                            }
                        }
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853),
                    disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.4f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        "Create Habit",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF1C1C1E),
    unfocusedContainerColor = Color(0xFF1C1C1E),
    disabledContainerColor = Color(0xFF1C1C1E),

    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,

    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.White,
    disabledLabelColor = Color.Gray,

    cursorColor = Color.White,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledTextColor = Color.Gray
)