package com.example.saktinocompose.teknisi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.saktinocompose.ui.theme.SaktiNoComposeTheme

class TeknisiActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getIntExtra("USER_ID", 0)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "Unknown"
        val userName = intent.getStringExtra("USER_NAME") ?: "Unknown"
        val userRole = intent.getStringExtra("USER_ROLE") ?: "Unknown"

        setContent {
            SaktiNoComposeTheme {
                TeknisiScreen(
                    userId = userId,
                    userEmail = userEmail,
                    userName = userName,
                    userRole = userRole
                )
            }
        }
    }
}