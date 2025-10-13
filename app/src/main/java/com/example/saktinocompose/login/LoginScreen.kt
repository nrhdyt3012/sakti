package com.example.saktinocompose.login

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Definisikan warna yang digunakan
val White = Color(0xFFFFFFFF)

val MediumGreen = Color(0xFF009951)
val GrayBackground = Color(0xFF485F88)

val Black = Color(0xFF000000)


@Composable
fun LoginScreen(
    loginViewModel: LoginViewModelCompose = viewModel(),
    onLoginSuccess: (email: String, role: UserRole) -> Unit
) {
    val uiState by loginViewModel.uiState.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var successEmail by remember { mutableStateOf("") }
    var successRole by remember { mutableStateOf<UserRole?>(null) }

    // Listener untuk event dari ViewModel
    LaunchedEffect(Unit) {
        loginViewModel.loginEvent.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> {
                    showSuccessDialog = true
                }
                is LoginEvent.LoginError -> {
                    showErrorDialog = event.message
                }
            }
        }
    }

    // --- Dialog ---
    if (showSuccessDialog && successRole != null) {
        val roleText = when (successRole) {
            UserRole.TEKNISI -> "Teknisi"
            UserRole.END_USER -> "Pengguna Akhir"
            else -> ""
        }

        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Yeah!") },
            text = { Text("Selamat datang kembali, $successEmail\nRole: $roleText") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    successRole?.let { onLoginSuccess(successEmail, it) }
                }) {
                    Text("Lanjut")
                }
            }
        )
    }

    if (showErrorDialog != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Gagal") },
            text = { Text(showErrorDialog ?: "Terjadi kesalahan") },
            confirmButton = {
                Button(onClick = { showErrorDialog = null }) {
                    Text("Kembali")
                }
            }
        )
    }

    // --- Animasi ---
    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val imageTranslationX by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "image translation"
    )

    val viewAlphas = List(7) { remember { Animatable(0f) } }
    LaunchedEffect(Unit) {
        launch {
            viewAlphas.forEachIndexed { index, animatable ->
                delay(150) // Jeda antar animasi
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200)
                )
            }
        }
    }

    // --- UI Layout ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image with animation
            Image(
                painter = painterResource(id = R.drawable.image_login), // Pastikan drawable ada
                contentDescription = "Login Image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Texts and Inputs
            Text(
                text = "Log In",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(viewAlphas[0].value)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Email Field
            // === EMAIL FIELD ===
            OutlinedTextField(
                value = uiState.email,
                onValueChange = loginViewModel::onEmailChange,
                label = {
                    Surface(
                        color = Color.White, // Background label putih
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Email",
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(viewAlphas[2].value),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedIndicatorColor = MediumGreen,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = MediumGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

// === PASSWORD FIELD ===
            var passwordVisible by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = uiState.password,
                onValueChange = loginViewModel::onPasswordChange,
                label = {
                    Surface(
                        color = Color.White, // Background label putih
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Password",
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, "Toggle password visibility")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(viewAlphas[3].value),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedIndicatorColor = MediumGreen,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = MediumGreen
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = { loginViewModel.login() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .alpha(viewAlphas[4].value),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = Black)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Login", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {_, _ -> })
}