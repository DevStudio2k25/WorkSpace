package com.devstudio.workspace.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit = {}
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    // Fade in animation
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )
    
    // Start animation and navigate after delay
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000) // 3 seconds splash
        onSplashComplete()
    }
    
    // Gradient background
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = gradientColors
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            // Lottie Animation
            // Place your JSON file in: app/src/main/res/raw/splash_animation.json
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(
                    resId = androidx.compose.ui.platform.LocalContext.current.resources.getIdentifier(
                        "splash_animation",
                        "raw",
                        androidx.compose.ui.platform.LocalContext.current.packageName
                    )
                )
            )
            
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                isPlaying = true,
                speed = 1f,
                restartOnPlay = false
            )
            
            // Only show Lottie animation - no fallback
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(250.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Name
            Text(
                text = "Workspace",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "Your Personal Notes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
