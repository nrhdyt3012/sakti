package com.example.saktinocompose.teknisi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.saktinocompose.ui.theme.SaktiNoComposeTheme

class HomeActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "Unknown"
        val userRole = intent.getStringExtra("USER_ROLE") ?: "Unknown"
        setContent {
            SaktiNoComposeTheme {
                HomeScreen(userEmail = userEmail, userRole = userRole)
            }
        }
    }
}