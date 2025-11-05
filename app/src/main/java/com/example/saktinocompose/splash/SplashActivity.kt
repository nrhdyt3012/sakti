package com.example.saktinocompose.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.saktinocompose.R
import com.example.saktinocompose.login.LoginActivity
import com.example.saktinocompose.ui.theme.SaktiNoComposeTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide system bars for full screen experience
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SaktiNoComposeTheme {
                SplashScreen {
                    // Navigate to LoginActivity after animation
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onNavigate: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    // Logo animations with smooth easing
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "logoAlpha"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    // Top Right Ornament animations
    val topOrnamentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "topOrnamentAlpha"
    )

    val topOrnamentOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -20f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "topOrnamentOffset"
    )

    // Bottom Left Ornament animations
    val bottomOrnamentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "bottomOrnamentAlpha"
    )

    val bottomOrnamentOffset by animateFloatAsState(
        targetValue = if (startAnimation) 10f else 20f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 200, 
            easing = FastOutSlowInEasing
        ),
        label = "bottomOrnamentOffset"
    )

    // Start animation and navigate
    LaunchedEffect(Unit) {
        delay(150)
        startAnimation = true
        delay(2800) // Total splash duration
        onNavigate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8EAF6)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_corner_top),
            contentDescription = "Top Ornament",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(200.dp)
                .absoluteOffset(x = 40.dp, y = topOrnamentOffset.dp) // 👉 geser sedikit ke kanan
                .alpha(topOrnamentAlpha),
            contentScale = ContentScale.Fit
        )

        Image(
            painter = painterResource(id = R.drawable.splash_corner_bottom),
            contentDescription = "Bottom Ornament",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(200.dp)
                .absoluteOffset(x = (-40).dp, y = bottomOrnamentOffset.dp) // 👉 geser sedikit ke kiri
                .alpha(bottomOrnamentAlpha),
            contentScale = ContentScale.Fit
        )


        Image(
            painter = painterResource(id = R.drawable.logo_splash),
            contentDescription = "SAKTI Logo",
            modifier = Modifier
                .size(220.dp, 88.dp)
                .scale(logoScale)
                .alpha(logoAlpha),
            contentScale = ContentScale.Fit
        )
    }

}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Splash Screen Preview"
)
@Composable
fun SplashScreenPreview() {
    SaktiNoComposeTheme {
        SplashScreen(onNavigate = {})
    }
}