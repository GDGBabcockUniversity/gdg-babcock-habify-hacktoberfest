package com.example.habify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habify.ui.components.StarryBackground

@Composable
fun CreateHabitScreen(onHabitCreated: () -> Unit) {
    var habitName by remember { mutableStateOf("") }

    StarryBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("Habit name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onHabitCreated() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Habit")
            }
        }
    }
}