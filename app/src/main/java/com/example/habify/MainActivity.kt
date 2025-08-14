package com.example.habify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.habify.navigation.AuthNavHost
import com.example.habify.navigation.MainNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var userIsLoggedIn by remember { mutableStateOf(false) }

                if (userIsLoggedIn) {
                    MainNavHost()
                } else {
                    AuthNavHost(onAuthComplete = { userIsLoggedIn = true })
                }
            }
        }
    }
}