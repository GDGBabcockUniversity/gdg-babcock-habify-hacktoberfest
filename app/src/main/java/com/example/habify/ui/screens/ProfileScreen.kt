package com.example.habify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onEditProfile: () -> Unit = {}
) {
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var totalHabits by remember { mutableStateOf(0) }
    var totalFriends by remember { mutableStateOf(0) }
    var longestStreak by remember { mutableStateOf(0) }
    var achievements by remember { mutableStateOf<List<Achievement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    // Load user data
    LaunchedEffect(Unit) {
        auth.currentUser?.let { user ->
            userName = user.displayName ?: "User"
            userEmail = user.email ?: ""

            try {
                // Load user stats
                val habitsSnapshot = db.collection("habits")
                    .whereEqualTo("userId", user.uid)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                totalHabits = habitsSnapshot.size()
                longestStreak = habitsSnapshot.documents.maxOfOrNull {
                    it.getLong("streak")?.toInt() ?: 0
                } ?: 0

                // Load friends count (simplified)
                totalFriends = 34 // Sample data

                // Load achievements
                achievements = getSampleAchievements()

            } catch (e: Exception) {
                // Handle error
            }
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00C853))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header
                item {
                    ProfileHeader(
                        userName = userName,
                        userEmail = userEmail,
                        onEditProfile = onEditProfile
                    )
                }

                // Stats Row
                item {
                    StatsRow(
                        totalHabits = totalHabits,
                        totalFriends = totalFriends,
                        longestStreak = longestStreak
                    )
                }

                // Achievements Section
                item {
                    Text(
                        text = "Achievements",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    AchievementsGrid(achievements = achievements)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C853)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = userEmail,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Just a wonderful person in general",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onEditProfile,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile", color = Color.White)
            }
        }
    }
}

@Composable
fun StatsRow(
    totalHabits: Int,
    totalFriends: Int,
    longestStreak: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = totalHabits.toString(),
            label = "Habits",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = totalFriends.toString(),
            label = "Friends",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = longestStreak.toString(),
            label = "Streak",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AchievementsGrid(achievements: List<Achievement>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(achievements.chunked(2)) { rowAchievements ->
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowAchievements.forEach { achievement ->
                    AchievementCard(achievement = achievement)
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.size(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked)
                achievement.color.copy(alpha = 0.1f)
            else
                Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Achievement Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isUnlocked)
                            achievement.color
                        else
                            Color.Gray.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.icon,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.title,
                color = if (achievement.isUnlocked) Color.White else Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

fun getSampleAchievements(): List<Achievement> {
    return listOf(
        Achievement(
            id = "first_habit",
            title = "First Steps",
            description = "Created your first habit",
            icon = "🎯",
            isUnlocked = true,
            color = Color(0xFF00C853)
        ),
        Achievement(
            id = "week_streak",
            title = "Week Warrior",
            description = "7 day streak",
            icon = "🔥",
            isUnlocked = true,
            color = Color(0xFFFF5722)
        ),
        Achievement(
            id = "early_bird",
            title = "Early Bird",
            description = "Complete morning habits",
            icon = "🌅",
            isUnlocked = true,
            color = Color(0xFF2196F3)
        ),
        Achievement(
            id = "social_butterfly",
            title = "Social",
            description = "Add 5 friends",
            icon = "👥",
            isUnlocked = false,
            color = Color(0xFF9C27B0)
        ),
        Achievement(
            id = "consistency",
            title = "Consistency",
            description = "30 day streak",
            icon = "💎",
            isUnlocked = false,
            color = Color(0xFFFFD700)
        ),
        Achievement(
            id = "habit_master",
            title = "Habit Master",
            description = "Complete 100 habits",
            icon = "👑",
            isUnlocked = false,
            color = Color(0xFF795548)
        )
    )
}