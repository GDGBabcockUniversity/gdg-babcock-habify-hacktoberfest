package com.example.habify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
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
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class Habit(
    val id: String = "",
    val name: String = "",
    val userId: String = "",
    val createdAt: Timestamp? = null,
    val isActive: Boolean = true,
    val streak: Int = 0,
    val totalCompletions: Int = 0,
    val targetTime: String = "9:00 AM",
    val isCompletedToday: Boolean = false
)

@Composable
fun HomeScreen(
    onAddHabit: () -> Unit = {},
    onNavigateToStats: () -> Unit = {}
) {
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var completionsToday by remember { mutableStateOf<Set<String>>(emptySet()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Load habits and completions
    LaunchedEffect(Unit) {
        auth.currentUser?.let { user ->
            try {
                // Load habits
                val habitsSnapshot = db.collection("habits")
                    .whereEqualTo("userId", user.uid)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                val habitsList = habitsSnapshot.documents.mapNotNull { doc ->
                    try {
                        Habit(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            userId = doc.getString("userId") ?: "",
                            createdAt = doc.getTimestamp("createdAt"),
                            isActive = doc.getBoolean("isActive") ?: true,
                            streak = doc.getLong("streak")?.toInt() ?: 0,
                            totalCompletions = doc.getLong("totalCompletions")?.toInt() ?: 0,
                            targetTime = doc.getString("targetTime") ?: "9:00 AM"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // Load today's completions
                val completionsSnapshot = db.collection("completions")
                    .whereEqualTo("date", today)
                    .whereIn("habitId", habitsList.map { it.id }.takeIf { it.isNotEmpty() } ?: listOf("dummy"))
                    .get()
                    .await()

                val completedHabitIds = completionsSnapshot.documents.mapNotNull { doc ->
                    doc.getString("habitId")
                }.toSet()

                // Update habits with completion status
                val updatedHabits = habitsList.map { habit ->
                    habit.copy(isCompletedToday = completedHabitIds.contains(habit.id))
                }

                habits = updatedHabits
                completionsToday = completedHabitIds
                isLoading = false

            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error loading data: ${e.message}")
                }
                isLoading = false
            }
        } ?: run {
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                containerColor = Color(0xFF00C853),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        containerColor = Color.Transparent
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
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Header
                item {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "Today",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = currentDate,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Progress Summary
                item {
                    ProgressSummaryCard(
                        habits = habits,
                        onStatsClick = onNavigateToStats
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Habits by time period
                if (habits.isNotEmpty()) {
                    val groupedHabits = groupHabitsByTimePeriod(habits)

                    groupedHabits.forEach { (period, periodHabits) ->
                        if (periodHabits.isNotEmpty()) {
                            item {
                                Text(
                                    text = period,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }

                            items(periodHabits) { habit ->
                                HabitCard(
                                    habit = habit,
                                    onToggleComplete = { habitId, isCompleted ->
                                        coroutineScope.launch {
                                            val success = toggleHabitCompletion(
                                                habitId = habitId,
                                                isCompleted = isCompleted,
                                                db = db,
                                                today = today
                                            )

                                            if (success) {
                                                // Update local state
                                                habits = habits.map {
                                                    if (it.id == habitId) {
                                                        it.copy(
                                                            isCompletedToday = isCompleted,
                                                            streak = if (isCompleted) it.streak + 1 else maxOf(0, it.streak - 1),
                                                            totalCompletions = if (isCompleted) it.totalCompletions + 1 else maxOf(0, it.totalCompletions - 1)
                                                        )
                                                    } else it
                                                }

                                                completionsToday = if (isCompleted) {
                                                    completionsToday + habitId
                                                } else {
                                                    completionsToday - habitId
                                                }
                                            } else {
                                                snackbarHostState.showSnackbar("Failed to update habit")
                                            }
                                        }
                                    }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                } else {
                    // Empty state
                    item {
                        EmptyStateCard(onAddHabit = onAddHabit)
                    }
                }

                // Bottom padding for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun ProgressSummaryCard(
    habits: List<Habit>,
    onStatsClick: () -> Unit
) {
    val completedToday = habits.count { it.isCompletedToday }
    val totalHabits = habits.size
    val completionPercentage = if (totalHabits > 0) (completedToday * 100) / totalHabits else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Progress",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                TextButton(onClick = onStatsClick) {
                    Text("View Stats", color = Color(0xFF00C853))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$completedToday/$totalHabits",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00C853)
                    )
                    Text(
                        text = "Habits Completed",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$completionPercentage%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Completion Rate",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = completionPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF00C853),
                trackColor = Color(0xFF333333)
            )
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    onToggleComplete: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday)
                Color(0xFF00C853).copy(alpha = 0.1f)
            else
                Color(0xFF1A1A1A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onToggleComplete(habit.id, !habit.isCompletedToday) }
                ) {
                    Icon(
                        imageVector = if (habit.isCompletedToday)
                            Icons.Filled.CheckCircle
                        else
                            Icons.Outlined.Circle,
                        contentDescription = if (habit.isCompletedToday) "Completed" else "Not completed",
                        tint = if (habit.isCompletedToday) Color(0xFF00C853) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = habit.name,
                        color = if (habit.isCompletedToday)
                            Color.White.copy(alpha = 0.7f)
                        else
                            Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = habit.targetTime,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            if (habit.streak > 0) {
                Text(
                    text = "${habit.streak} day streak",
                    color = Color(0xFF00C853),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(onAddHabit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Habits Yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "Start building better habits today!",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddHabit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                Text("Create Your First Habit", color = Color.White)
            }
        }
    }
}

// Helper functions
fun groupHabitsByTimePeriod(habits: List<Habit>): Map<String, List<Habit>> {
    return habits.groupBy { habit ->
        val timeparts = habit.targetTime.split(":")
        val hour = timeparts[0].toIntOrNull() ?: 9
        when {
            hour < 12 -> "Morning"
            hour < 17 -> "Afternoon"
            else -> "Evening"
        }
    }
}

suspend fun toggleHabitCompletion(
    habitId: String,
    isCompleted: Boolean,
    db: FirebaseFirestore,
    today: String
): Boolean {
    return try {
        val completionDocId = "${habitId}_$today"

        if (isCompleted) {
            // Mark as completed
            val completionData = hashMapOf(
                "habitId" to habitId,
                "date" to today,
                "completedAt" to Timestamp.now()
            )

            db.collection("completions")
                .document(completionDocId)
                .set(completionData)
                .await()

            // Update habit streak and total completions
            val habitDoc = db.collection("habits").document(habitId).get().await()
            val currentStreak = habitDoc.getLong("streak")?.toInt() ?: 0
            val totalCompletions = habitDoc.getLong("totalCompletions")?.toInt() ?: 0

            db.collection("habits").document(habitId)
                .update(
                    mapOf(
                        "streak" to currentStreak + 1,
                        "totalCompletions" to totalCompletions + 1
                    )
                )
                .await()
        } else {
            // Mark as incomplete
            db.collection("completions")
                .document(completionDocId)
                .delete()
                .await()

            // Update habit streak (decrease by 1, but don't go below 0)
            val habitDoc = db.collection("habits").document(habitId).get().await()
            val currentStreak = habitDoc.getLong("streak")?.toInt() ?: 0
            val totalCompletions = habitDoc.getLong("totalCompletions")?.toInt() ?: 0

            db.collection("habits").document(habitId)
                .update(
                    mapOf(
                        "streak" to maxOf(0, currentStreak - 1),
                        "totalCompletions" to maxOf(0, totalCompletions - 1)
                    )
                )
                .await()
        }
        true
    } catch (e: Exception) {
        false
    }
}