// MainActivity.kt
package com.example.habify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.habify.navigation.AuthNavHost
import com.example.habify.navigation.MainNavHost
import com.example.habify.ui.components.StarFieldProvider
import com.example.habify.ui.components.StarryBackground
import com.example.habify.ui.components.rememberStarFieldState
import com.example.habify.ui.theme.HabifyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabifyTheme {
                val stars = rememberStarFieldState()
                var authState by remember { mutableStateOf<AuthState>(AuthState.Loading) }
                val scope = rememberCoroutineScope()

                // Check authentication state on app start
                LaunchedEffect(Unit) {
                    scope.launch {
                        checkAuthState { state ->
                            authState = state
                        }
                    }
                }

                // Listen for Firebase auth state changes
                LaunchedEffect(Unit) {
                    val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                        if (auth.currentUser == null) {
                            // User signed out, reset to unauthenticated state
                            authState = AuthState.Unauthenticated
                        }
                    }
                    FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
                }

                StarFieldProvider(stars) {
                    StarryBackground {
                        when (authState) {
                            AuthState.Loading -> {
                                // Show loading screen
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF00C853)
                                    )
                                }
                            }

                            AuthState.Unauthenticated -> {
                                AuthNavHost { isNewUser ->
                                    authState = if (isNewUser) {
                                        AuthState.AuthenticatedWithoutOnboarding
                                    } else {
                                        AuthState.AuthenticatedWithOnboarding
                                    }
                                }
                            }

                            AuthState.AuthenticatedWithOnboarding -> {
                                MainNavHost(
                                    startDestination = "home",
                                    onLogout = {
                                        // Reset auth state to trigger navigation back to welcome
                                        authState = AuthState.Unauthenticated
                                    }
                                )
                            }

                            AuthState.AuthenticatedWithoutOnboarding -> {
                                MainNavHost(
                                    startDestination = "addHabit",
                                    onLogout = {
                                        // Reset auth state to trigger navigation back to welcome
                                        authState = AuthState.Unauthenticated
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun checkAuthState(onResult: (AuthState) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onResult(AuthState.Unauthenticated)
            return
        }

        try {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val hasOnboarded = userDoc.getBoolean("hasOnboarded") ?: false

            onResult(
                if (hasOnboarded) {
                    AuthState.AuthenticatedWithOnboarding
                } else {
                    AuthState.AuthenticatedWithoutOnboarding
                }
            )
        } catch (e: Exception) {
            // If we can't check onboarding status, assume they need to complete it
            onResult(AuthState.AuthenticatedWithoutOnboarding)
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    object AuthenticatedWithOnboarding : AuthState()
    object AuthenticatedWithoutOnboarding : AuthState()
}