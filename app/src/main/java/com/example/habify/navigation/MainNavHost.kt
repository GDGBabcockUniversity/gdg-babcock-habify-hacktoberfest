// MainNavHost.kt
package com.example.habify.navigation

//import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.habify.ui.screens.*

@Composable
fun MainNavHost(
    startDestination: String = "home",
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("friends", "Friends", Icons.Default.Group),
        BottomNavItem("addHabit", "Add Habit", Icons.Default.Add),
        BottomNavItem("profile", "Profile", Icons.Default.Person),
        BottomNavItem("settings", "Settings", Icons.Default.Settings)
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (currentRoute == item.route) Color(0xFF00C853) else Color.White
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                color = if (currentRoute == item.route) Color(0xFF00C853) else Color.White
                            )
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00C853),
                            selectedTextColor = Color(0xFF00C853),
                            unselectedIconColor = Color.White,
                            unselectedTextColor = Color.White,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("friends") { FriendsScreen() }
            composable("addHabit") {
                AddHabitScreen(
                    onHabitSaved = {
                        // After successfully saving a habit, navigate back to home
                        navController.navigate("home") {
                            popUpTo("addHabit") { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        // Handle back navigation when user taps close button
                        navController.popBackStack()
                    }
                )
            }
            composable("profile") { ProfileScreen {  } }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = onLogout,
                    // Add other callbacks as needed
                    onEditProfile = { /* TODO: Navigate to edit profile */ },
                    onChangePassword = { /* TODO: Navigate to change password */ },
                    onDeleteAccount = { /* TODO: Handle account deletion */ },
                    onHelpCenter = { /* TODO: Navigate to help center */ },
                    onContactUs = { /* TODO: Navigate to contact us */ }
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)