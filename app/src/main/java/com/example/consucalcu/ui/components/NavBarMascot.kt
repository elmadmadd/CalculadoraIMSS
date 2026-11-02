package com.example.consucalcu.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.consucalcu.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

private const val MascotFrameCount = 36
private const val MascotColumns = 6
private const val MascotRows = 6

/**
 * Cameo decorativo y no interactivo que cruza ocasionalmente la barra inferior,
 * siempre dentro de ella para no tapar ofertas ni cifras.
 * Usa un atlas para mantener una sola imagen decodificada en memoria.
 */
@Composable
fun SporadicNavBarMascot(
    enabled: Boolean,
    onAppearance: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val animationsEnabled = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) > 0f
    }
    if (!enabled || !animationsEnabled) return

    val spriteSheet = androidx.compose.ui.graphics.ImageBitmap.imageResource(
        id = R.drawable.mascot_walk_sheet
    )
    var frameIndex by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(false) }
    var facingRight by remember { mutableStateOf(true) }
    val horizontalPosition = remember { Animatable(0f) }
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val mascotSize = 72.dp
        val mascotSizePx = with(density) { mascotSize.toPx() }
        val travelWidthPx = constraints.maxWidth.toFloat()
        val frameWidth = (spriteSheet.width / MascotColumns).coerceAtLeast(1)
        val frameHeight = (spriteSheet.height / MascotRows).coerceAtLeast(1)

        LaunchedEffect(enabled, travelWidthPx) {
            // La primera visita sucede pronto para que el detalle pueda descubrirse.
            delay(Random.nextLong(10_000L, 22_000L))

            while (isActive) {
                facingRight = Random.nextBoolean()
                val start = if (facingRight) -mascotSizePx else travelWidthPx + mascotSizePx
                val end = if (facingRight) travelWidthPx + mascotSizePx else -mascotSizePx
                horizontalPosition.snapTo(start)
                frameIndex = Random.nextInt(MascotFrameCount)
                visible = true
                onAppearance()

                coroutineScope {
                    val frameJob = launch {
                        while (isActive) {
                            frameIndex = (frameIndex + 1) % MascotFrameCount
                            delay(84L)
                        }
                    }
                    horizontalPosition.animateTo(
                        targetValue = end,
                        animationSpec = tween(durationMillis = 6_500, easing = LinearEasing)
                    )
                    frameJob.cancel()
                }

                visible = false
                delay(Random.nextLong(45_000L, 100_000L))
            }
        }

        if (visible) {
            Canvas(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = horizontalPosition.value.roundToInt(),
                            y = ((constraints.maxHeight - mascotSizePx) / 2).roundToInt()
                        )
                    }
                    .size(mascotSize)
                    .graphicsLayer { scaleX = if (facingRight) 1f else -1f }
            ) {
                val column = frameIndex % MascotColumns
                val row = frameIndex / MascotColumns
                drawImage(
                    image = spriteSheet,
                    srcOffset = IntOffset(column * frameWidth, row * frameHeight),
                    srcSize = IntSize(frameWidth, frameHeight),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                    filterQuality = FilterQuality.Medium
                )
            }
        }
    }
}

/** Saludo de una sola reproducción para el primer paso del onboarding. */
@Composable
fun OnboardingGreetingMascot(
    playGreeting: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val animationsEnabled = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) > 0f
    }
    val spriteSheet = androidx.compose.ui.graphics.ImageBitmap.imageResource(
        id = R.drawable.mascot_greeting_sheet
    )
    var frameIndex by remember { mutableIntStateOf(if (animationsEnabled) 0 else MascotFrameCount - 1) }
    val frameWidth = (spriteSheet.width / MascotColumns).coerceAtLeast(1)
    val frameHeight = (spriteSheet.height / MascotRows).coerceAtLeast(1)

    LaunchedEffect(playGreeting, animationsEnabled) {
        if (!animationsEnabled || !playGreeting) {
            frameIndex = MascotFrameCount - 1
            return@LaunchedEffect
        }

        frameIndex = 0
        repeat(MascotFrameCount - 1) {
            delay(84L)
            frameIndex++
        }
    }

    Canvas(modifier = modifier) {
        val column = frameIndex % MascotColumns
        val row = frameIndex / MascotColumns
        drawImage(
            image = spriteSheet,
            srcOffset = IntOffset(column * frameWidth, row * frameHeight),
            srcSize = IntSize(frameWidth, frameHeight),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            filterQuality = FilterQuality.Medium
        )
    }
}
