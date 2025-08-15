package com.example.habify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.habify.ui.components.StarryBackground
import com.example.habify.ui.screens.CreateHabitScreen
import com.example.habify.ui.screens.LoginScreen
import com.example.habify.ui.screens.SignUpScreen
import com.example.habify.ui.screens.WelcomeScreen

@Composable
fun AuthNavHost(onAuthComplete: () -> Unit) {
    val navController = rememberNavController()

    // Wrap the entire NavHost with StarryBackground
    StarryBackground {
        NavHost(navController = navController, startDestination = "welcome") {

            composable("welcome") {
                WelcomeScreen(onGetStarted = {
                    navController.navigate("signup")
                })
            }

            composable("signup") {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate("createHabit")
                    },
                    onLoginClick = {
                        navController.navigate("login")
                    }
                )
            }

            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("createHabit")
                    }
                )
            }

            composable("createHabit") {
                CreateHabitScreen(
                    onHabitCreated = {
                        onAuthComplete()
                    }
                )
            }
        }
    }
}
