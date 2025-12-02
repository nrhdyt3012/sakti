package com.example.saktinocompose.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

lateinit var successUsername: String
val White = Color(0xFFFFFFFF)
val MediumGreen = Color(0xFF009951)
val GrayBackground = Color(0xFF485F88)
val Black = Color(0xFF000000)

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModelCompose = viewModel(),
    onLoginSuccess: (userId: String, email: String, name: String, role: String, token: String?) -> Unit
) {
    val uiState by loginViewModel.uiState.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var successUserId: String by remember { mutableStateOf("") }
    var successEmail by remember { mutableStateOf("") }
    var successName by remember { mutableStateOf("") }
    var successRole by remember { mutableStateOf("") }
    var successToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loginViewModel.loginEvent.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> {
                    successUserId = event.user.id
                    successUsername = event.user.username
                    successName = event.user.name
                    successRole = event.user.role
                    successToken = event.token
                    showSuccessDialog = true
                }
                is LoginEvent.LoginError -> {
                    showErrorDialog = event.message
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        val roleText = when (successRole.uppercase()) {
            "TEKNISI" -> "Teknisi"
            "END_USER" -> "End User"
            else -> successRole
        }

        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Selamat Datang!") },
            text = {
                Column {
                    Text("Login berhasil!")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nama: $successName")
                    Text("Role: $roleText")
                    if (successToken != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "✓ Terhubung ke server",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    onLoginSuccess(successUserId, successEmail, successName, successRole, successToken)
                }) {
                    Text("Lanjut")
                }
            }
        )
    }

    // Error Dialog
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

    val viewAlphas = List(7) { remember { Animatable(0f) } }
    LaunchedEffect(Unit) {
        launch {
            viewAlphas.forEachIndexed { index, animatable ->
                delay(150)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200)
                )
            }
        }
    }

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
            // Image
            Image(
                painter = painterResource(id = R.drawable.image_login),
                contentDescription = "Login Image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Login",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(viewAlphas[0].value)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Username Field (changed from email)
            OutlinedTextField(
                value = uiState.username,
                onValueChange = loginViewModel::onUsernameChange,
                label = {
                    Surface(
                        color = Color.White,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Username",
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(viewAlphas[2].value),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = MediumGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            var passwordVisible by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = uiState.password,
                onValueChange = loginViewModel::onPasswordChange,
                label = {
                    Surface(
                        color = Color.White,
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
                    contentColor = Black
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Black
                    )
                } else {
                    Text("Login", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}