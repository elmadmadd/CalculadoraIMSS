package com.example.consucalcu.ui.screens

import android.provider.Settings
import android.os.SystemClock
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.ConsuNavy
import com.example.consucalcu.ConsuNavyDark
import com.example.consucalcu.ConsuOrange
import com.example.consucalcu.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    val animationScale = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ).coerceAtLeast(0f)
    }
    val startedAt by rememberSaveable { mutableStateOf(SystemClock.elapsedRealtime()) }
    var phase by rememberSaveable { mutableStateOf(0) }
    val latestOnFinished by rememberUpdatedState(onSplashFinished)

    LaunchedEffect(animationScale, startedAt) {
        if (animationScale == 0f) {
            delay(120)
            latestOnFinished()
            return@LaunchedEffect
        }

        val revealEnd = startedAt + (650L * animationScale).toLong()
        val splashEnd = revealEnd + (360L * animationScale).toLong()
        phase = 1
        delay((revealEnd - SystemClock.elapsedRealtime()).coerceAtLeast(0L))
        phase = 2
        delay((splashEnd - SystemClock.elapsedRealtime()).coerceAtLeast(0L))
        latestOnFinished()
    }

    val entered = phase >= 1
    val collapsing = phase >= 2

    // El logo arranca visible y en la misma posición que el splash del sistema,
    // así el relevo no parpadea. Solo el texto hace la entrada.
    val logoAlpha by animateFloatAsState(
        targetValue = if (collapsing) 0f else 1f,
        animationSpec = tween(120, easing = EaseInOutCubic),
        label = "splashLogoAlpha",
    )
    val logoScale by animateFloatAsState(
        targetValue = if (collapsing) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 460f),
        label = "splashLogoScale",
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (entered && !collapsing) 1f else 0f,
        animationSpec = tween(if (collapsing) 120 else 260, easing = EaseInOutCubic),
        label = "splashTextAlpha",
    )
    val textOffset by animateDpAsState(
        targetValue = if (entered && !collapsing) 0.dp else 10.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "splashTextOffset",
    )
    val capsuleAlpha by animateFloatAsState(
        targetValue = if (collapsing) 1f else 0f,
        animationSpec = tween(110),
        label = "splashCapsuleAlpha",
    )
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (collapsing) 0f else 1f,
        animationSpec = tween(
            durationMillis = 280,
            delayMillis = if (collapsing) 40 else 0,
            easing = EaseInOutCubic,
        ),
        label = "splashBackgroundAlpha",
    )
    val capsuleWidth by animateDpAsState(
        targetValue = if (collapsing) 132.dp else 190.dp,
        animationSpec = tween(340, easing = FastOutSlowInEasing),
        label = "splashCapsuleWidth",
    )
    val capsuleHeight by animateDpAsState(
        targetValue = if (collapsing) 32.dp else 46.dp,
        animationSpec = tween(340, easing = FastOutSlowInEasing),
        label = "splashCapsuleHeight",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            },
    ) {
        val compactLayout = maxHeight < 480.dp
        // Mismas medidas que el splash del sistema: círculo de 160 dp y logo
        // de 240 dp con 55 dp de margen (ver drawable/splash_icon.xml).
        val circleSize = if (compactLayout) 120.dp else 160.dp
        val logoSize = circleSize * 0.81f
        val density = LocalDensity.current
        val statusBarHeight = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
        val capsuleTop by animateDpAsState(
            // Keep the same anchor as DynamicIslandHeader: status bar + 4dp.
            // This prevents a visible jump when the custom splash is removed.
            targetValue = if (collapsing) {
                statusBarHeight + 4.dp
            } else {
                // Nace en el centro del logo, igual que el splash del sistema.
                (maxHeight - capsuleHeight) / 2
            },
            animationSpec = tween(340, easing = FastOutSlowInEasing),
            label = "splashCapsuleTop",
        )

        // Fondo liso igual al del sistema; el degradado entra suavemente encima.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(backgroundAlpha)
                .background(ConsuNavy),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(textAlpha)
                    .background(Brush.verticalGradient(listOf(ConsuNavy, ConsuNavyDark))),
            )
        }

        // El sistema centra el ícono en toda la ventana, no en el área segura.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (maxHeight - circleSize) / 2)
                .size(circleSize)
                .scale(logoScale)
                .alpha(logoAlpha)
                .clip(CircleShape)
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.consucalcu_logo_mark),
                contentDescription = null,
                modifier = Modifier.size(logoSize),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (maxHeight + circleSize) / 2 + (if (compactLayout) 12.dp else 20.dp) + textOffset)
                .alpha(textAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Black)) {
                        append("Consu")
                    }
                    withStyle(SpanStyle(color = ConsuOrange, fontWeight = FontWeight.Black)) {
                        append("Calcu")
                    }
                },
                fontSize = if (compactLayout) 27.sp else 32.sp,
                letterSpacing = 0.2.sp,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Cotiza con claridad",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = capsuleTop)
                .width(capsuleWidth)
                .height(capsuleHeight)
                .alpha(capsuleAlpha)
                .clip(RoundedCornerShape(50))
                .background(ConsuNavy),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ConsuCalcu",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
            )
        }
    }
}
