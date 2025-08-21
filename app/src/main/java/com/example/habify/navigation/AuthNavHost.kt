// AuthNavHost.kt
package com.example.habify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.habify.ui.screens.CreateHabitScreen
import com.example.habify.ui.screens.LoginScreen
import com.example.habify.ui.screens.SignUpScreen
import com.example.habify.ui.screens.WelcomeScreen

@Composable
fun AuthNavHost(onAuthComplete: (Boolean) -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {

        composable("welcome") {
            WelcomeScreen(onGetStarted = {
                navController.navigate("signup")
            })
        }

        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {
                    // New users go to create habit (onboarding)
                    navController.navigate("createHabit") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate("login")
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Existing users with onboarding complete go to home
                    onAuthComplete(false) // false = go to home
                },
                onNeedsOnboarding = {
                    // Existing users without onboarding go to create habit
                    navController.navigate("createHabit") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }

        composable("createHabit") {
            CreateHabitScreen(
                onHabitCreated = {
                    // After habit creation, mark onboarding as complete
                    onAuthComplete(true) // true = new user completed onboarding
                }
            )
        }
    }
}