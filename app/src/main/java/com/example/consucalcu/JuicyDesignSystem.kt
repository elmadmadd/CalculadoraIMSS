package com.example.consucalcu

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RawRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

// -----------------------------------------------------------------------------
// 1. REAL DARK MODE & JUICY THEME
// -----------------------------------------------------------------------------

object JuicyColors {
    // Light Palette
    val PrimaryLight = Color(0xFF00E5FF) // Cyan
    val SecondaryLight = Color(0xFFFF4081) // Pink
    val TertiaryLight = Color(0xFFFFEA00) // Yellow
    val BackgroundLight = Color(0xFFFAFAFA)
    val SurfaceLight = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF1A1A1A)

    // Dark Palette
    val PrimaryDark = Color(0xFF00B8D4) // Darker Cyan
    val SecondaryDark = Color(0xFFF50057) // Darker Pink
    val TertiaryDark = Color(0xFFFFD600) // Darker Yellow
    val BackgroundDark = Color(0xFF121212) // Near Black
    val SurfaceDark = Color(0xFF1E1E1E) // Dark Gray
    val TextLight = Color(0xFFF5F5F5)
    
    // Semantic
    val Error = Color(0xFFFF5252)
    val Success = Color(0xFF69F0AE)
}

object JuicyTheme {
    @Composable
    fun getColors(darkTheme: Boolean = isSystemInDarkTheme()): JuicyColorScheme {
        return if (darkTheme) {
            JuicyColorScheme(
                primary = JuicyColors.PrimaryDark,
                secondary = JuicyColors.SecondaryDark,
                tertiary = JuicyColors.TertiaryDark,
                background = JuicyColors.BackgroundDark,
                surface = JuicyColors.SurfaceDark,
                text = JuicyColors.TextLight,
                isDark = true
            )
        } else {
            JuicyColorScheme(
                primary = JuicyColors.PrimaryLight,
                secondary = JuicyColors.SecondaryLight,
                tertiary = JuicyColors.TertiaryLight,
                background = JuicyColors.BackgroundLight,
                surface = JuicyColors.SurfaceLight,
                text = JuicyColors.TextDark,
                isDark = false
            )
        }
    }
}

data class JuicyColorScheme(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val text: Color,
    val isDark: Boolean
)

// -----------------------------------------------------------------------------
// 2. ADVANCED HAPTICS (CoreHaptics Equivalent)
// -----------------------------------------------------------------------------

class JuicyHaptics(context: Context) {
    @Suppress("DEPRECATION") // Necesario para compatibilidad con Android < 12
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun play(style: HapticStyle) {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (style) {
                HapticStyle.LIGHT -> VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticStyle.MEDIUM -> VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticStyle.HEAVY -> VibrationEffect.createOneShot(50, 255)
                HapticStyle.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 20), intArrayOf(0, 100, 0, 150), -1)
                HapticStyle.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 30, 60, 30, 60, 30), intArrayOf(0, 200, 0, 200, 0, 200), -1)
                HapticStyle.BUZZ -> VibrationEffect.createOneShot(200, 100)
            }
            vibrator.vibrate(effect)
        } else {
            // Fallback para dispositivos Android < 8 (API 26)
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }

    enum class HapticStyle {
        LIGHT, MEDIUM, HEAVY, SUCCESS, ERROR, BUZZ
    }
}

val LocalJuicyHaptics = androidx.compose.runtime.staticCompositionLocalOf<JuicyHaptics?> { null }


// -----------------------------------------------------------------------------
// 3. LOTTIE PRE-LOADING CACHE
// -----------------------------------------------------------------------------

object LottieCache {
    private val cache = ConcurrentHashMap<Int, LottieComposition>()

    fun preload(context: Context, @RawRes resIds: List<Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            resIds.forEach { id ->
                if (!cache.containsKey(id)) {
                    val result = LottieCompositionFactory.fromRawResSync(context, id)
                    result.value?.let { composition ->
                        cache[id] = composition
                    }
                }
            }
        }
    }

    fun get(id: Int): LottieComposition? = cache[id]
}

// -----------------------------------------------------------------------------
// 4. JUICY CARD WITH HERO EXPANSION (MATCHED GEOMETRY SIMULATION)
// -----------------------------------------------------------------------------

@Composable
fun JuicyHeroCard(
    modifier: Modifier = Modifier,
    colors: JuicyColorScheme = JuicyTheme.getColors(),
    content: @Composable () -> Unit
) {
    // A stable container that animates changes
    // EquatableView logic is implicit in Compose if inputs are stable,
    // but we ensure the container is optimized.
    
    Surface(
        modifier = modifier
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = colors.primary.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        contentColor = colors.text
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun Modifier.juicyClick(
    haptics: JuicyHaptics,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null
        ) {
            haptics.play(JuicyHaptics.HapticStyle.MEDIUM)
            onClick()
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown()
                isPressed = true
                val up = waitForUpOrCancellation()
                isPressed = false
            }
        }
}
