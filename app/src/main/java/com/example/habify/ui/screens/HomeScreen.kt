package com.example.habify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
    val isCompletedToday: Boolean = false,
    val categoryId: String = "",
    val categoryName: String = "",
    val iconId: String = "",
    val iconName: String = ""
)

@Composable
fun HomeScreen(
    onAddHabit: () -> Unit = {},
    onNavigateToStats: () -> Unit = {}
) {
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var completionsToday by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedCategory by rememberSaveable { mutableStateOf("all") }

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
                            targetTime = doc.getString("targetTime") ?: "9:00 AM",
                            categoryId = doc.getString("categoryId") ?: "other",
                            categoryName = doc.getString("categoryName") ?: "Other",
                            iconId = doc.getString("iconId") ?: "work",
                            iconName = doc.getString("iconName") ?: "Work"
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

    // Filter habits based on selected category
    val filteredHabits = remember(habits, selectedCategory) {
        if (selectedCategory == "all") {
            habits
        } else {
            habits.filter { it.categoryId == selectedCategory }
        }
    }

    // Get available categories from current habits with counts
    val availableCategories = remember(habits) {
        val habitCategories = habits.groupBy { it.categoryId }
        val allCount = habits.size
        val categoryList = habitCategories.map { (categoryId, habitList) ->
            val categoryName = habitList.firstOrNull()?.categoryName ?: "Unknown"
            categoryId to "$categoryName (${habitList.size})"
        }.toList()
        listOf("all" to "All Categories ($allCount)") + categoryList
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
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
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

                // Category Filter Chips
                if (habits.isNotEmpty()) {
                    item {
                        CategoryFilterSection(
                            categories = availableCategories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Progress Summary
                item {
                    ProgressSummaryCard(
                        habits = filteredHabits,
                        onStatsClick = onNavigateToStats
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Habits by time period or category
                if (filteredHabits.isNotEmpty()) {
                    val groupedHabits = if (selectedCategory == "all") {
                        groupHabitsByTimePeriod(filteredHabits)
                    } else {
                        mapOf("${availableCategories.find { it.first == selectedCategory }?.second ?: "Category"} Habits" to filteredHabits)
                    }

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
fun CategoryFilterSection(
    categories: List<Pair<String, String>>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Categories",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(categories) { (categoryId, categoryName) ->
                FilterChip(
                    onClick = { onCategorySelected(categoryId) },
                    label = {
                        Text(
                            text = categoryName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = selectedCategory == categoryId,
                    modifier = Modifier.animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00C853).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFF00C853),
                        containerColor = Color(0xFF1A1A1A),
                        labelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategory == categoryId,
                        borderColor = Color.Gray.copy(alpha = 0.3f),
                        selectedBorderColor = Color(0xFF00C853),
                        borderWidth = 1.dp,
                        selectedBorderWidth = 2.dp
                    )
                )
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = habit.name,
                            color = if (habit.isCompletedToday)
                                Color.White.copy(alpha = 0.7f)
                            else
                                Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (habit.categoryName.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = getCategoryColor(habit.categoryId).copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.padding(0.dp)
                            ) {
                                Text(
                                    text = habit.categoryName,
                                    color = getCategoryColor(habit.categoryId),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
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

fun groupHabitsByCategory(habits: List<Habit>): Map<String, List<Habit>> {
    return habits.groupBy { habit ->
        habit.categoryName.ifEmpty { "Other" }
    }
}

fun getCategoryColor(categoryId: String): Color {
    return when (categoryId) {
        "health" -> Color(0xFF4CAF50)
        "fitness" -> Color(0xFF2196F3)
        "productivity" -> Color(0xFF9C27B0)
        "social" -> Color(0xFFFF5722)
        "finance" -> Color(0xFFFFEB3B)
        "other" -> Color(0xFF795548)
        else -> Color(0xFF00C853)
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