package com.example.habify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

// Data classes for habit customization
data class HabitIcon(
    val id: String,
    val icon: ImageVector,
    val name: String,
    val color: Color = Color(0xFF00C853)
)

data class HabitCategory(
    val id: String,
    val name: String,
    val color: Color
)

enum class HabitFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onHabitSaved: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    var habitName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(getAvailableIcons().first()) }
    var selectedCategory by remember { mutableStateOf(getAvailableCategories().first()) }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("9:00 AM") }
    var showTimePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Enhanced form validation
    val isFormValid = remember(habitName) {
        habitName.isNotBlank() &&
                habitName.length <= 20 &&
                habitName.trim().length >= 2
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Habit",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0D0D0D)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Habit Name Input
            Text(
                text = "Habit Name",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = habitName,
                onValueChange = { if (it.length <= 20) habitName = it },
                placeholder = { Text("Enter habit name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                isError = habitName.isNotEmpty() && habitName.trim().length < 2,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (habitName.isNotEmpty() && habitName.trim().length < 2) {
                            Text(
                                text = "Name must be at least 2 characters",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text(
                            "${habitName.length}/20",
                            color = when {
                                habitName.length > 20 -> Color.Red
                                habitName.length > 15 -> Color.Yellow
                                else -> Color.Gray
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Icon Selection
            Text(
                text = "Icon",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            IconSelectionGrid(
                icons = getAvailableIcons(),
                selectedIcon = selectedIcon,
                onIconSelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedIcon = it
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category Selection
            Text(
                text = "Category",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            CategorySelectionRow(
                categories = getAvailableCategories(),
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedCategory = it
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Frequency Selection
            Text(
                text = "Frequency",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            FrequencySelectionRow(
                frequencies = HabitFrequency.entries.toList(),
                selectedFrequency = selectedFrequency,
                onFrequencySelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedFrequency = it
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reminder Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Set Reminder",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = "Get notified to complete your habit",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            reminderEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00C853),
                            checkedTrackColor = Color(0xFF00C853).copy(alpha = 0.4f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reminder Time Selection
            Text(
                text = "Reminder Time",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showTimePicker = true
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Set Time",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = selectedTime,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select time",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    if (isFormValid) {
                        isLoading = true
                        saveHabit(
                            habitName = habitName.trim(),
                            icon = selectedIcon,
                            category = selectedCategory,
                            frequency = selectedFrequency,
                            reminderEnabled = reminderEnabled,
                            reminderTime = selectedTime,
                            onSuccess = {
                                isLoading = false
                                onHabitSaved()
                            },
                            onError = { error ->
                                isLoading = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            }
                        )
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please fill in all required fields correctly")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853),
                    disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.4f)
                )
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Creating...",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        "Create Habit",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            selectedTime = selectedTime,
            onTimeSelected = { time ->
                selectedTime = time
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun IconSelectionGrid(
    icons: List<HabitIcon>,
    selectedIcon: HabitIcon,
    onIconSelected: (HabitIcon) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(200.dp)
    ) {
        items(icons) { icon ->
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectedIcon.id == icon.id)
                            Color(0xFF00C853).copy(alpha = 0.2f)
                        else
                            Color(0xFF1A1A1A)
                    )
                    .border(
                        width = if (selectedIcon.id == icon.id) 2.dp else 0.dp,
                        color = Color(0xFF00C853),
                        shape = CircleShape
                    )
                    .clickable { onIconSelected(icon) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon.icon,
                    contentDescription = "Select ${icon.name} icon",
                    tint = if (selectedIcon.id == icon.id) Color(0xFF00C853) else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun CategorySelectionRow(
    categories: List<HabitCategory>,
    selectedCategory: HabitCategory,
    onCategorySelected: (HabitCategory) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(120.dp)
    ) {
        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable { onCategorySelected(category) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCategory.id == category.id)
                        category.color.copy(alpha = 0.2f)
                    else
                        Color(0xFF1A1A1A)
                ),
                border = if (selectedCategory.id == category.id)
                    androidx.compose.foundation.BorderStroke(2.dp, category.color)
                else
                    androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.name,
                        color = if (selectedCategory.id == category.id)
                            category.color
                        else
                            Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (selectedCategory.id == category.id)
                            FontWeight.SemiBold
                        else
                            FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FrequencySelectionRow(
    frequencies: List<HabitFrequency>,
    selectedFrequency: HabitFrequency,
    onFrequencySelected: (HabitFrequency) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        frequencies.forEach { frequency ->
            FilterChip(
                onClick = { onFrequencySelected(frequency) },
                label = { Text(frequency.displayName) },
                selected = selectedFrequency == frequency,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00C853).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFF00C853),
                    containerColor = Color(0xFF1A1A1A),
                    labelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.split(":")[0].trim().toIntOrNull() ?: 9,
        initialMinute = selectedTime.split(":")[1].trim().split(" ")[0].toIntOrNull() ?: 0,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Time",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = Color(0xFF2A2A2A),
                    selectorColor = Color(0xFF00C853),
                    containerColor = Color(0xFF1A1A1A),
                    periodSelectorBorderColor = Color(0xFF00C853),
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = Color.Gray,
                    periodSelectorSelectedContainerColor = Color(0xFF00C853),
                    periodSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                    periodSelectorSelectedContentColor = Color.White,
                    periodSelectorUnselectedContentColor = Color.Gray,
                    timeSelectorSelectedContainerColor = Color(0xFF00C853),
                    timeSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                    timeSelectorSelectedContentColor = Color.White,
                    timeSelectorUnselectedContentColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        val amPm = if (hour < 12) "AM" else "PM"
                        val displayHour = when {
                            hour == 0 -> 12
                            hour > 12 -> hour - 12
                            else -> hour
                        }
                        val formattedTime = String.format("%d:%02d %s", displayHour, minute, amPm)
                        onTimeSelected(formattedTime)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C853)
                    )
                ) {
                    Text("OK", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color(0xFF1A1A1A),
    unfocusedContainerColor = Color(0xFF1A1A1A),
    focusedBorderColor = Color(0xFF00C853),
    unfocusedBorderColor = Color.Gray,
    errorBorderColor = Color.Red,
    cursorColor = Color.White
)

// Helper functions
fun getAvailableIcons(): List<HabitIcon> {
    return listOf(
        HabitIcon("run", Icons.AutoMirrored.Filled.DirectionsRun, "Running"),
        HabitIcon("fitness", Icons.Default.FitnessCenter, "Fitness"),
        HabitIcon("book", Icons.AutoMirrored.Filled.MenuBook, "Reading"),
        HabitIcon("water", Icons.Default.WaterDrop, "Water"),
        HabitIcon("meditation", Icons.Default.SelfImprovement, "Meditation"),
        HabitIcon("sleep", Icons.Default.Bedtime, "Sleep"),
        HabitIcon("food", Icons.Default.Restaurant, "Food"),
        HabitIcon("study", Icons.Default.School, "Study"),
        HabitIcon("walk", Icons.AutoMirrored.Filled.DirectionsWalk, "Walking"),
        HabitIcon("music", Icons.Default.MusicNote, "Music"),
        HabitIcon("work", Icons.Default.Work, "Work"),
        HabitIcon("phone", Icons.Default.PhoneAndroid, "Phone")
    )
}

fun getAvailableCategories(): List<HabitCategory> {
    return listOf(
        HabitCategory("health", "Health", Color(0xFF4CAF50)),
        HabitCategory("fitness", "Fitness", Color(0xFF2196F3)),
        HabitCategory("productivity", "Productivity", Color(0xFF9C27B0)),
        HabitCategory("social", "Social", Color(0xFFFF5722)),
        HabitCategory("finance", "Finance", Color(0xFFFFEB3B)),
        HabitCategory("other", "Other", Color(0xFF795548))
    )
}

fun saveHabit(
    habitName: String,
    icon: HabitIcon,
    category: HabitCategory,
    frequency: HabitFrequency,
    reminderEnabled: Boolean,
    reminderTime: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser
    if (currentUser == null) {
        onError("Please sign in to create habits")
        return
    }

    val habitData = hashMapOf(
        "name" to habitName,
        "userId" to currentUser.uid,
        "iconId" to icon.id,
        "iconName" to icon.name,
        "categoryId" to category.id,
        "categoryName" to category.name,
        "frequency" to frequency.name,
        "reminderEnabled" to reminderEnabled,
        "createdAt" to Timestamp.now(),
        "isActive" to true,
        "streak" to 0,
        "totalCompletions" to 0,
        "targetTime" to reminderTime
    )

    db.collection("habits")
        .add(habitData)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
            val errorMessage = when (exception) {
                is FirebaseNetworkException -> "No internet connection. Please check your network and try again."
                is FirebaseException -> "Server error. Please try again in a moment."
                else -> exception.message ?: "Failed to create habit. Please try again."
            }
            onError(errorMessage)
        }
}