package com.example.habify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onHelpCenter: () -> Unit = {},
    onContactUs: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var isDarkTheme by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var friendRequestsEnabled by remember { mutableStateOf(true) }
    var habitUpdatesEnabled by remember { mutableStateOf(true) }
    var remindersEnabled by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Preferences Section
            item {
                SectionHeader("Preferences")
            }

            item {
                SettingsToggleItem(
                    title = "Theme",
                    subtitle = "Dark",
                    icon = Icons.Default.Palette,
                    isChecked = isDarkTheme,
                    onCheckedChange = { isDarkTheme = it }
                )
            }

            item {
                SectionHeader("Notifications")
            }

            item {
                SettingsToggleItem(
                    title = "Friend Requests",
                    subtitle = "Receive notifications for new friend requests",
                    icon = Icons.Default.PersonAdd,
                    isChecked = friendRequestsEnabled,
                    onCheckedChange = { friendRequestsEnabled = it }
                )
            }

            item {
                SettingsToggleItem(
                    title = "Habit Updates",
                    subtitle = "Receive notifications for habit updates from friends",
                    icon = Icons.Default.Notifications,
                    isChecked = habitUpdatesEnabled,
                    onCheckedChange = { habitUpdatesEnabled = it }
                )
            }

            item {
                SettingsToggleItem(
                    title = "Reminders",
                    subtitle = "Receive reminders for your habits",
                    icon = Icons.Default.Schedule,
                    isChecked = remindersEnabled,
                    onCheckedChange = { remindersEnabled = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Account Section
            item {
                SectionHeader("Account")
            }

            item {
                SettingsClickableItem(
                    title = "Edit Profile",
                    icon = Icons.Default.Person,
                    onClick = onEditProfile
                )
            }

            item {
                SettingsClickableItem(
                    title = "Change Password",
                    icon = Icons.Default.Lock,
                    onClick = onChangePassword
                )
            }

            item {
                SettingsClickableItem(
                    title = "Delete Account",
                    icon = Icons.Default.Delete,
                    onClick = onDeleteAccount,
                    textColor = Color.Red
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Support Section
            item {
                SectionHeader("Support")
            }

            item {
                SettingsClickableItem(
                    title = "Help Center",
                    icon = Icons.Default.Help,
                    onClick = onHelpCenter
                )
            }

            item {
                SettingsClickableItem(
                    title = "Contact Us",
                    icon = Icons.Default.Email,
                    onClick = onContactUs
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Logout Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    onClick = { showLogoutDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Logout",
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                coroutineScope.launch {
                    try {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Logout failed: ${e.message}")
                    }
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF00C853),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00C853),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF333333)
                )
            )
        }
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = Color.White
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (textColor == Color.Red) Color.Red else Color(0xFF00C853),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Logout",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                "Are you sure you want to logout?",
                color = Color.Gray
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Logout", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}