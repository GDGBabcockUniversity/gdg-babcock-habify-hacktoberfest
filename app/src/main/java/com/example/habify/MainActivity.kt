package com.example.habify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.habify.navigation.AuthNavHost
import com.example.habify.navigation.MainNavHost
import com.example.habify.ui.components.StarFieldProvider
import com.example.habify.ui.components.StarryBackground
import com.example.habify.ui.components.rememberStarFieldState
import com.example.habify.ui.theme.HabifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabifyTheme {
                // Single shared star field for the whole app
                val stars = rememberStarFieldState()

                // Fake auth state for now
                var isAuthenticated by remember { mutableStateOf(false) }

                StarFieldProvider(stars) {
                    // StarryBackground now wraps *everything*
                    StarryBackground {
                        if (isAuthenticated) {
                            MainNavHost()
                        } else {
                            AuthNavHost(onAuthComplete = {
                                isAuthenticated = true
                            })
                        }
                    }
                }
            }
        }
    }
}
