package com.example.saktinocompose.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.saktinocompose.enduser.EnduserActivity
import com.example.saktinocompose.teknisi.TeknisiActivity

class LoginActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(
                onLoginSuccess = { email, role -> // ✅ Tambah parameter role
                    when (role) { // ✅ Routing berdasarkan role
                        UserRole.TEKNISI -> {
                            val intent = Intent(this, TeknisiActivity::class.java)
                            intent.putExtra("USER_EMAIL", email)
                            intent.putExtra("USER_ROLE", "TEKNISI") // ✅ Kirim role
                            startActivity(intent)
                            finish()
                        }
                        UserRole.END_USER -> { // ✅ Route baru untuk End User
                            val intent = Intent(this, EnduserActivity::class.java)
                            intent.putExtra("USER_EMAIL", email)
                            intent.putExtra("USER_ROLE", "END_USER")
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            )
        }
    }
}