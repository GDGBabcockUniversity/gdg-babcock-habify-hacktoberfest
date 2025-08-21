package com.example.habify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.PatternsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNeedsOnboarding: () -> Unit, // Added missing parameter
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Form validation
    val isFormValid = !emailError && email.isNotBlank() && password.isNotBlank()

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back",
                fontSize = 24.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = it.isNotBlank() && !PatternsCompat.EMAIL_ADDRESS.matcher(it).matches()
                },
                isError = emailError,
                label = { Text("Email") },
                colors = textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            if (emailError) {
                Text("Invalid email format", color = Color.Red, fontSize = 12.sp)
            }

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                colors = textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        isLoading = true
                        val auth = FirebaseAuth.getInstance()
                        val db = FirebaseFirestore.getInstance()

                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = task.result?.user
                                    if (user != null) {
                                        // Check if user has completed onboarding
                                        db.collection("users").document(user.uid)
                                            .get()
                                            .addOnSuccessListener { doc ->
                                                isLoading = false
                                                val hasOnboarded = doc.getBoolean("hasOnboarded") ?: false
                                                if (hasOnboarded) {
                                                    onLoginSuccess() // goes to Home
                                                } else {
                                                    onNeedsOnboarding() // goes to CreateHabit
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                isLoading = false
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        "Failed to check user data: ${exception.message}"
                                                    )
                                                }
                                            }
                                    } else {
                                        isLoading = false
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Login failed: User data unavailable")
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    val message = task.exception?.localizedMessage ?: "Login failed"
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.4f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Log In")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToSignUp) {
                Text("Don't have an account? Sign up", color = Color.White, fontSize = 14.sp)
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