package com.example.habify.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habify.R
import androidx.compose.ui.text.style.TextAlign

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top half image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Takes half the screen
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.welcome_image), // your logo
                contentDescription = "Habify Logo",
                modifier = Modifier
                    .fillMaxWidth(0.7f) // adjust size
                    .aspectRatio(1f)    // keep square
            )
        }

        // Text directly under image
        Column(
            modifier = Modifier
                .weight(1f) // takes the other half
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Build habits together",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join a community of students to stay motivated and achieve your goals.",
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // Bottom button
        Button(
            onClick = { onGetStarted() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853),
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Get Started")
        }
    }
}