package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RiverDarkBackground
import com.example.ui.theme.RiverPrimary
import com.example.ui.theme.RiverSecondary
import com.example.ui.viewmodel.BharamputraViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: BharamputraViewModel) {
    var startAnimate by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimate) 1.1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimate) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "contentAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimate = true
        delay(2500)
        val user = viewModel.currentUser.value
        if (user != null) {
            viewModel.navigateTo("main")
        } else {
            viewModel.navigateTo("auth")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RiverDarkBackground,
                        Color(0xFF020202)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Brand Logo Container (Sophisticated Dark gradient rounded square)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                RiverPrimary,
                                RiverSecondary
                            )
                        )
                    )
                    .padding(3.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(RiverDarkBackground),
                contentAlignment = Alignment.Center
            ) {
                // High-contrast clean 'B' glyph in white or Play symbols
                Text(
                    text = "B",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Typography
            Text(
                text = "BHARAMPUTRA",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 6.sp,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Stream • Share • Discover",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = RiverSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = RiverSecondary,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(28.dp)
                    .alpha(contentAlpha)
            )
        }
    }
}
