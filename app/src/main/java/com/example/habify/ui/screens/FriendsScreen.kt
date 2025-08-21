package com.example.habify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Simplified data class
data class Friend(
    val id: String = "",
    val displayName: String = "",
    val streakCount: Int = 0,
    val isOnline: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onNavigateBack: () -> Unit = {}
) {
    var friends by remember { mutableStateOf(getSampleFriends()) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Friends",
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
                actions = {
                    IconButton(onClick = { showAddFriendDialog = true }) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Add Friend",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (friends.isNotEmpty()) {
                item {
                    Text(
                        text = "Your Friends (${friends.size})",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(friends) { friend ->
                    FriendCard(friend = friend)
                }
            } else {
                item {
                    EmptyFriendsCard()
                }
            }
        }
    }

    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            onSendRequest = { email ->
                // Add friend logic here
                showAddFriendDialog = false
            }
        )
    }
}

@Composable
fun FriendCard(friend: Friend) {
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
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C853)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.displayName.firstOrNull()?.toString()?.uppercase() ?: "?",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.displayName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (friend.isOnline) "Online" else "Offline",
                    color = if (friend.isOnline) Color(0xFF00C853) else Color.Gray,
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${friend.streakCount}",
                    color = Color(0xFF00C853),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "days",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onSendRequest: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Friend", color = Color.White) },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Enter email", color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1A1A1A),
                    unfocusedContainerColor = Color(0xFF1A1A1A),
                    focusedBorderColor = Color(0xFF00C853),
                    unfocusedBorderColor = Color.Gray
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (email.isNotBlank()) {
                        onSendRequest(email.trim())
                    }
                }
            ) {
                Text("Send", color = Color(0xFF00C853))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}

@Composable
fun EmptyFriendsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Friends Yet",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add friends to compare streaks!",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Sample data
fun getSampleFriends(): List<Friend> {
    return listOf(
        Friend(
            id = "1",
            displayName = "Alice Johnson",
            streakCount = 25,
            isOnline = true
        ),
        Friend(
            id = "2",
            displayName = "Bob Smith",
            streakCount = 18,
            isOnline = false
        ),
        Friend(
            id = "3",
            displayName = "Carol Davis",
            streakCount = 32,
            isOnline = true
        )
    )
}