package com.example.consucalcu

import kotlinx.coroutines.Job
import androidx.compose.material.icons.rounded.Share
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material3.MenuAnchorType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

// ========== REWARDS SYSTEM IMPORTS ==========
import com.example.consucalcu.rewards.*
import com.example.consucalcu.rewards.ui.*
// ============================================

// ========== ACADEMY SYSTEM IMPORTS ==========
import com.example.consucalcu.academy.*

// ========== HAPTIC FEEDBACK SYSTEM ==========
val LocalHapticManager = compositionLocalOf<HapticManager?> { null }

// =================================================================================================
// MAIN ACTIVITY
// =================================================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            // Inicializar Sistemas Premium
            val context = LocalContext.current
            val hapticManager = remember { HapticManager(context) }
            
            // Pre-load Lottie
            LaunchedEffect(Unit) {
               LottieCache.preload(context, listOf(
                   R.raw.lottie_success, 
                   R.raw.lottie_fire, 
                   R.raw.lottie_levelup,
                   R.raw.pop, // Sound but maybe user meant lottie? No, pop is mp3.
                   R.raw.lottie_loading
               ))
            }

            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            androidx.compose.runtime.CompositionLocalProvider(
                LocalHapticManager provides hapticManager
            ) {

            // Create ViewModel instance
            val viewModel: CalculatorViewModel = viewModel()
            
            // Observe rewards state for theme changes
            val rewardsState by viewModel.rewardsState.collectAsState()
            
            // Get active theme
            val activeTheme = remember(rewardsState.active.currentTheme) {
                ThemeCatalog.getThemeById(rewardsState.active.currentTheme)
                    ?: ThemeCatalog.ALL_THEMES.first()
            }
            
            // ========== ANIMATED COLOR TRANSITIONS ==========
            val animatedPrimary by animateColorAsState(
                targetValue = activeTheme.colors.primary,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "primaryColor"
            )
            
            val animatedSecondary by animateColorAsState(
                targetValue = activeTheme.colors.secondary,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "secondaryColor"
            )
            
            val animatedBackground by animateColorAsState(
                targetValue = if (isDarkTheme) {
                    activeTheme.colors.background
                } else {
                    // En modo claro, aplicar un tinte sutil del color primario al fondo
                    BgLight.blendWith(activeTheme.colors.primaryGlow, ratio = 0.08f)
                },
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "backgroundColor"
            )
            
            val animatedSurface by animateColorAsState(
                targetValue = if (isDarkTheme) {
                    activeTheme.colors.surface
                } else {
                    // En modo claro, aplicar un tinte sutil del color primario a las superficies
                    SurfaceLight.blendWith(activeTheme.colors.primaryGlow, ratio = 0.05f)
                },
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "surfaceColor"
            )
            
            val animatedAccent by animateColorAsState(
                targetValue = activeTheme.colors.accent,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "accentColor"
            )
            
            val animatedPrimaryShadow by animateColorAsState(
                targetValue = activeTheme.colors.primaryShadow,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "primaryShadow"
            )
            
            val animatedPrimaryGlow by animateColorAsState(
                targetValue = activeTheme.colors.primaryGlow,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "primaryGlow"
            )
            
            val animatedSecondaryShadow by animateColorAsState(
                targetValue = activeTheme.colors.secondaryShadow,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "secondaryShadow"
            )
            
            val animatedSecondaryGlow by animateColorAsState(
                targetValue = activeTheme.colors.secondaryGlow,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "secondaryGlow"
            )
            // ================================================
            
            // Create dynamic color scheme based on active theme with animated colors
            val dynamicColors = if (isDarkTheme) {
                darkColorScheme(
                    primary = animatedPrimary,
                    onPrimary = Color.White,
                    primaryContainer = animatedPrimaryShadow,
                    onPrimaryContainer = Color.White,
                    secondary = animatedSecondary,
                    onSecondary = Color.White,
                    secondaryContainer = animatedSecondaryShadow,
                    onSecondaryContainer = Color.White,
                    tertiary = animatedAccent,
                    onTertiary = Color.White,
                    background = animatedBackground,
                    onBackground = TextLight,
                    surface = animatedSurface,
                    onSurface = TextLight,
                    surfaceVariant = Color(0xFF252B42),
                    onSurfaceVariant = TextLight.copy(alpha = 0.7f),
                    outline = BorderDark,
                    error = ErrorRed,
                    onError = Color.White
                )
            } else {
                lightColorScheme(
                    primary = animatedPrimary,
                    onPrimary = Color.White,
                    primaryContainer = animatedPrimaryGlow,
                    onPrimaryContainer = TextDark,
                    secondary = animatedSecondary,
                    onSecondary = Color.White,
                    secondaryContainer = animatedSecondaryGlow,
                    onSecondaryContainer = TextDark,
                    tertiary = animatedAccent,
                    onTertiary = Color.White,
                    background = animatedBackground,
                    onBackground = TextDark,
                    surface = animatedSurface,
                    onSurface = TextDark,
                    surfaceVariant = Color(0xFFE8EDF5),
                    onSurfaceVariant = TextDark.copy(alpha = 0.7f),
                    outline = BorderLight,
                    error = ErrorRed,
                    onError = Color.White
                )
            }
            // ==========================================

            MaterialTheme(
                colorScheme = dynamicColors,  // Use dynamic colors instead of static
                typography = JuicyTypography
            ) {
                ConsuCalcuApp(
                    viewModel = viewModel,  // Pass the ViewModel instance
                    isDark = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
            }
        }
    }
}



// =================================================================================================
// 1. HELPERS & EXTENSIONS
// =================================================================================================
/**
 * Mezcla dos colores con alpha blending
 */
fun Color.blendWith(other: Color, ratio: Float = 0.5f): Color {
    val clampedRatio = ratio.coerceIn(0f, 1f)
    return Color(
        red = this.red * (1 - clampedRatio) + other.red * clampedRatio,
        green = this.green * (1 - clampedRatio) + other.green * clampedRatio,
        blue = this.blue * (1 - clampedRatio) + other.blue * clampedRatio,
        alpha = this.alpha * (1 - clampedRatio) + other.alpha * clampedRatio
    )
}

val BD_ZERO = BigDecimal.ZERO; val BD_500 = BigDecimal("500"); val BD_1000 = BigDecimal("1000")
fun String.toBD(): BigDecimal = this.replace(",", "").replace("$", "").trim().toBigDecimalOrNull() ?: BD_ZERO
fun Double.toBD(): BigDecimal = BigDecimal.valueOf(this)
fun BigDecimal.toCurrency(): String = DecimalFormat("#,###.00").format(this)

// =================================================================================================
// 1.1 NSS VALIDATION UTILITY
// =================================================================================================
/**
 * Valida un Número de Seguridad Social (NSS) mexicano usando el algoritmo Luhn modificado.
 * @return true si el NSS es válido, false en caso contrario.
 */
fun String.isValidNSS(): Boolean {
    if (this.length != 11) return false
    if (!this.all { it.isDigit() }) return false
    
    val digits = this.map { it.toString().toInt() }
    var sum = 0
    
    // Algoritmo Luhn (usado por IMSS)
    for (i in 0..9) {
        var digit = digits[i]
        if (i % 2 == 0) digit *= 2
        if (digit > 9) digit -= 9
        sum += digit
    }
    
    val checkDigit = (10 - (sum % 10)) % 10
    return checkDigit == digits[10]
}

// =================================================================================================
// 1.2 AMORTIZATION CALCULATOR
// =================================================================================================
data class AmortizationRow(
    val month: Int,
    val payment: BigDecimal,
    val interest: BigDecimal,
    val principal: BigDecimal,
    val balance: BigDecimal
)

/**
 * Calcula la tabla de amortización francesa para un crédito.
 * @param amount Monto del préstamo
 * @param annualRate Tasa de interés anual (ej. 0.24 para 24%)
 * @param term Plazo en meses
 * @return Lista de filas de amortización
 */
fun calculateAmortizationTable(
    amount: BigDecimal,
    annualRate: BigDecimal,
    term: Int
): List<AmortizationRow> {
    if (amount <= BigDecimal.ZERO || term <= 0) return emptyList()
    
    val monthlyRate = annualRate.divide(BigDecimal("12"), 10, RoundingMode.HALF_UP)
    val rows = mutableListOf<AmortizationRow>()
    var balance = amount
    
    // Cálculo del pago mensual fijo (Fórmula Francesa)
    val onePlusRate = BigDecimal.ONE.add(monthlyRate)
    val numerator = amount.multiply(monthlyRate).multiply(onePlusRate.pow(term))
    val denominator = onePlusRate.pow(term).subtract(BigDecimal.ONE)
    val payment = numerator.divide(denominator, 2, RoundingMode.HALF_UP)
    
    for (month in 1..term) {
        val interest = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP)
        val principal = payment.subtract(interest)
        balance = balance.subtract(principal).max(BigDecimal.ZERO)
        
        rows.add(
            AmortizationRow(
                month = month,
                payment = payment,
                interest = interest,
                principal = principal,
                balance = balance
            )
        )
    }
    
    return rows
}


// =================================================================================================
// 2. DESIGN SYSTEM & THEME - PALETA PREMIUM REFINADA
// =================================================================================================

// AZUL PREMIUM - Más profundo y profesional
val JuicyBlue = Color(0xFF2563EB)        // Azul más sofisticado (de 0xFF0A84FF)
val JuicyBlueShadow = Color(0xFF1D4ED8)  // Sombra más integrada
val JuicyBlueGlow = Color(0xFF60A5FA)    // Glow más suave

// VERDE ESMERALDA - Menos neón, más premium
val JuicyGreen = Color(0xFF10B981)       // Verde esmeralda (de 0xFF00E676)
val JuicyGreenShadow = Color(0xFF059669) // Sombra profunda
val JuicyGreenGlow = Color(0xFF34D399)   // Glow refinado

// NARANJA CÁLIDO - Más sofisticado
val JuicyOrange = Color(0xFFF97316)      // Naranja cálido (de 0xFFFF6D00)
val JuicyOrangeShadow = Color(0xFFEA580C) // Sombra más rica
val JuicyOrangeGlow = Color(0xFFFB923C)  // Glow suave

// ROJO CORAL - Más elegante
val JuicyRed = Color(0xFFEF4444)         // Rojo coral (de 0xFFFF1744)
val JuicyRedShadow = Color(0xFFDC2626)   // Sombra integrada
val JuicyRedGlow = Color(0xFFF87171)     // Glow suave

// PÚRPURA REFINADO - Más profundo
val JuicyPurple = Color(0xFF8B5CF6)      // Púrpura refinado (de 0xFF7C4DFF)
val JuicyPurpleShadow = Color(0xFF7C3AED) // Sombra rica
val JuicyPurpleGlow = Color(0xFFA78BFA)  // Glow elegante

// COLORES SECUNDARIOS - También refinados
val JuicyCyan = Color(0xFF06B6D4)        // Cyan más premium (de 0xFF00E5FF)
val JuicyPink = Color(0xFFEC4899)        // Rosa más elegante (de 0xFFFF4081)
val JuicyYellow = Color(0xFFFACC15)      // Amarillo dorado (de 0xFFFFEA00)
val JuicyLime = Color(0xFF84CC16)        // Lima más natural (de 0xFFC6FF00)

// GRADIENTES REFINADOS
val GradientBlue = Brush.linearGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
)
val GradientGreen = Brush.linearGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF14B8A6))
)
val GradientOrange = Brush.linearGradient(
    colors = listOf(Color(0xFFF97316), Color(0xFFFB923C))
)
val GradientPurple = Brush.linearGradient(
    colors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
)
val GradientSunset = Brush.linearGradient(
    colors = listOf(Color(0xFFF97316), Color(0xFFEF4444), Color(0xFFD946EF))
)

val BgLight = Color(0xFFF5F7FA)
val BgDark = Color(0xFF0F172A)        // Actualizado para coincidir con tema premium

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E293B)   // Actualizado para coincidir con tema premium

val TextDark = Color(0xFF0D1B2A)
val TextLight = Color(0xFFF0F4F8)

val BorderLight = Color(0xFFD0D7E2)
val BorderDark = Color(0xFF334155)    // Más claro para mejor contraste

// COLORES DE ESTADO - Actualizados a paleta premium
val SuccessGreen = Color(0xFF10B981)  // Verde esmeralda (de 0xFF00E676)
val WarningOrange = Color(0xFFF97316) // Naranja cálido (de 0xFFFF9100)
val ErrorRed = Color(0xFFEF4444)      // Rojo coral (de 0xFFFF1744)
val InfoBlue = Color(0xFF2563EB)      // Azul premium (de 0xFF0A84FF)

private val LightColorScheme = lightColorScheme(
    primary = JuicyBlue,
    onPrimary = Color.White,
    secondary = JuicyGreen,
    onSecondary = Color(0xFF003300),
    tertiary = JuicyPurple,
    onTertiary = Color.White,
    background = BgLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    outline = BorderLight,
    surfaceVariant = Color(0xFFE8EDF5),
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = JuicyBlue,
    onPrimary = Color.White,
    secondary = JuicyGreen,
    onSecondary = Color(0xFF003300),
    tertiary = JuicyPurple,
    onTertiary = Color.White,
    background = BgDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    outline = BorderDark,
    surfaceVariant = Color(0xFF252B42),
    error = ErrorRed,
    onError = Color.White
)

val JuicyTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-1).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
)

/**
 * CurrencyVisualTransformation with INTELLIGENT CURSOR MAPPING
 * 
 * Transforms raw input "10000" → "$10,000" with precise cursor positioning.
 * The cursor now correctly accounts for inserted special characters ($ and ,)
 * to prevent unexpected jumps when editing mid-string.
 * 
 * Algorithm:
 * - originalToTransformed: Counts commas that would appear BEFORE cursor position
 * - transformedToOriginal: Strips visual characters to find true data position
 */

// =================================================================================================
// 3. UI UTILS & MODIFIERS
// =================================================================================================
class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val parts = originalText.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else null

        val formattedInteger = try {
            if (integerPart.isNotEmpty()) {
                val num = integerPart.toLong()
                DecimalFormat("#,###").format(num)
            } else ""
        } catch (e: Exception) {
            integerPart
        }

        val newTextString = if (decimalPart != null) {
            "$formattedInteger.$decimalPart" // Sin $ inicial - ícono lo muestra
        } else {
            formattedInteger // Sin $ inicial - ícono lo muestra
        }

        val newText = AnnotatedString(newTextString)

        val offsetMapping = object : OffsetMapping {
            /**
             * Maps original cursor position to transformed (visual) position
             * Counts exactly how many commas are inserted before the cursor
             */
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0 // Sin $ inicial
                
                val beforeCursor = originalText.take(offset)
                
                val commasBeforeCursor = try {
                    val intPart = beforeCursor.split(".")[0]
                    if (intPart.isNotEmpty()) {
                        val numValue = intPart.toLongOrNull() ?: 0L
                        val formatted = DecimalFormat("#,###").format(numValue)
                        formatted.count { it == ',' }
                    } else 0
                } catch (e: Exception) {
                    0
                }
                
                return minOf(offset + commasBeforeCursor, newText.length) // Sin +1 porque no hay $
            }

            /**
             * Maps transformed (visual) cursor position back to original position
             * Strips out visual-only characters ($ and ,)
             */
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0 // Sin $ inicial
                
                val visualText = newTextString.take(offset)
                val dataChars = visualText.count { it.isDigit() || it == '.' }
                
                return minOf(dataChars, originalText.length)
            }
        }

        return TransformedText(newText, offsetMapping)
    }
}

fun Modifier.bouncyClick(soundManager: SoundManager? = null, onClick: () -> Unit) = composed {
    var isPressed by remember { mutableStateOf(false) }
    
    // Optimización: Cachear animationSpec
    val animSpec = remember { 
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh // Más rápido para mejor respuesta
        )
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f, // Más sutil: 0.97f en lugar de 0.95f
        label = "scale",
        animationSpec = animSpec
    )
    
    val haptic = LocalHapticFeedback.current

    this
        .graphicsLayer {
            // Optimización: graphicsLayer es más eficiente que Modifier.scale
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                val up = waitForUpOrCancellation()
                isPressed = false
                if (up != null) {
                    soundManager?.play("pop")
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            }
        }
}

/**
 * Modifier para efecto 3D de "hundirse" en botones
 * En lugar de escalar, mueve el botón hacia abajo y reduce la sombra
 * Retorna el estado isPressed para que el Composable pueda ajustar la sombra
 */
@Composable
fun rememberPressable3DState(): Pair<Boolean, Modifier> {
    var isPressed by remember { mutableStateOf(false) }
    
    val animSpec = remember { 
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }
    
    val pressDepth by animateFloatAsState(
        targetValue = if (isPressed) 4f else 0f, // Cuánto se "hunde"
        label = "pressDepth",
        animationSpec = animSpec
    )
    
    val modifier = Modifier
        .graphicsLayer {
            translationY = pressDepth // Mueve hacia abajo, no escala
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                waitForUpOrCancellation()
                isPressed = false
            }
        }
    
    return Pair(isPressed, modifier)
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(), targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart), label = "offset"
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFB8B5B5).copy(alpha = 0.2f),
                Color(0xFF8F8B8B).copy(alpha = 0.4f),
                Color(0xFFB8B5B5).copy(alpha = 0.2f)
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned { size = it.size }
}

/**
 * PREMIUM HAPTIC FEEDBACK SYSTEM
 * 
 * Triggers custom haptic patterns optimized for different Android versions:
 * - API 31+ (Android 12+): Uses VibrationEffect.Composition with PRIMITIVE_CLICK + PRIMITIVE_TICK
 *   for a mechanical "gear click" sensation that feels like physical hardware
 * - API 26-30 (Android 8-11): Falls back to waveform patterns
 * - API <26: No haptic feedback (graceful degradation)
 * 
 * @param context Android context for accessing vibrator service
 * @param pattern LongArray pattern [delay, vibrate, sleep, vibrate, ...] (used on API 26-30)
 * @param useComposition If true and API 31+, uses primitive composition for premium feel
 */

// =================================================================================================
// 4. HAPTICS SYSTEM
// =================================================================================================
fun triggerHapticTexture(context: Context, pattern: LongArray, useComposition: Boolean = true) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    vibrator?.let {
        if (!it.hasVibrator()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && useComposition) {
            try {
                val composition = VibrationEffect.startComposition()
                
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f, 0)
                
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.4f, 50)
                
                it.vibrate(composition.compose())
            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    it.vibrate(effect)
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, -1)
            it.vibrate(effect)
        }
    }
}

object HapticPatterns {
    val SUCCESS = longArrayOf(0, 10, 50, 20)
    val ERROR = longArrayOf(0, 50, 40, 50)
}

/**
 * Wraps content with staggered entrance animation using graphicsLayer
 * @param index Item index for stagger delay calculation
 * @param content Composable content to animate
 */
@Composable


// =================================================================================================
// 5. ANIMATION UTILS
// =================================================================================================
fun AnimatedResultItem(
    index: Int,
    content: @Composable () -> Unit
) {
    // Optimización: Usar Animatable para animación más eficiente
    val animProgress = remember { Animatable(0f) }
    
    // Spec de animación cacheado para evitar recreación
    val animSpec = remember {
        tween<Float>(
            durationMillis = 400,
            delayMillis = index * 40, // Stagger más rápido
            easing = FastOutSlowInEasing
        )
    }
    
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animSpec)
    }
    
    val progress = animProgress.value
    
    Box(
        modifier = Modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 30f // Reducido de 50f para más sutileza
            val scale = 0.92f + (progress * 0.08f) // De 0.92 a 1.0
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}

/**
 * Animated wrapper for tab content with floating entrance effect (SUTIL)
 * Triggers animation when tabKey changes (tab switch)
 */
@Composable
fun AnimatedTabContent(
    tabKey: Int,
    content: @Composable () -> Unit
) {
    // Optimización: Animatable para control preciso y mejor rendimiento
    val animProgress = remember { Animatable(0f) }
    
    // Spec cacheado - tween es más eficiente que spring para transiciones de página
    val animSpec = remember {
        tween<Float>(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        )
    }
    
    LaunchedEffect(tabKey) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animSpec)
    }
    
    val progress = animProgress.value
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Solo alpha y translationY - eliminar scale para mejor rendimiento
                alpha = progress
                translationY = (1f - progress) * 20f
            }
    ) {
        content()
    }
}

/**
 * Custom Navigation Bar Item con animaciones suaves y haptic feedback
 */
@Composable
fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Optimización: Cachear animationSpec
    val colorAnimSpec = remember { tween<Color>(250) }
    val floatAnimSpec = remember { tween<Float>(200, easing = FastOutSlowInEasing) }
    
    // 🎮 NUEVO: Haptic Manager
    val hapticManager = LocalHapticManager.current
    
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    
    val animatedColor by animateColorAsState(
        targetValue = if (selected) color else unselectedColor,
        animationSpec = colorAnimSpec,
        label = "navItemColor"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.85f,
        animationSpec = floatAnimSpec,
        label = "navItemScale"
    )
    
    val animatedBackgroundAlpha by animateFloatAsState(
        targetValue = if (selected) 0.15f else 0f,
        animationSpec = floatAnimSpec,
        label = "navItemBgAlpha"
    )
    
    // Optimización: Usar remember para interactionSource
    val interactionSource = remember { MutableInteractionSource() }
    
    Column(
        modifier = modifier
            .height(80.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Sin ripple para mejor rendimiento
                onClick = {
                    // ✨ Haptic diferenciado según estado
                    if (selected) {
                        hapticManager?.light() // Ya seleccionado = light
                    } else {
                        hapticManager?.medium() // Nuevo = medium
                    }
                    onClick()
                }
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    // Optimización: graphicsLayer es más eficiente que Modifier.scale
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .background(
                    color = color.copy(alpha = animatedBackgroundAlpha),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = animatedColor,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}


// =================================================================================================
// 6. SERVICES (SOUND & TICKET)
// =================================================================================================
class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()

    init {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attr)
            .build()
        try {
            soundMap["pop"] = soundPool.load(context, R.raw.pop, 1)
            soundMap["cash"] = soundPool.load(context, R.raw.cash, 1)
            soundMap["magic"] = soundPool.load(context, R.raw.magic, 1)
            soundMap["wrong"] = soundPool.load(context, R.raw.wrong, 1)

            soundMap["villager"] = soundPool.load(context, R.raw.villager_blip, 1)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play(name: String) {
        val volume = if (name == "villager") 0.6f else 1f
        val rate = if (name == "villager") (0.9f + Math.random().toFloat() * 0.2f) else 1f
        soundMap[name]?.let { soundPool.play(it, volume, volume, 1, 0, rate) }
    }

    fun release() {
        soundPool.release()
    }
}

// =================================================================================================
// HAPTIC FEEDBACK MANAGER - Premium Tactile Experience
// =================================================================================================
class HapticManager(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Light haptic - Para hover, focus, interacciones sutiles
     */
    fun light() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(10, 50))
        }
    }

    /**
     * Medium haptic - Para clicks, toggles, acciones estándar
     */
    fun medium() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, 100))
        }
    }

    /**
     * Heavy haptic - Para confirmaciones importantes, errores
     */
    fun heavy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(40, 200))
        }
    }

    /**
     * Success haptic - Patrón de doble pulse para acciones exitosas
     */
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 20, 50, 20)
            val amplitudes = intArrayOf(0, 100, 0, 150)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        }
    }

    /**
     * Error haptic - Patrón de triple pulse para errores
     */
    fun error() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 15, 30, 15, 30, 20)
            val amplitudes = intArrayOf(0, 80, 0, 80, 0, 120)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        }
    }

    /**
     * Tick haptic - Para sliders, incrementos numéricos
     */
    fun tick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(5, 40))
        }
    }
}

object TicketGenerator {
    suspend fun generateAndShare(
        context: Context,
        offer: LoanOffer,
        client: ClientData,
        advisor: String
    ) = withContext(Dispatchers.IO) {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // --- PALETA DE COLORES PREMIUM ---
        val colorDarkBg = android.graphics.Color.parseColor("#0F172A") // Slate 900
        val colorCard = android.graphics.Color.WHITE
        val colorPrimary = android.graphics.Color.parseColor("#3B82F6") // Blue 500
        val colorSuccess = android.graphics.Color.parseColor("#22C55E") // Green 500
        val colorAccent = android.graphics.Color.parseColor("#F59E0B") // Amber 500
        val colorTextMain = android.graphics.Color.parseColor("#1E293B") // Slate 800
        val colorTextSec = android.graphics.Color.parseColor("#64748B") // Slate 500
        val colorDivider = android.graphics.Color.parseColor("#E2E8F0") // Slate 200

        // --- PINCELES ---
        val bgPaint = Paint().apply { color = colorDarkBg; style = Paint.Style.FILL }
        val cardPaint = Paint().apply {
            color = colorCard
            style = Paint.Style.FILL
            setShadowLayer(40f, 0f, 20f, android.graphics.Color.parseColor("#88000000"))
        }

        val textTitle = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 60f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val textSubtitle = Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8") // Slate 400
            textSize = 36f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val textLabel = Paint().apply {
            color = colorTextSec
            textSize = 34f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val textValue = Paint().apply {
            color = colorTextMain
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        val textGold = Paint().apply {
            color = colorAccent
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val dividerPaint = Paint().apply {
            color = colorDivider
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        // 1. FONDO
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. HEADER
        canvas.drawText("CONSUCALCU PRO", width / 2f, 150f, textTitle)
        canvas.drawText("Simulación Oficial de Crédito", width / 2f, 210f, textSubtitle)

        // 3. TARJETA FLOTANTE
        val cardMargin = 60f
        val cardTop = 300f
        val cardBottom = height - 350f
        val cardRect = android.graphics.RectF(cardMargin, cardTop, width - cardMargin, cardBottom)
        canvas.drawRoundRect(cardRect, 40f, 40f, cardPaint)

        // 4. CONTENIDO TARJETA
        var y = cardTop + 100f
        val xLeft = cardMargin + 60f
        val xRight = width - cardMargin - 60f

        // Badge de Aprobado
        val badgeWidth = 400f
        val badgeHeight = 80f
        val badgeRect = android.graphics.RectF(
            (width - badgeWidth) / 2f,
            y,
            (width + badgeWidth) / 2f,
            y + badgeHeight
        )
        val badgePaint = Paint().apply { color = android.graphics.Color.parseColor("#DCFCE7"); style = Paint.Style.FILL } // Green 100
        canvas.drawRoundRect(badgeRect, 40f, 40f, badgePaint)
        
        val textBadge = Paint().apply {
            color = colorSuccess // Green 600
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val fontMetrics = textBadge.fontMetrics
        val badgeTextY = badgeRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText("¡PROPUESTA APROBADA!", width / 2f, badgeTextY, textBadge)

        y += 180f

        // Monto Gigante (Hero)
        val textCurrency = Paint().apply {
            color = colorPrimary
            textSize = 140f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(10f, 0f, 5f, android.graphics.Color.parseColor("#443B82F6"))
        }
        val textCurrencyLabel = Paint().apply {
            color = colorTextSec
            textSize = 36f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        canvas.drawText("MONTO AUTORIZADO", width / 2f, y, textCurrencyLabel)
        y += 140f
        canvas.drawText("$${offer.amount.toCurrency()}", width / 2f, y, textCurrency)
        
        y += 100f
        canvas.drawLine(xLeft, y, xRight, y, dividerPaint)
        y += 80f

        // Función Helper para filas
        fun drawDetailRow(label: String, value: String, highlight: Boolean = false) {
            canvas.drawText(label, xLeft, y, textLabel)
            
            // Safe Paint Clone
            val valPaint = Paint(textValue).apply {
                 if(highlight) {
                     color = colorPrimary
                     textSize = 50f
                 }
            }
            canvas.drawText(value, xRight, y, valPaint)
            y += 90f
        }

        drawDetailRow("Cliente", client.name.take(20) + if(client.name.length>20)"..." else "")
        drawDetailRow("Producto", offer.product)
        drawDetailRow("Plazo", "${offer.term} Meses")
        
        y += 20f
        // Caja secundaria para pago
        val paymentBg = Paint().apply { color = android.graphics.Color.parseColor("#F1F5F9"); style = Paint.Style.FILL } // Slate 100
        val paymentRect = android.graphics.RectF(cardMargin + 20f, y, width - cardMargin - 20f, y + 140f)
        canvas.drawRoundRect(paymentRect, 20f, 20f, paymentBg)
        
        val oldY = y
        y += 90f // Centrar verticalmente en la caja
        val paymentLabelX = xLeft + 40f
        val paymentValueX = xRight - 40f
        
        // Safe Paint Clones
        val paymentLabelPaint = Paint(textLabel).apply { typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("MENSUALIDAD", paymentLabelX, y, paymentLabelPaint)
        
        val paymentValuePaint = Paint(textValue).apply { textSize = 60f; color = colorTextMain }
        canvas.drawText("$${offer.monthlyPayment.toCurrency()}", paymentValueX, y, paymentValuePaint)
        
        y = oldY + 140f + 60f // Salir de la caja + margen

        drawDetailRow("CAT Promedio", offer.cat)
        drawDetailRow("Tasa de Interés", offer.tipoTasa)
        
        y += 40f
        canvas.drawLine(xLeft, y, xRight, y, dividerPaint)
        y += 80f
        
        // Footer de la tarjeta
        val textFooter = Paint().apply {
             color = colorTextSec
             textSize = 30f
             textAlign = Paint.Align.CENTER
             isAntiAlias = true
        }
        canvas.drawText("Oferta válida por 7 días. Sujeto a aprobación final.", width / 2f, cardBottom - 60f, textFooter)

        // 5. FOOTER GLOBAL
        val footerY = height - 150f
        canvas.drawText("ASESOR CERTIFICADO", width / 2f, footerY, textGold)
        
        val footerNamePaint = Paint(textTitle).apply { textSize = 44f }
        canvas.drawText(advisor.uppercase(), width / 2f, footerY + 60f, footerNamePaint)


        try {
            val filename = "Ticket_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ConsuCalcu")
                } else {
                    // Legacy Support (API < 29)
                    @Suppress("DEPRECATION")
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val imageDir = File(imagesDir, "ConsuCalcu")
                    if (!imageDir.exists()) {
                        imageDir.mkdirs()
                    }
                    val imageFile = File(imageDir, filename)
                    put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
                }
            }
            val imageUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            imageUri?.let { uri ->
                context.contentResolver.openOutputStream(uri)
                    ?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                withContext(Dispatchers.Main) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_STREAM, uri); type =
                        "image/jpeg"; addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(
                            shareIntent,
                            "Enviar Ticket Dorado"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Error generando ticket",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}


// =================================================================================================
// 7. DATA MODELS
// =================================================================================================
data class Debt(
    val id: Long = System.currentTimeMillis(),
    val product: String = "",
    val balance: String = "",
    val payment: String = "",
    val rate: String = "",
    val originalTerm: String = "",
    val paymentsMade: String = ""
)

@Parcelize
data class LoanOffer(
    val product: String,
    val amount: BigDecimal,
    val monthlyPayment: BigDecimal,
    val term: Int,
    val mainValue: BigDecimal = BD_ZERO,
    val mainLabel: String = "",
    val reasons: List<String> = emptyList(),
    val isBest: Boolean = false,
    val cat: String = "0.0%",
    val tipoTasa: String = "TRADICIONAL",
    val rejectionReason: String? = null // Razón por la que no calificó para retención
) : Parcelable

@Parcelize
data class ClientData(
    val name: String = "",
    val nss: String = "",
    val curp: String = "",
    val rfc: String = "",
    val email: String = "",
    val phone: String = "",
    val clabe: String = "",
    val bank: String = "",
    val age: String = "",
    val pensionType: String = "",
    val agreement: String = "IMSS",
    val disease: String = "NINGUNA",
    val diseaseDate: String = "",
    
    // Tipo de trámite
    val transactionType: String = "NUEVO", // NUEVO, CNCA, 2ª DISP, LCOM
    val creditId: String = "", // ID del crédito a liquidar
    val discountAmount: String = "", // Descuento
    val numDiscounts: String = "", // Número de descuentos
    val liquidatingBrand: String = "", // Marca que liquida
    
    // Datos del crédito nuevo (se llenarán automáticamente desde la oferta)
    // monto, plazo, descuento, cotizador ya vienen de LoanOffer
    val isCommissionable: String = "SÍ",
    
    // Referencias
    val ref1Name: String = "",
    val ref1Phone: String = "",
    val ref2Name: String = "",
    val ref2Phone: String = ""
) : Parcelable

data class UserProfile(
    val name: String = "",
    val username: String = "",
    val password: String = "",
    val supervisor: String = "",
    val xp: Int = 0,
    val level: Int = 1
)


// =================================================================================================
// 7.1 NORMATIVA IMSS (INSURANCE RULES)
// =================================================================================================
enum class TipoPension { INVALIDEZ, VEJEZ, OTRO }
enum class ResultadoSeguro { NINGUNO, UN_ANIO, PLAZO_COMPLETO, RECHAZO }

object NormativaIMSS {
    fun calcularReglaSeguro(
        edad: Int,
        plazoMeses: Int,
        tipoPension: TipoPension,
        esNuevo: Boolean,
        antiguedadMeses: Int
    ): ResultadoSeguro {
        val plazoAnios = plazoMeses / 12.0
        val edadFinal = edad + plazoAnios

        // Validación Global Actualizada:
        // 1. Si edadFinal > 85, es RECHAZO absouto.
        if (edadFinal > 85) return ResultadoSeguro.RECHAZO
        
        // 2. Si edadFinal está entre 80.01 y 85 (TODO TIPO DE PENSIÓN):
        // "Se puede considerar una edad máxima de 85 años... si el cliente realiza el pago del seguro por el primer año"
        if (edadFinal > 80) return ResultadoSeguro.UN_ANIO

        return when (tipoPension) {
            TipoPension.INVALIDEZ -> {
                // Caso INVALIDEZ
                if (edad in 51..70) {
                    // Si edad entre 51-70: Cobrar UN_ANIO si es nuevo o antigüedad <= 24 meses
                    if (esNuevo || antiguedadMeses <= 24) ResultadoSeguro.UN_ANIO else ResultadoSeguro.NINGUNO
                } else {
                    // Si edad fuera de rango pero (edad + plazo/12) > 75: Cobrar UN_ANIO
                    if (edadFinal > 75) ResultadoSeguro.UN_ANIO else ResultadoSeguro.NINGUNO
                }
            }
            TipoPension.VEJEZ -> {
                // Caso VEJEZ
                if (edad in 66..75) {
                    if (edadFinal > 75) {
                        // Si (edad + plazo/12) > 75: Cobrar PLAZO_COMPLETO
                        ResultadoSeguro.PLAZO_COMPLETO
                    } else if (esNuevo || antiguedadMeses <= 24) {
                        // Si no excede 75 y (es nuevo o antigüedad <= 24): Cobrar UN_ANIO
                        ResultadoSeguro.UN_ANIO
                    } else {
                        // Si no excede 75 y antigüedad > 24: NINGUNO
                        ResultadoSeguro.NINGUNO
                    }
                } else if (edad >= 76) {
                    // Si edad >= 76: NINGUNO (siempre que no viole la regla global de 80 años)
                    ResultadoSeguro.NINGUNO
                } else {
                    ResultadoSeguro.NINGUNO
                }
            }
            TipoPension.OTRO -> ResultadoSeguro.NINGUNO
        }
    }
}

// =================================================================================================
// 8. FINANCIAL CONFIGURATION
// =================================================================================================
object FinancialConfig {
    data class ProductMeta(val tasaRef: Double, val catTrad: String, val catRet: String)
    data class RetentionRule(val factors: Map<Int, BigDecimal>, val minAmount: BigDecimal)
    data class ProductData(
        val factorsTrad: Map<Int, BigDecimal>,
        val retention: RetentionRule? = null,
        val meta: ProductMeta,
        val minGlobal: BigDecimal,
        val minInc: BigDecimal,
        val minTermMap: Map<Int, BigDecimal> = emptyMap()
    )

    val PRODUCTS = mapOf(
        "CONSUBANCO" to ProductData(
            mapOf(
                60 to BigDecimal("34.47785"),
                54 to BigDecimal("36.50000"),
                48 to BigDecimal("38.34620"),
                36 to BigDecimal("45.48000"),
                12 to BigDecimal("105.00000")
            ),
            RetentionRule(
                mapOf(60 to BigDecimal("30.95418")),  // Factor de Pago Preferencial 60m
                BigDecimal("100000.0")  // Monto mínimo CNCA Refinanciamiento
            ),
            ProductMeta(2.50, "39.03%", "31.50%"),
            BigDecimal("5000"),
            BigDecimal("1000"),
            mapOf(
                60 to BigDecimal("40000"),
                54 to BigDecimal("95000"),
                48 to BigDecimal("100000"),
                36 to BigDecimal("145000"),
                12 to BigDecimal("145000")
            )
        ),
        "CONSUPAGO" to ProductData(
            factorsTrad = mapOf(
                60 to BigDecimal("36.88917"),
                54 to BigDecimal("38.35000"),
                48 to BigDecimal("40.37447"),
                36 to BigDecimal("46.54880"),
                24 to BigDecimal("59.77820"),
                12 to BigDecimal("101.15000")
            ),
            retention = RetentionRule(
                 mapOf(60 to BigDecimal("33.25726")),  // Factor de Pago Preferencial 60m
                 BigDecimal("50000.0")  // Monto mínimo CNCA Refinanciamiento
            ),
            meta = ProductMeta(2.73, "44.20%", "36.42%"),
            minGlobal = BigDecimal("5000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(
                60 to BigDecimal("10000"), 54 to BigDecimal("10000"),
                48 to BigDecimal("15000"), 36 to BigDecimal("15000"),
                24 to BigDecimal("35000"), 18 to BigDecimal("35000"), 12 to BigDecimal("35000")
            )
        ),
        "OPCIPRES" to ProductData(
            mapOf(
                60 to BigDecimal("35.98562"),
                54 to BigDecimal("37.65909"),
                48 to BigDecimal("39.86520"),
                36 to BigDecimal("46.47090"),
                24 to BigDecimal("59.92300"),
                12 to BigDecimal("105.0000")
            ),
            RetentionRule(
                mapOf(60 to BigDecimal("32.22283")),  // Factor de Pago Preferencial 60m
                BigDecimal("75000.0")  // Monto mínimo CNCA Refinanciamiento
            ),
            ProductMeta(2.67, "42.26%", "34.21%"),
            BigDecimal("5000"),
            BigDecimal("750"),
            mapOf(
                60 to BigDecimal("30000"),
                54 to BigDecimal("45000"),
                48 to BigDecimal("50000"),
                36 to BigDecimal("70000"),
                12 to BigDecimal("70000")
            )
        ),
        "MASNOMINA" to ProductData(
            factorsTrad = mapOf(
                60 to BigDecimal("37.06127"),
                48 to BigDecimal("40.45340"),
                36 to BigDecimal("46.62260"),
                24 to BigDecimal("59.80870"),
                12 to BigDecimal("105.00000")
            ),
            retention = RetentionRule(emptyMap(), BigDecimal("999999999.0")),
            meta = ProductMeta(2.73, "44.57%", "46.50%"),
            minGlobal = BigDecimal("8000"),
            minInc = BigDecimal("0"),
            minTermMap = emptyMap()
        )
    )
    val STANDARD_TERMS = listOf("12", "18", "24", "36", "48", "54", "60")
    
    // Factores de Seguro: [Producto][Plazo] -> Pair(Factor1erAño, FactorTotal)
    val INSURANCE_FACTORS = mapOf(
        "CONSUBANCO" to mapOf(
            36 to Pair(BigDecimal("0.020390"), BigDecimal("0.040555")),
            48 to Pair(BigDecimal("0.021213"), BigDecimal("0.055907")),
            54 to Pair(BigDecimal("0.021461"), BigDecimal("0.063849")),
            60 to Pair(BigDecimal("0.021671"), BigDecimal("0.072217"))
        ),
        "CONSUPAGO" to mapOf(
            12 to Pair(BigDecimal("0.012956"), BigDecimal("0.012956")),
            24 to Pair(BigDecimal("0.018685"), BigDecimal("0.026342")),
            36 to Pair(BigDecimal("0.020507"), BigDecimal("0.041028")),
            48 to Pair(BigDecimal("0.021362"), BigDecimal("0.056948")),
            54 to Pair(BigDecimal("0.021623"), BigDecimal("0.065283")),
            60 to Pair(BigDecimal("0.021827"), BigDecimal("0.073927"))
        ),
        "OPCIPRES" to mapOf(
            36 to Pair(BigDecimal("0.020501"), BigDecimal("0.041001")),
            48 to Pair(BigDecimal("0.021325"), BigDecimal("0.056689")),
            54 to Pair(BigDecimal("0.021576"), BigDecimal("0.064861")),
            60 to Pair(BigDecimal("0.021770"), BigDecimal("0.073298")),
            // Caso especial Retención 60m
            999 to Pair(BigDecimal("0.023390"), BigDecimal("0.076685"))
        ),
        "MASNOMINA" to mapOf(
            24 to Pair(BigDecimal("0.018687"), BigDecimal("0.026347")),
            36 to Pair(BigDecimal("0.020513"), BigDecimal("0.041051")),
            48 to Pair(BigDecimal("0.021367"), BigDecimal("0.056986")),
            60 to Pair(BigDecimal("0.021837"), BigDecimal("0.074045"))
        )
    )
}


// =================================================================================================
// 9. FINANCIAL ENGINE
// =================================================================================================
object FinancialEngine {
    data class CalculationResult(val amount: BigDecimal, val payment: BigDecimal)

    private val MC = MathContext(20, RoundingMode.HALF_UP)
    private val BD_1000 = BigDecimal("1000")
    private val BD_60 = BigDecimal("60")

    private fun roundSipre(value: BigDecimal, step: BigDecimal): BigDecimal {
        if (step.compareTo(BigDecimal.ZERO) == 0) return value
        return value.divide(step, 0, RoundingMode.FLOOR).multiply(step)
    }

    // REGLAS ESTÁNDAR 2026
    // Montos mínimos para Tasa Preferencial - CRÉDITO NUEVO (Sin deuda)
    val MIN_RET_NUEVO = mapOf(
        "CONSUBANCO" to BigDecimal("200000"),
        "OPCIPRES" to BigDecimal("125000"),
        "CONSUPAGO" to BigDecimal("100000"),
        "MASNOMINA" to BigDecimal("999999999")
    )
    
    // Montos mínimos para Tasa Preferencial - REFINANCIAMIENTO (CNCA)
    // Requiere además: 24 pagos mínimos realizados
    val MIN_RET_CNCA = mapOf(
        "CONSUBANCO" to BigDecimal("100000"),
        "OPCIPRES" to BigDecimal("75000"),
        "CONSUPAGO" to BigDecimal("50000"),
        "MASNOMINA" to BigDecimal("999999999")
    )
    
    // Factores Globales SIPRE para Liquidación de Deuda
    val SIPRE_FACTORS = mapOf(
        "CONSUBANCO" to BigDecimal("1.857251"),
        "OPCIPRES" to BigDecimal("1.933371"),
        "CONSUPAGO" to BigDecimal("1.9954353")
    )

    fun calculate(
        prodKey: String,
        value: BigDecimal,
        term: Int,
        isByAmount: Boolean,
        useRetention: Boolean,
        roundingStep: BigDecimal
    ): Pair<CalculationResult, String>? {
        val config = FinancialConfig.PRODUCTS[prodKey] ?: return null
        
        var limit = config.minTermMap[term]
        if (limit == null && config.minTermMap.isNotEmpty()) {
             limit = BigDecimal("999999999")
        }
        if (limit == null) {
            limit = config.minGlobal
        }

        val minMontoReal = limit ?: BigDecimal.ZERO

        var factor = config.factorsTrad[term]
        var tipoTasa = "TRADICIONAL"
        var isSipreGlobal = false

        var activeRoundingStep = if(roundingStep == BigDecimal.ZERO) config.minInc else roundingStep
        if (activeRoundingStep == BigDecimal.ZERO) activeRoundingStep = BigDecimal("100")

        if (useRetention && term == 60) {
            val globalFactor = config.retention?.factors?.get(60)
            if (globalFactor != null) {
                factor = globalFactor
                tipoTasa = "RETENCION"
                // isSipreGlobal = true // ERROR: Los factores de retención son "por mil", no globales. Usar lógica estándar.
                
            }
        } else if (useRetention) {
             val f = config.retention?.factors?.get(term)
             if (f != null) {
                 factor = f
                 tipoTasa = "RETENCION"
             }
        }

        if (factor == null || factor == BigDecimal.ZERO) return null

        
        if (isSipreGlobal) {
            
            var currentVal = value
            var workingByAmount = isByAmount

            for (i in 0 until 50) {
                val montoBase: BigDecimal
                
                if (workingByAmount) {
                     montoBase = roundSipre(currentVal, activeRoundingStep)
                } else {
                     val montoEst = currentVal.multiply(BD_60, MC).divide(factor, MC)
                     montoBase = roundSipre(montoEst, activeRoundingStep)
                }

                if (montoBase < minMontoReal) return null

                val totalTeorico = montoBase.multiply(factor, MC)

                val pagoMensual = totalTeorico.divide(BD_60, MC).setScale(2, RoundingMode.HALF_UP)

                if (!workingByAmount && pagoMensual > value) {
                    val nuevoMonto = montoBase.subtract(activeRoundingStep)
                    if (nuevoMonto < minMontoReal) return null
                    currentVal = nuevoMonto
                    workingByAmount = true
                    continue
                }

                return Pair(CalculationResult(montoBase, pagoMensual), tipoTasa)
            }
            return null

        } else {
            
            val montoFinal: BigDecimal
            val pagoFinal: BigDecimal
            
            if (isByAmount) {
                montoFinal = roundSipre(value, activeRoundingStep)
                val bruto = montoFinal.divide(BD_1000, MC).multiply(factor, MC)
                pagoFinal = bruto.setScale(2, RoundingMode.HALF_UP)
            } else {
                val bruto = value.divide(factor, MC).multiply(BD_1000, MC)
                montoFinal = roundSipre(bruto, activeRoundingStep)
                
                val check = montoFinal.divide(BD_1000, MC).multiply(factor, MC)
                pagoFinal = check.setScale(2, RoundingMode.HALF_UP)
                
            }
            
            if (montoFinal < minMontoReal) return null
            
            return Pair(CalculationResult(montoFinal, pagoFinal), tipoTasa)
        }
    }

    fun checkNormative(
        prodKey: String,
        debts: List<Debt>,
        offer: LoanOffer,
        config: FinancialConfig.ProductData
    ): Pair<Boolean, List<String>> {
        val reasons = mutableListOf<String>()
        val totalDebts = debts.sumOf { it.balance.toBD() }
        val remnant = offer.amount.subtract(totalDebts)
        val increase = remnant
        
        // Validación de liquidez positiva (obligatoria para todos)
        if (remnant <= BigDecimal.ZERO) return Pair(false, listOf("Rechazo: Sin Liquidez"))
        if (debts.isEmpty()) return Pair(true, listOf("Crédito Nuevo"))

        // MASNOMINA: Sin restricciones, solo requiere liquidez positiva
        if (prodKey == "MASNOMINA") {
             return Pair(true, listOf("✓ Viable: Liquidez Positiva")) 
        }

        val total = debts.size
        var cVal1 = 0  // Validación 1: Incremento de Tasa
        var cVal2 = 0  // Validación 2: Pagos Acumulados + Tasa Referencia
        var cVal3 = 0  // Validación 3: Incremento de Descuento
        var cVal4 = 0  // Validación 4: Incremento de Plazo
        
        val plazoNuevo = offer.term
        
        for (d in debts) {
            val dRate = d.rate.toDoubleOrNull() ?: 0.0
            val dPaid = d.paymentsMade.toIntOrNull() ?: 0
            val dTerm = d.originalTerm.toIntOrNull() ?: 0
            
            // ========== VALIDACIÓN 1: INCREMENTO DE TASA ==========
            var v1Ok = false
            when (prodKey) {
                "CONSUPAGO" -> v1Ok = true  // Sin restricción de tasa
                "OPCIPRES" -> if (dRate <= 2.57) v1Ok = true
                "CONSUBANCO" -> if (dRate <= 2.40) v1Ok = true
            }
            if (v1Ok) cVal1++

            // ========== VALIDACIÓN 2: PAGOS ACUMULADOS Y TASA DE REFERENCIA ==========
            // Requiere 16 pagos aplicados independientemente del plazo original
            var v2Ok = false
            when (prodKey) {
                "CONSUPAGO" -> v2Ok = true  // Sin restricción de tasa ni pagos
                "OPCIPRES" -> if (dPaid >= 16 && dRate <= 2.67) v2Ok = true
                "CONSUBANCO" -> if (dPaid >= 16 && dRate <= 2.57) v2Ok = true
            }
            if (v2Ok) cVal2++

            // ========== VALIDACIÓN 3: INCREMENTO DE DESCUENTO ==========
            // Incremento de descuento = Ajuste de liquidez
            var v3Ok = false
            when (prodKey) {
                "CONSUPAGO" -> v3Ok = true  // Sin restricción
                "OPCIPRES" -> if (increase >= BigDecimal("750") && dRate <= 2.67) v3Ok = true
                "CONSUBANCO" -> if (increase >= BigDecimal("1000") && dRate <= 2.57) v3Ok = true
            }
            if (v3Ok) cVal3++

            // ========== VALIDACIÓN 4: INCREMENTO DE PLAZO ==========
            // Aplica a todos los productos (CONSUBANCO, OPCIPRES, MASNOMINA, CONSUPAGO)
            var v4Ok = false
            if (dTerm == 12 && plazoNuevo >= 36) v4Ok = true
            else if (dTerm == 24 && plazoNuevo >= 48) v4Ok = true
            else if ((dTerm == 36 || dTerm == 48) && plazoNuevo >= 54) v4Ok = true
            else if (dTerm == 54 && plazoNuevo >= 60) v4Ok = true
            
            if (v4Ok) cVal4++
        }

        // Agregar razones para validaciones que TODAS las deudas cumplieron
        if (cVal1 == total) reasons.add("✓ Validación 1: Mejora de Tasa")
        if (cVal2 == total) reasons.add("✓ Validación 2: Antigüedad (16+ pagos)")
        if (cVal3 == total) reasons.add("✓ Validación 3: Incremento de Liquidez")
        if (cVal4 == total) reasons.add("✓ Validación 4: Extensión de Plazo")

        // Viable si cumple AL MENOS UNA de las 4 validaciones
        return if (reasons.isNotEmpty()) {
            Pair(true, reasons)
        } else {
            Pair(false, listOf("No cumple ninguna validación CNCA (1-4)"))
        }
    }
}


// =================================================================================================
// 10. VIEWMODEL STATE
// =================================================================================================
data class CalculatorUiState(
    val selectedTab: Int = 0,
    val debts: List<Debt> = listOf(Debt()),
    val capacityCNCA: String = "",
    val offersCNCA: List<LoanOffer> = emptyList(),
    val newCreditInput: String = "",
    val isByAmount: Boolean = false,
    val offersNew: List<LoanOffer> = emptyList(),
    val showClientForm: Boolean = false,
    val showConfigDialog: Boolean = false,
    val showSuccessAnim: Boolean = false,
    val showOfferPreview: Boolean = false,
    val inputError: Boolean = false,
    val selectedOfferForLayout: LoanOffer? = null,
    val clientData: ClientData = ClientData(),
    val userProfile: UserProfile = UserProfile(),
    val showTutorial: Boolean = false,
    val tutorialStep: Int = 0,
    val showOnboarding: Boolean = false,
    val isLoading: Boolean = false,
    val showConfetti: Boolean = false,
    val streak: Int = 0,
    val useRetention: Boolean = false,
    val showStreakOverlay: Boolean = false,
    val showLevelUpOverlay: Boolean = false,
    val unlockedTitle: String? = null,
    val mandatoryConfigNeeded: Boolean = false,
    
    // Ticket Generation State
    val isGeneratingTicket: Boolean = false,
    
    // ========== REWARDS SYSTEM STATE ==========
    val showRewardUnlockOverlay: Boolean = false,
    val showRewardsGallery: Boolean = false,
    // ==========================================
    
    // Insurance Module State
    val insPensionType: String = "Invalidez",
    val insAge: String = "",
    val insAmount: String = "",
    val insTerm: Int = 60,
    val insProvider: String = "CONSUBANCO",
    val insIsNewClient: Boolean = false,
    val insAntiquity: String = "", // Added antiquity field
    val insResultStatus: String = "",
    val insResultColor: Color = Color.Transparent,
    val insCost1Year: BigDecimal = BD_ZERO,
    val insCostTotal: BigDecimal = BD_ZERO,
    val insMonthlyPayment: BigDecimal = BD_ZERO, // Added PMT field
    
    // ========== NEW FEATURES STATE ==========
    // Comparador de Ofertas
    val showOffersComparator: Boolean = false,
    
    // Simulador de Amortización
    val showAmortizationSimulator: Boolean = false,
    val selectedOfferForAmortization: LoanOffer? = null,
    val amortizationTable: List<AmortizationRow> = emptyList(),
    

)



// =================================================================================================
// 11. VIEWMODEL LOGIC
// =================================================================================================
class CalculatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState = _uiState.asStateFlow()
    private val gson = Gson()
    private var saveJob: Job? = null
    
    // ========== REWARDS SYSTEM ==========
    private lateinit var rewardsManager: RewardsManager
    private val _fallbackRewardsState = MutableStateFlow(RewardsState())
    
    val rewardsState: StateFlow<RewardsState>
        get() = if (::rewardsManager.isInitialized) {
            rewardsManager.rewardsState
        } else {
            _fallbackRewardsState
        }
    // ====================================



    fun loadData(ctx: Context) {
        // ========== INITIALIZE REWARDS SYSTEM ==========
        if (!::rewardsManager.isInitialized) {
            rewardsManager = RewardsManager(ctx)
        }
        // ===============================================
        
        val prefs = ctx.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE)

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val today = sdf.format(java.util.Date())

        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DATE, -1)
        val yesterday = sdf.format(cal.time)

        val lastDate = prefs.getString("last_streak_date", "") ?: ""
        var currentStreak = prefs.getInt("current_streak", 0)
        val previousStreak = currentStreak
        var shouldShowStreakOverlay = false

        if (lastDate != today) {
            if (lastDate == yesterday) {
                currentStreak++
                if (currentStreak > 1) {
                    shouldShowStreakOverlay = true
                }
            } else {
                currentStreak = 1
            }
            prefs.edit()
                .putString("last_streak_date", today)
                .putInt("current_streak", currentStreak)
                .apply()
        }

        val p = UserProfile(
            prefs.getString("name", "") ?: "",
            prefs.getString("username", "") ?: "",
            prefs.getString("password", "") ?: "",
            prefs.getString("supervisor", "") ?: "",
            prefs.getInt("xp", 0),
            prefs.getInt("level", 1)
        )
        val debtsJson = prefs.getString("debts", null)
        val debtsList: List<Debt> = if (debtsJson != null) gson.fromJson(
            debtsJson,
            object : TypeToken<List<Debt>>() {}.type
        ) else listOf(Debt(id = System.nanoTime()))

        val clientJson = prefs.getString("client", null)
        val client: ClientData = if (clientJson != null) gson.fromJson(
            clientJson,
            ClientData::class.java
        ) else ClientData()

        val onboardingSeen = prefs.getBoolean("onboarding_seen", false)

        _uiState.update {
            it.copy(
                userProfile = p,
                debts = debtsList,
                clientData = client,
                capacityCNCA = prefs.getString("capCNCA", "") ?: "",
                showOnboarding = !onboardingSeen,
                streak = currentStreak,
                showStreakOverlay = shouldShowStreakOverlay,
                
                // --- RECUPERAR DATOS SEGURO ---
                insAge = prefs.getString("insAge", "") ?: "",
                insAmount = prefs.getString("insAmount", "") ?: "",
                insTerm = prefs.getInt("insTerm", 60),
                insProvider = prefs.getString("insProvider", "CONSUBANCO") ?: "CONSUBANCO",
                insPensionType = prefs.getString("insPensionType", "Invalidez") ?: "Invalidez",
                insIsNewClient = prefs.getBoolean("insIsNew", false),
                insAntiquity = prefs.getString("insAntiquity", "") ?: "",
                



                
                // Verificar si falta configuración obligatoria
                mandatoryConfigNeeded = p.name.isBlank() || p.username.isBlank() || p.password.isBlank() || p.supervisor.isBlank()
            )
        }
    }

    fun saveData(ctx: Context) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(1000) // Debounce de 1 segundo para no saturar disco
            val s = _uiState.value;
            val prefs = ctx.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("debts", gson.toJson(s.debts))
                .putString("client", gson.toJson(s.clientData))
                .putString("capCNCA", s.capacityCNCA)
                // --- NUEVOS CAMPOS SEGURO ---
                .putString("insAge", s.insAge)
                .putString("insAmount", s.insAmount)
                .putInt("insTerm", s.insTerm)
                .putString("insProvider", s.insProvider)
                .putString("insPensionType", s.insPensionType)
                .putBoolean("insIsNew", s.insIsNewClient)
                .putString("insAntiquity", s.insAntiquity)

                .apply()
        }
    }

    fun saveProfile(ctx: Context, p: UserProfile) {
        val prefs = ctx.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("name", p.name).putString("username", p.username)
            .putString("password", p.password).putString("supervisor", p.supervisor)
            .putInt("xp", p.xp).putInt("level", p.level).apply(); _uiState.update {
            it.copy(
                userProfile = p,
                showConfigDialog = false,
                mandatoryConfigNeeded = false
            )
        }
    }

    private fun addXp(ctx: Context) {
        val p = _uiState.value.userProfile
        var nx = p.xp + 10
        var nl = p.level
        
        if (nx >= nl * 100) {
            nl++
            nx = 0
            
            // ========== CHECK FOR UNLOCKED REWARDS ==========
            val newUnlocks = if (::rewardsManager.isInitialized) {
                rewardsManager.checkAndUnlockRewards(nl)
            } else {
                emptyList()
            }
            // ================================================
            
            val unlockedTitle = when (nl) {
                1 -> "Novato"
                5 -> "Experto Financiero"
                10 -> "Lobo de Wall St"
                20 -> "Leyenda"
                else -> null
            }
            
            _uiState.update {
                it.copy(
                    showLevelUpOverlay = true,
                    unlockedTitle = unlockedTitle,
                    // ========== SHOW REWARD OVERLAY IF UNLOCKS ==========
                    showRewardUnlockOverlay = newUnlocks.isNotEmpty()
                    // ====================================================
                )
            }
        }
        
        saveProfile(ctx, p.copy(xp = nx, level = nl))
    }

    /**
     * Returns a personalized greeting based on time of day
     * ¡Buen día (6-12), ¡Buena tarde (12-19), ¡Buena noche (19-6)
     */
    fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..11 -> "¡Buen día"
            in 12..18 -> "¡Buena tarde"
            else -> "¡Buena noche"
        }
    }

    fun completeOnboarding(ctx: Context) {
        ctx.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE).edit()
            .putBoolean("onboarding_seen", true).apply(); _uiState.update {
            it.copy(
                showOnboarding = false
            )
        }
    }

    fun checkTutorialStatus(ctx: Context) {
        if (_uiState.value.showOnboarding) return; _uiState.update {
            it.copy(
                showTutorial = !ctx.getSharedPreferences(
                    "ConsuCalcuPrefs",
                    Context.MODE_PRIVATE
                ).getBoolean("rpg_tutorial_seen", false)
            )
        }
    }

    fun advanceTutorial() {
        _uiState.update { it.copy(tutorialStep = it.tutorialStep + 1) }
    }

    fun closeTutorial(ctx: Context) {
        ctx.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE).edit()
            .putBoolean("rpg_tutorial_seen", true).apply(); _uiState.update {
            it.copy(
                showTutorial = false,
                tutorialStep = 0
            )
        }
    }

    fun resetTutorial() {
        _uiState.update { it.copy(showTutorial = true, tutorialStep = 0) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun addDebtRow() {
        _uiState.update { it.copy(debts = it.debts + Debt(id = System.nanoTime())) }
    }

    fun removeDebtRow(debt: Debt) {
        _uiState.update { it.copy(debts = it.debts - debt) }
    }

    fun updateDebt(id: Long, newDebt: Debt) {
        _uiState.update { s -> s.copy(debts = s.debts.map { if (it.id == id) newDebt else it }) }
    }

    fun updateCapacityCNCA(v: String) {
        _uiState.update { it.copy(capacityCNCA = v) }
    }

    fun clearRefinanceData() {
        _uiState.update {
            it.copy(
                debts = listOf(Debt(id = System.nanoTime())),
                capacityCNCA = "",
                offersCNCA = emptyList()
            )
        }
    }

    fun updateNewCreditInput(v: String) {
        _uiState.update { it.copy(newCreditInput = v) }
    }

    fun toggleCalculationMode(byAmount: Boolean) {
        _uiState.update { it.copy(isByAmount = byAmount) }
    }

    fun clearNewCreditData() {
        _uiState.update { it.copy(newCreditInput = "", offersNew = emptyList()) }
    }

    fun toggleConfigDialog(show: Boolean) {
        _uiState.update { it.copy(showConfigDialog = show) }
    }

    fun updateClientData(data: ClientData) {
        _uiState.update { it.copy(clientData = data) }
    }

    fun onSelectOffer(offer: LoanOffer) {
        _uiState.update { it.copy(selectedOfferForLayout = offer, showOfferPreview = true) }
    }

    fun dismissPreview() {
        _uiState.update { it.copy(showOfferPreview = false) }
    }

    fun continueToClientForm() {
        _uiState.update { it.copy(showOfferPreview = false, showClientForm = true) }
    }

    fun dismissForm() {
        _uiState.update { it.copy(showClientForm = false) }
    }

    fun triggerSuccess() {
        _uiState.update { it.copy(showSuccessAnim = true) }
    }

    fun hideSuccess() {
        _uiState.update { it.copy(showSuccessAnim = false) }
    }

    fun dismissStreak() {
        _uiState.update { it.copy(showStreakOverlay = false) }
    }

    fun dismissLevelUp() {
        _uiState.update { it.copy(showLevelUpOverlay = false, unlockedTitle = null) }
    }
    
    // ========== REWARDS SYSTEM FUNCTIONS ==========
    
    fun dismissRewardUnlock() {
        if (::rewardsManager.isInitialized) {
            rewardsManager.clearNewUnlocks()
        }
        _uiState.update { it.copy(showRewardUnlockOverlay = false) }
    }
    
    fun activateTheme(themeId: String) {
        if (::rewardsManager.isInitialized) {
            rewardsManager.activateTheme(themeId)
        }
    }
    
    fun activateParticle(particleId: String) {
        if (::rewardsManager.isInitialized) {
            rewardsManager.activateParticle(particleId)
        }
    }
    
    fun activateAvatar(avatarId: String) {
        if (::rewardsManager.isInitialized) {
            rewardsManager.activateAvatar(avatarId)
        }
    }
    
    fun activateCardStyle(cardStyleId: String) {
        if (::rewardsManager.isInitialized) {
            rewardsManager.activateCardStyle(cardStyleId)
        }
    }
    
    fun openRewardsGallery() {
        _uiState.update { it.copy(showRewardsGallery = true) }
    }
    
    fun closeRewardsGallery() {
        _uiState.update { it.copy(showRewardsGallery = false) }
    }
    
    // ==============================================


    fun toggleRetention(enabled: Boolean) {
        _uiState.update { it.copy(useRetention = enabled) }
    }

    fun triggerConfetti() {
        _uiState.update { it.copy(showConfetti = true) }; viewModelScope.launch {
            delay(2000); _uiState.update {
            it.copy(
                showConfetti = false
            )
        }
        }
    }

    fun calculateRefinance(ctx: Context, soundManager: SoundManager) {
        viewModelScope.launch {
            val current = _uiState.value
            val totalDebts = current.debts.sumOf { it.balance.toBD() }
            val cap = current.capacityCNCA.toBD()
            
            if (totalDebts <= BD_ZERO && cap == BD_ZERO) {
                 _uiState.update { it.copy(inputError = true) }
                 soundManager.play("wrong")
                 triggerHapticTexture(ctx, HapticPatterns.ERROR)
                 delay(500)
                 _uiState.update { it.copy(inputError = false) }
                 return@launch
            }

            _uiState.update { it.copy(isLoading = true, offersCNCA = emptyList()) }
            delay(1500)
            val calculatedOffers = withContext(Dispatchers.Default) {
                val state = _uiState.value;
                val cap = state.capacityCNCA.toBD();
                
                if (state.clientData.nss.isNotEmpty()) {
                    val nss = state.clientData.nss
                    if (nss.length != 11) {
                    }
                }

                val debtBalance = state.debts.sumOf { it.balance.toBD() };
                val debts = state.debts.filter { it.balance.toBD() > BD_ZERO };
                val results = mutableListOf<LoanOffer>()
                
                
                val totalPagosDeudas = state.debts.sumOf { it.payment.toBD() }
                val capacidadTotal = totalPagosDeudas.add(cap)

                if (capacidadTotal > BD_ZERO && debts.isNotEmpty()) {
                    // Detectar plazos originales de las deudas
                    val originalTerms = debts.mapNotNull { it.originalTerm.toIntOrNull() }.distinct()
                    
                    // Determinar qué plazos calcular según las reglas de extensión de plazo
                    // Reglas CNCA:
                    // - 12 meses → 36+
                    // - 24 meses → 48+
                    // - 36/48 meses → 54+
                    // - 54 meses → 60
                    val termsToCalculate = mutableSetOf<Int>()
                    
                    originalTerms.forEach { origTerm ->
                        when (origTerm) {
                            12 -> {
                                // Puede ir a 36, 48, 54, 60
                                termsToCalculate.addAll(listOf(36, 48, 54, 60))
                            }
                            24 -> {
                                // Puede ir a 48, 54, 60
                                termsToCalculate.addAll(listOf(48, 54, 60))
                            }
                            36, 48 -> {
                                // Puede ir a 54 o 60
                                termsToCalculate.addAll(listOf(54, 60))
                            }
                            54 -> {
                                // Solo puede ir a 60
                                termsToCalculate.add(60)
                            }
                            else -> {
                                // Plazo no estándar, por defecto 60
                                termsToCalculate.add(60)
                            }
                        }
                    }
                    
                    // Si no hay plazos determinados, usar 60 por defecto
                    if (termsToCalculate.isEmpty()) {
                        termsToCalculate.add(60)
                    }
                    
                    FinancialConfig.PRODUCTS.forEach { (prodKey, config) ->
                        termsToCalculate.forEach { term ->
                            var retencionAplicada = false
                            
                            // Validar 24 pagos mínimos para Retención en CNCA
                            val minPagosRetencion = 24
                            val minPagosActuales = debts.minOfOrNull { it.paymentsMade.toIntOrNull() ?: 0 } ?: 0
                            val cumplePagos = minPagosActuales >= minPagosRetencion

                            if (cumplePagos) {
                                val calcRet = FinancialEngine.calculate(
                                    prodKey,
                                    capacidadTotal,
                                    term,
                                    false,
                                    true, // useRetention = true
                                    BD_500
                                )
                                if (calcRet != null && calcRet.second == "RETENCION") {
                                    val (res, tipo) = calcRet
                                    val offerRet = LoanOffer(
                                        prodKey,
                                        res.amount,
                                        res.payment,
                                        term,
                                        cat = config.meta.catRet,
                                        tipoTasa = tipo
                                    )
                                    // Usar MIN_RET_CNCA para refinanciamiento
                                    val minRetCNCA = FinancialEngine.MIN_RET_CNCA[prodKey] ?: BigDecimal.ZERO
                                    if (offerRet.amount >= minRetCNCA) {
                                        val (viable, reasons) = FinancialEngine.checkNormative(
                                            prodKey,
                                            debts,
                                            offerRet,
                                            config
                                        )
                                        if (viable && offerRet.amount >= debtBalance) {
                                            val remnant = offerRet.amount.subtract(debtBalance)
                                            val finalReasons = reasons.toMutableList().apply {
                                                add("Tasa Preferencial (24+ pagos)")
                                            }
                                            results.add(
                                                offerRet.copy(
                                                    mainValue = remnant,
                                                    mainLabel = "REMANENTE",
                                                    reasons = finalReasons,
                                                    isBest = true
                                                )
                                            )
                                            retencionAplicada = true
                                        }
                                    }
                                }
                            }
                            
                            // FALLBACK: Si no aplicó retención, calcular tradicional
                            if (!retencionAplicada) {
                                val calcData = FinancialEngine.calculate(
                                    prodKey,
                                    capacidadTotal,
                                    term,
                                    false,
                                    false, // useRetention = false
                                    BD_500
                                )
                                if (calcData != null) {
                                    val (res, tipo) = calcData
                                    val offer = LoanOffer(
                                        prodKey,
                                        res.amount,
                                        res.payment,
                                        term,
                                        cat = config.meta.catTrad,
                                        tipoTasa = tipo
                                    )
                                    if (offer.amount >= config.minGlobal) {
                                        val (viable, reasons) = FinancialEngine.checkNormative(
                                            prodKey,
                                            debts,
                                            offer,
                                            config
                                        )
                                        if (viable && offer.amount >= debtBalance) {
                                            val remnant = offer.amount.subtract(debtBalance)
                                            results.add(
                                                offer.copy(
                                                    mainValue = remnant,
                                                    mainLabel = "REMANENTE",
                                                    reasons = reasons
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Ordenar y marcar ofertas de retención como "mejor opción"
                results.sortedByDescending { it.mainValue }
                    .mapIndexed { i, offer ->
                        offer.copy(isBest = offer.tipoTasa == "RETENCION" || i == 0)
                    }
            }
            if (calculatedOffers.isNotEmpty()) {
                soundManager.play("cash"); addXp(ctx); triggerConfetti()
                triggerHapticTexture(ctx, HapticPatterns.SUCCESS)
            } else {
                // Sin ofertas disponibles
                soundManager.play("wrong")
                triggerHapticTexture(ctx, HapticPatterns.ERROR)
            }
            _uiState.update { it.copy(isLoading = false, offersCNCA = calculatedOffers) }
        }
    }

    fun calculateNewCredit(ctx: Context, soundManager: SoundManager) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current.newCreditInput.toBD() <= BD_ZERO) {
                 _uiState.update { it.copy(inputError = true) }
                 soundManager.play("wrong")
                 triggerHapticTexture(ctx, HapticPatterns.ERROR)
                 delay(500)
                 _uiState.update { it.copy(inputError = false) }
                 return@launch
            }
            _uiState.update { it.copy(isLoading = true, offersNew = emptyList()) }
            delay(1500)
            val calculatedOffers = withContext(Dispatchers.Default) {
                val state = _uiState.value;
                val inputVal = state.newCreditInput.toBD();
                val results = mutableListOf<LoanOffer>()
                if (inputVal > BD_ZERO) {
                    FinancialConfig.PRODUCTS.forEach { (prodKey, config) ->
                        if (state.useRetention) {
                            // MODO RETENCIÓN: Solo calcular ofertas de retención para 60 meses
                            // NO SE MUESTRAN OFERTAS TRADICIONALES cuando este modo está activo
                            val term = 60
                            val calcRet = FinancialEngine.calculate(
                                prodKey,
                                inputVal,
                                term,
                                state.isByAmount,
                                true,
                                BigDecimal("100")
                            )
                            if (calcRet != null && calcRet.second == "RETENCION") {
                                val (res, tipo) = calcRet
                                val minRetNuevo = FinancialEngine.MIN_RET_NUEVO[prodKey] ?: BigDecimal("999999999")
                                if (res.amount >= minRetNuevo) {
                                    results.add(
                                        LoanOffer(
                                            prodKey,
                                            res.amount,
                                            res.payment,
                                            term,
                                            mainValue = res.amount,
                                            mainLabel = "MONTO PRÉSTAMO",
                                            cat = config.meta.catRet,
                                            tipoTasa = tipo,
                                            isBest = true
                                        )
                                    )
                                }
                            }
                            // No hay fallback a tradicional - solo ofertas preferenciales
                        } else {
                            // MODO TRADICIONAL: Calcular todas las ofertas tradicionales
                            config.factorsTrad.keys.forEach { term ->
                                val calcTrad = FinancialEngine.calculate(
                                    prodKey,
                                    inputVal,
                                    term,
                                    state.isByAmount,
                                    false,
                                    BigDecimal("100")
                                )
                                if (calcTrad != null) {
                                    val (res, tipo) = calcTrad
                                    val minRequired = config.minTermMap[term] ?: config.minGlobal
                                    if (res.amount >= minRequired && res.amount >= config.minGlobal) {
                                        results.add(
                                            LoanOffer(
                                                prodKey,
                                                res.amount,
                                                res.payment,
                                                term,
                                                mainValue = res.amount,
                                                mainLabel = "MONTO PRÉSTAMO",
                                                cat = config.meta.catTrad,
                                                tipoTasa = tipo
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Ordenar y marcar ofertas de retención como "mejor opción"
                results.sortedWith(
                    compareByDescending<LoanOffer> { it.amount }.thenBy { it.monthlyPayment }
                ).mapIndexed { i, offer ->
                    offer.copy(isBest = offer.tipoTasa == "RETENCION" || i == 0)
                }
            }
            if (calculatedOffers.isNotEmpty()) {
                soundManager.play("cash"); addXp(ctx); triggerConfetti()
                triggerHapticTexture(ctx, HapticPatterns.SUCCESS)
            } else {
                // Sin ofertas disponibles
                soundManager.play("wrong")
                triggerHapticTexture(ctx, HapticPatterns.ERROR)
            }
            _uiState.update { it.copy(isLoading = false, offersNew = calculatedOffers) }
        }
    }

    fun generateLayout(): String {
        val s = _uiState.value
        val d = s.clientData
        val p = s.userProfile
        val o = s.selectedOfferForLayout ?: return ""
        
        // Determinar tipo de trámite basado en la oferta
        val isNuevo = d.transactionType == "NUEVO"
        val isCNCA = d.transactionType == "CNCA"
        val is2Disp = d.transactionType == "2ª DISP"
        val isLCOM = d.transactionType == "LCOM"
        
        return """
SOLICITUD DE CAPTURA

USUARIO: ${p.username}
CONTRASEÑA: ${p.password}

CONVENIO: ${d.agreement}
TIPO DE ORIGINACIÓN: ${o.product}

DATOS DE CLIENTE
-NOMBRE: ${d.name.uppercase()}
-NSS: ${d.nss}
-CURP: ${d.curp.uppercase()}
-RFC (CON HOMOCLAVE): ${d.rfc.uppercase()}
-CORREO: ${d.email}
-TELEFONO: ${d.phone}
-CLABE Y BANCO: ${d.clabe} - ${d.bank.uppercase()}
-EDAD: ${d.age}
-PENSIÓN: ${d.pensionType}

CUESTIONARIO MEDICO
-ENFERMEDAD: ${d.disease}
-FECHA DE INICIO DE PADECIMIENTO: ${d.diseaseDate}

TIPO DE TRÁMITE
-NUEVO(${if (isNuevo) "X" else " "})	CNCA(${if (isCNCA) "X" else " "})	2ª DISP(${if (is2Disp) "X" else " "})	LCOM(${if (isLCOM) "X" else " "})
-ID (CRÉDITO A LIQUIDAR): ${d.creditId}
-DESCUENTO: ${d.discountAmount}
-NÚM DE DESCUENTOS: ${d.numDiscounts}
-MARCA QUE LIQUIDA: ${d.liquidatingBrand}

DATOS DEL CRÉDITO NUEVO
-MONTO: ${'$'}${o.amount.toCurrency()}
-PLAZO: ${o.term} meses
-DESCUENTO: ${'$'}${o.monthlyPayment.toCurrency()}
-COTIZADOR: ${o.tipoTasa}
-COMISIONABLE: ${d.isCommissionable}

REFERENCIAS
1ª ${d.ref1Name} - ${d.ref1Phone}
2ª ${d.ref2Name} - ${d.ref2Phone}

PROMOTOR (NOMBRE COMPLETO): ${p.name.uppercase()}
SUPERVISOR: ${p.supervisor.uppercase()}

CAT: ${o.cat}
        """.trimIndent()
    }

    fun generateTicketBackground(ctx: Context) {
        val o = _uiState.value.selectedOfferForLayout ?: return
        val c = _uiState.value.clientData
        val u = _uiState.value.userProfile
        
        _uiState.update { it.copy(isGeneratingTicket = true) }
        
        viewModelScope.launch {
            try {
                TicketGenerator.generateAndShare(ctx, o, c, u.name)
            } finally {
                _uiState.update { it.copy(isGeneratingTicket = false) }
            }
        }
    }
    
    // Insurance Module Functions
    fun updateInsPensionType(type: String) {
        _uiState.update { it.copy(insPensionType = type) }
    }
    
    fun updateInsAge(age: String) {
        _uiState.update { it.copy(insAge = age) }
    }
    
    fun updateInsAmount(amount: String) {
        _uiState.update { it.copy(insAmount = amount) }
    }
    
    fun updateInsTerm(term: Int) {
        _uiState.update { it.copy(insTerm = term) }
    }
    
    fun updateInsProvider(provider: String) {
        _uiState.update { it.copy(insProvider = provider) }
    }
    
    fun toggleInsNewClient(isNew: Boolean) {
        _uiState.update { it.copy(insIsNewClient = isNew) }
    }
    
    fun updateInsAntiquity(v: String) {
        _uiState.update { it.copy(insAntiquity = v) }
    }
    
    fun calculateInsurance(ctx: Context, soundManager: SoundManager) {
        viewModelScope.launch {
            val current = _uiState.value
            val age = current.insAge.toIntOrNull()
            val amount = current.insAmount.toBD()
            
            // Validación de inputs
            if (age == null || age <= 0 || amount <= BD_ZERO) {
                _uiState.update { 
                    it.copy(
                        inputError = true,
                        insResultStatus = "Error: Ingrese edad y monto válidos",
                        insResultColor = ErrorRed
                    ) 
                }
                soundManager.play("wrong")
                triggerHapticTexture(ctx, HapticPatterns.ERROR)
                delay(500)
                _uiState.update { it.copy(inputError = false) }
                return@launch
            }
            
            // --- VALIDACIÓN DE MONTO MÍNIMO ---
            val configProducto = FinancialConfig.PRODUCTS[current.insProvider]
            // Obtenemos el mínimo específico para ese plazo, o el global si no hay específico
            val minRequerido = configProducto?.minTermMap?.get(current.insTerm) 
                               ?: configProducto?.minGlobal 
                               ?: BD_ZERO

            if (amount < minRequerido) {
                _uiState.update { 
                    it.copy(
                        inputError = true,
                        insResultStatus = "RECHAZADO: Monto menor al mínimo.\nRequiere $${minRequerido.toCurrency()} para ${current.insTerm} meses.",
                        insResultColor = ErrorRed,
                        insCost1Year = BD_ZERO, // Limpiamos costos anteriores
                        insCostTotal = BD_ZERO
                    ) 
                }
                soundManager.play("wrong") // Sonido de error/alerta
                triggerHapticTexture(ctx, HapticPatterns.ERROR)
                delay(500)
                _uiState.update { it.copy(inputError = false) }
                return@launch
            }
            // -----------------------------------------------
            
            _uiState.update { it.copy(isLoading = true) }
            delay(800)
            
            val result = withContext(Dispatchers.Default) {
                val state = _uiState.value
                val pensionTypeStr = state.insPensionType
                val term = state.insTerm
                val provider = state.insProvider
                val isNewClient = state.insIsNewClient
                val antiquity = state.insAntiquity.toIntOrNull() ?: 0
                
                // Mapear String a Enum TipoPension
                val tipoPension = when(pensionTypeStr) {
                    "Invalidez" -> TipoPension.INVALIDEZ
                    "Vejez" -> TipoPension.VEJEZ
                    else -> TipoPension.OTRO
                }
                
                // === EJECUTAR MOTOR DE REGLAS ===
                val resultadoRegla = NormativaIMSS.calcularReglaSeguro(
                    edad = age,
                    plazoMeses = term,
                    tipoPension = tipoPension,
                    esNuevo = isNewClient,
                    antiguedadMeses = antiquity
                )
                
                var status = "NO APLICA SEGURO" // Default
                var color = SuccessGreen
                var applyOneYear = false
                var applyTotal = false
                
                when(resultadoRegla) {
                    ResultadoSeguro.RECHAZO -> {
                        status = "RECHAZADO: Edad Total Excede Límite (80 años)"
                        color = ErrorRed
                    }
                    ResultadoSeguro.UN_ANIO -> {
                        status = "APLICA 1 AÑO DE SEGURO"
                        color = WarningOrange
                        applyOneYear = true
                    }
                    ResultadoSeguro.PLAZO_COMPLETO -> {
                        status = "APLICA SEGURO PLAZO COMPLETO"
                        color = JuicyOrange
                        applyTotal = true
                    }
                    ResultadoSeguro.NINGUNO -> {
                        status = "NO APLICA SEGURO (Aprobado)"
                        color = SuccessGreen
                    }
                }
                
                // === CÁLCULO DE COSTOS ===
                var cost1Year = BD_ZERO
                var costTotal = BD_ZERO
                var monthlyPayment = BD_ZERO
                
                // Solo calculamos si aplica algún cobro
                if (applyOneYear || applyTotal) {
                    val factors = FinancialConfig.INSURANCE_FACTORS[provider]
                    
                    // Caso especial: OPCIPRES Retención 60m usa factor especial (key 999)
                    val termKey = if (provider == "OPCIPRES" && term == 60 && state.useRetention) {
                        999
                    } else {
                        term
                    }
                    
                    val factorPair = factors?.get(termKey)
                    
                    if (factorPair != null) {
                        val (factor1Year, factorTotal) = factorPair
                        
                        // Calculamos ambos valores para referencia, pero mostramos el que aplica
                        val calculated1Year = amount.multiply(factor1Year, MathContext(20, RoundingMode.HALF_UP))
                            .setScale(2, RoundingMode.HALF_UP)
                        val calculatedTotal = amount.multiply(factorTotal, MathContext(20, RoundingMode.HALF_UP))
                            .setScale(2, RoundingMode.HALF_UP)
                        
                        if (applyOneYear) {
                            // Solo cobramos 1 año
                            cost1Year = calculated1Year
                            status += "\nCosto: $${cost1Year.toCurrency()}"
                        } else if (applyTotal) {
                            // Cobramos todo el plazo
                            costTotal = calculatedTotal
                            status += "\nCosto Total: $${costTotal.toCurrency()}"
                        }
                        
                        // CÁLCULO DE MENSUALIDAD (PMT)
                        // Sumar costo al monto principal
                        val totalAmountWithIns = amount.add(if(applyOneYear) cost1Year else costTotal)
                        
                        // Obtener factor de pago tradicional
                        val paymentFactor = FinancialConfig.PRODUCTS[provider]?.factorsTrad?.get(term)
                        if (paymentFactor != null) {
                            // PMT = (TotalAmount * Factor) / 1000
                            monthlyPayment = totalAmountWithIns.multiply(paymentFactor)
                                .divide(BigDecimal(1000), 2, RoundingMode.HALF_UP)
                        }
                    } else {
                        status = "ERROR: Sin factor para $provider ($term m)"
                        color = ErrorRed
                    }
                }
                
                Triple(status, color, Triple(cost1Year, costTotal, monthlyPayment))
            }
            
            val status = result.first
            val color = result.second
            val costResult = result.third
            val cost1Year = costResult.first
            val costTotal = costResult.second
            val monthlyPayment = costResult.third
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    insResultStatus = status,
                    insResultColor = color,
                    insCost1Year = cost1Year,
                    insCostTotal = costTotal,
                    insMonthlyPayment = monthlyPayment
                ) 
            }
            
            soundManager.play("cash")
            triggerHapticTexture(ctx, HapticPatterns.SUCCESS)
        }
    }
    
    fun clearInsuranceData() {
        _uiState.update { 
            it.copy(
                insAge = "",
                insAmount = "",
                insResultStatus = "",
                insResultColor = Color.Transparent,
                insCost1Year = BD_ZERO,
                insCostTotal = BD_ZERO
            ) 
        }
    }
    
    // =================================================================================================
    // NEW FEATURES FUNCTIONS
    // =================================================================================================
    
    /**
     * Muestra/oculta el comparador de ofertas
     */
    fun toggleOffersComparator(show: Boolean) {
        _uiState.update { it.copy(showOffersComparator = show) }
    }
    
    /**
     * Abre el simulador de amortización para una oferta específica
     * Calcula automáticamente la tabla usando una tasa estimada del 24% anual
     */
    fun showAmortizationFor(offer: LoanOffer) {
        // Estimamos una tasa anual del 24% (ajustar según producto si es necesario)
        val estimatedAnnualRate = BigDecimal("0.24")
        val table = calculateAmortizationTable(
            amount = offer.amount,
            annualRate = estimatedAnnualRate,
            term = offer.term
        )
        
        _uiState.update {
            it.copy(
                showAmortizationSimulator = true,
                selectedOfferForAmortization = offer,
                amortizationTable = table
            )
        }
    }
    
    /**
     * Cierra el simulador de amortización
     */
    fun dismissAmortizationSimulator() {
        _uiState.update {
            it.copy(
                showAmortizationSimulator = false,
                selectedOfferForAmortization = null,
                amortizationTable = emptyList()
            )
        }
    }
    

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable

// =================================================================================================
// 12. MAIN UI ENTRY POINT
// =================================================================================================
fun ConsuCalcuApp(
    viewModel: CalculatorViewModel = viewModel(),
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val rewardsState by viewModel.rewardsState.collectAsState() // NUEVO: Para sistema de recompensas
    val ctx = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager(ctx) }

    var showMenu by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { 4 })
    LaunchedEffect(state.selectedTab) { pagerState.animateScrollToPage(state.selectedTab) }
    // Sincronización Pager -> ViewModel (Solo cuando termina el scroll)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
            .collect { (isScrollInProgress, currentPage) ->
                if (!isScrollInProgress && state.selectedTab != currentPage) {
                    viewModel.setTab(currentPage)
                }
            }
    }

    DisposableEffect(Unit) { onDispose { soundManager.release() } }
    LaunchedEffect(Unit) { viewModel.loadData(ctx); delay(50); viewModel.checkTutorialStatus(ctx) }
    LaunchedEffect(state) { viewModel.saveData(ctx) }

    if (state.showOnboarding) {
        JuicyOnboarding { soundManager.play("magic"); viewModel.completeOnboarding(ctx) }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                val firstName = state.userProfile.name.split(" ").firstOrNull() ?: "Usuario"
                                Text(
                                    "${viewModel.getGreeting()}, $firstName! 🚀",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                                Text(
                                    "ConsuCalcu",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.offset(y = (-2).dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        actions = {
                            // ========== AVATAR ACTIVO ==========
                            val activeAvatar = remember(rewardsState.active.currentAvatar) {
                                AvatarCatalog.getAvatarById(rewardsState.active.currentAvatar)
                                    ?: AvatarCatalog.ALL_AVATARS.first()
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(activeAvatar.gradient)
                                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeAvatar.emoji,
                                    fontSize = 20.sp
                                )
                            }
                            
                            Spacer(Modifier.width(8.dp))
                            // ===================================
                            
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .height(32.dp)
                                    .clickable {
                                        soundManager.play("pop")
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.LocalFireDepartment,
                                        null,
                                        tint = JuicyOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "${state.streak}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Box(
                                        Modifier
                                            .padding(horizontal = 8.dp)
                                            .width(1.dp)
                                            .height(12.dp)
                                            .background(
                                                MaterialTheme.colorScheme.outline.copy(
                                                    alpha = 0.5f
                                                )
                                            )
                                    )

                                    Text(
                                        "LVL ${state.userProfile.level}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = JuicyBlue
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        Icons.Rounded.MoreVert,
                                        contentDescription = "Opciones",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Configuración",
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Settings, null) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.toggleConfigDialog(true)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Ver Tutorial",
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.HelpOutline, null) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.resetTutorial()
                                        }
                                    )
                                    
                                    // ========== REWARDS MENU ITEM ==========
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Mis Recompensas",
                                                    fontWeight = FontWeight.Bold
                                                )
                                                val rewardsState by viewModel.rewardsState.collectAsState()
                                                val totalUnlocked = rewardsState.unlocked.themes.size +
                                                        rewardsState.unlocked.particles.size +
                                                        rewardsState.unlocked.avatars.size +
                                                        rewardsState.unlocked.badges.size +
                                                        rewardsState.unlocked.cardStyles.size
                                                Text(
                                                    "$totalUnlocked/33",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.EmojiEvents, null, tint = Color(0xFFFFD700)) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.openRewardsGallery()
                                        }
                                    )
                                    // ========================================
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (isDark) "Modo Claro" else "Modo Oscuro",
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                                null
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            onToggleTheme()
                                        }
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                    )
                },
                bottomBar = {
                    // ========== CUSTOM NAVIGATION BAR CON INDICADOR DESLIZANTE ==========
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            )
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Indicador deslizante animado (FLUIDO como líquido)
                            val indicatorOffset by animateFloatAsState(
                                targetValue = state.selectedTab.toFloat(),
                                animationSpec = tween(
                                    durationMillis = 600, // Más lento para efecto fluido
                                    easing = FastOutSlowInEasing // Curva suave como líquido
                                ),
                                label = "indicatorOffset"
                            )
                            
                            // Colores por pestaña
                            val tabColors = listOf(JuicyBlue, JuicyGreen, JuicyPurple, JuicyOrange)
                            val currentColor = tabColors[state.selectedTab]
                            
                            val animatedIndicatorColor by animateColorAsState(
                                targetValue = currentColor,
                                animationSpec = tween(
                                    durationMillis = 500, // Transición de color más suave
                                    easing = FastOutSlowInEasing
                                ),
                                label = "indicatorColor"
                            )
                            
                            // Barra indicadora deslizante
                            Box(
                                    modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = (indicatorOffset * (LocalContext.current.resources.displayMetrics.widthPixels / 4f / LocalContext.current.resources.displayMetrics.density)).dp)
                                    .width((LocalContext.current.resources.displayMetrics.widthPixels / 4f / LocalContext.current.resources.displayMetrics.density).dp)
                                    .height(3.dp)
                                    .padding(horizontal = 24.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                animatedIndicatorColor,
                                                animatedIndicatorColor,
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                    )
                            )
                            
                            // Items de navegación
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Tab 0: Refinanciar
                                NavBarItem(
                                    icon = if (state.selectedTab == 0) Icons.Rounded.Loop else Icons.Outlined.Loop,
                                    label = "Refinanciar",
                                    selected = state.selectedTab == 0,
                                    color = JuicyBlue,
                                    onClick = {
                                        soundManager.play("pop")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setTab(0)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Tab 1: Nuevo
                                NavBarItem(
                                    icon = if (state.selectedTab == 1) Icons.Rounded.AttachMoney else Icons.Outlined.AttachMoney,
                                    label = "Nuevo",
                                    selected = state.selectedTab == 1,
                                    color = JuicyGreen,
                                    onClick = {
                                        soundManager.play("pop")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setTab(1)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Tab 2: Seguro
                                NavBarItem(
                                    icon = Icons.Outlined.Savings,
                                    label = "Seguro",
                                    selected = state.selectedTab == 2,
                                    color = JuicyPurple,
                                    onClick = {
                                        soundManager.play("pop")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setTab(2)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Tab 3: Academia
                                NavBarItem(
                                    icon = if (state.selectedTab == 3) Icons.Rounded.School else Icons.Outlined.School,
                                    label = "Academia",
                                    selected = state.selectedTab == 3,
                                    color = JuicyOrange,
                                    onClick = {
                                        soundManager.play("pop")
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setTab(3)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                
                            }
                        }
                    }
                }
            ) { padding ->
                // ========== HORIZONTAL PAGER CON EFECTO LIQUID GLASS ==========
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    pageSpacing = 0.dp, // Sin espacio entre páginas para transición suave
                    userScrollEnabled = true
                ) { page ->
                    // Efecto de transformación de página (Liquid Glass Enhanced)
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val absoluteOffset = kotlin.math.abs(pageOffset)
                    
                    // 🎨 NUEVO: Gradiente dinámico basado en la tab activa
                    val tabColor = when (page) {
                        0 -> JuicyBlue
                        1 -> JuicyGreen
                        2 -> JuicyPurple
                        3 -> JuicyOrange
                        else -> JuicyBlue
                    }
                    
                    val gradientAlpha by animateFloatAsState(
                        targetValue = if (absoluteOffset < 0.5f) 0.05f else 0f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        label = "gradientAlpha"
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            // 🎨 Gradiente de fondo dinámico por tab
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        tabColor.copy(alpha = gradientAlpha),
                                        Color.Transparent,
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = 400f
                                )
                            )
                            .graphicsLayer {
                                // Escala suave: la página actual es 1.0, las demás se reducen ligeramente
                                val scale = 1f - (absoluteOffset * 0.05f).coerceIn(0f, 0.05f)
                                scaleX = scale
                                scaleY = scale
                                
                                // Alpha suave: fade in/out durante la transición
                                alpha = 1f - (absoluteOffset * 0.3f).coerceIn(0f, 0.3f)
                                
                                // Translación suave en Y para efecto de profundidad
                                translationY = absoluteOffset * 20f
                            }
                            // ✨ NUEVO: Blur sutil en páginas no activas
                            .then(
                                if (absoluteOffset > 0.1f) {
                                    Modifier.blur(
                                        radius = (absoluteOffset * 8f).coerceIn(0f, 8f).dp
                                    )
                                } else Modifier
                            )
                    ) {
                        AnimatedTabContent(tabKey = page) {
                            when (page) {
                                0 -> RefinanceScreen(viewModel, state, soundManager, ctx, rewardsState)
                                1 -> NewCreditScreen(viewModel, state, soundManager, ctx, rewardsState)
                                2 -> InsuranceScreen(viewModel, state, soundManager, ctx)
                                3 -> AcademyScreen(soundManager = soundManager)
                            }
                            }
                        }
                    }
                }
                // ================================================================
            }

            state.selectedOfferForLayout?.let { selectedOffer ->
                if (state.showOfferPreview) {
                    OfferPreviewModal(
                        offer = selectedOffer,
                        viewModel = viewModel,
                        soundManager = soundManager,
                        context = ctx,
                        onDismiss = { viewModel.dismissPreview() },
                        onContinue = { viewModel.continueToClientForm() }
                    )
                }
            }

            if (state.showClientForm) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.dismissForm() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentWindowInsets = { androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0) }
                ) {
                    ClientFormContent(
                        viewModel,
                        onGenerate = { layout ->
                            soundManager.play("magic"); haptic.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        ); clipboard.setText(AnnotatedString(layout)); viewModel.dismissForm(); viewModel.triggerSuccess()
                        },
                        onGenerateTicket = { viewModel.generateTicketBackground(ctx) })
                }
            }

            if (state.mandatoryConfigNeeded) {
                MandatoryConfigScreen(viewModel, ctx, soundManager)
            } else {
                if (state.showConfigDialog) {
                    ConfigDialog(viewModel, ctx, soundManager)
                }

                if (state.showOnboarding) {
                    JuicyOnboarding(onFinished = { viewModel.completeOnboarding(ctx) })
                }
                
                if (state.showTutorial) {
                    RPGDialogOverlay(
                        state.tutorialStep, 
                        soundManager, 
                        onNext = { soundManager.play("pop"); viewModel.advanceTutorial() },
                        onFinish = { viewModel.closeTutorial(ctx) }
                    )
                }

                if (state.showStreakOverlay) {
                    StreakOverlay(state.streak) {
                        soundManager.play("magic")
                        viewModel.dismissStreak()
                    }
                }

                if (state.showLevelUpOverlay) {
                    LevelUpOverlay(state.userProfile.level, state.unlockedTitle) {
                        soundManager.play("cash")
                        viewModel.dismissLevelUp()
                    }
                }
                
                // ========== REWARDS UNLOCK OVERLAY ==========
                if (state.showRewardUnlockOverlay && rewardsState.newUnlocks.isNotEmpty()) {
                    RewardUnlockOverlay(
                        newUnlocks = rewardsState.newUnlocks,
                        onDismiss = {
                            soundManager.play("magic")
                            viewModel.dismissRewardUnlock()
                        }
                    )
                }
                // ============================================
                
                // ========== REWARDS GALLERY MODAL ==========
                if (state.showRewardsGallery) {
                    var showThemeSelector by remember { mutableStateOf(false) }
                    var showParticleSelector by remember { mutableStateOf(false) }
                    var showAvatarSelector by remember { mutableStateOf(false) }
                    var showCardStyleSelector by remember { mutableStateOf(false) }
                    
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeRewardsGallery() },
                        containerColor = MaterialTheme.colorScheme.background
                    ) {
                        when {
                            showThemeSelector -> {
                                ThemeSelectorScreen(
                                    currentLevel = state.userProfile.level,
                                    rewardsState = rewardsState,
                                    onThemeSelected = { themeId ->
                                        viewModel.activateTheme(themeId)
                                        soundManager.play("pop")
                                    },
                                    onClose = { showThemeSelector = false }
                                )
                            }
                            showParticleSelector -> {
                                ParticleSelectorScreen(
                                    currentLevel = state.userProfile.level,
                                    rewardsState = rewardsState,
                                    onParticleSelected = { particleId ->
                                        viewModel.activateParticle(particleId)
                                        soundManager.play("pop")
                                    },
                                    onClose = { showParticleSelector = false }
                                )
                            }
                            showAvatarSelector -> {
                                AvatarSelectorScreen(
                                    currentLevel = state.userProfile.level,
                                    rewardsState = rewardsState,
                                    onAvatarSelected = { avatarId ->
                                        viewModel.activateAvatar(avatarId)
                                        soundManager.play("pop")
                                    },
                                    onClose = { showAvatarSelector = false }
                                )
                            }
                            showCardStyleSelector -> {
                                CardStyleSelectorScreen(
                                    currentLevel = state.userProfile.level,
                                    rewardsState = rewardsState,
                                    onCardStyleSelected = { styleId ->
                                        viewModel.activateCardStyle(styleId)
                                        soundManager.play("pop")
                                    },
                                    onClose = { showCardStyleSelector = false }
                                )
                            }
                            else -> {
                                RewardsGalleryScreen(
                                    currentLevel = state.userProfile.level,
                                    rewardsState = rewardsState,
                                    onThemeClick = { showThemeSelector = true },
                                    onParticleClick = { showParticleSelector = true },
                                    onAvatarClick = { showAvatarSelector = true },
                                    onCardStyleClick = { showCardStyleSelector = true },
                                    onClose = { viewModel.closeRewardsGallery() }
                                )
                            }
                        }
                    }
                }
                // ===========================================
                
                // ========== NEW FEATURES MODALS ==========
                // Comparador de Ofertas
                if (state.showOffersComparator) {
                    val offers = if (state.selectedTab == 0) state.offersCNCA else state.offersNew
                    if (offers.isNotEmpty()) {
                        OffersComparator(
                            offers = offers,
                            onDismiss = {
                                soundManager.play("pop")
                                viewModel.toggleOffersComparator(false)
                            },
                            onSelectOffer = { offer ->
                                soundManager.play("magic")
                                viewModel.onSelectOffer(offer)
                                viewModel.toggleOffersComparator(false)
                            }
                        )
                    }
                }
                
                // Simulador de Amortización
                if (state.showAmortizationSimulator && state.selectedOfferForAmortization != null) {
                    AmortizationSimulator(
                        offer = state.selectedOfferForAmortization!!,
                        amortizationTable = state.amortizationTable,
                        onDismiss = {
                            soundManager.play("pop")
                            viewModel.dismissAmortizationSimulator()
                        }
                    )
                }
                // =========================================
            }

            if (state.showSuccessAnim) SuccessOverlay { viewModel.hideSuccess() }
            if (state.showConfetti) {
                // ========== APLICAR EFECTO DE PARTÍCULAS ACTIVO ==========
                val activeParticle = remember(rewardsState.active.currentParticle) {
                    ParticleCatalog.getParticleById(rewardsState.active.currentParticle)
                        ?: ParticleCatalog.ALL_PARTICLES.first()
                }
                
                val konfettiColors = if (activeParticle.id != "none" && activeParticle.colors.isNotEmpty()) {
                    activeParticle.colors.map { color ->
                        android.graphics.Color.rgb(
                            (color.red * 255).toInt(),
                            (color.green * 255).toInt(),
                            (color.blue * 255).toInt()
                        )
                    }
                } else {
                    listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def)
                }
                
                val particleCount = if (activeParticle.id != "none") {
                    activeParticle.particleCount
                } else {
                    100
                }
                // =========================================================
                
                KonfettiView(
                    parties = listOf(
                        Party(
                            speed = 0f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            spread = 360,
                            colors = konfettiColors,
                            position = Position.Relative(0.5, 0.3),
                            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(particleCount)
                        )
                    ), modifier = Modifier.fillMaxSize()
                )
            }

            if (state.showStreakOverlay) {
                StreakOverlay(state.streak) {
                    soundManager.play("magic")
                    viewModel.dismissStreak()
                }
            }
            
            if (state.showLevelUpOverlay) {
                LevelUpOverlay(state.userProfile.level, state.unlockedTitle) {
                    soundManager.play("cash")
                    viewModel.dismissLevelUp()
                }
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable

// =================================================================================================
// 13. SCREENS
// =================================================================================================
fun RefinanceScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    soundManager: SoundManager,
    ctx: Context,
    rewardsState: RewardsState = RewardsState() // NUEVO: Para estilos de tarjetas
) {
    val haptic = LocalHapticFeedback.current
    val totalDebt = state.debts.sumOf { it.balance.toBD() };
    val totalPayment = state.debts.sumOf { it.payment.toBD() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🔽 REDUNDANCY FIX: Ocultar tarjeta de resumen si solo hay 1 deuda
        item {
            AnimatedVisibility(
                visible = state.debts.size > 1,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                JuicyGradientCard(
                    brush = Brush.horizontalGradient(
                        listOf(JuicyPurple, Color(0xFF9C27B0))
                    ), shadowColor = JuicyPurpleShadow
                ) {
                    Row(
                        Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "SUMA DEUDAS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            ); Text(
                            "$${totalDebt.toCurrency()}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        }; Box(
                        Modifier.height(40.dp).width(1.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    ); Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "MENSUALIDAD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        ); Text(
                        "$${totalPayment.toCurrency()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        items(state.debts, key = { it.id }) { debt ->
            val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
                if (it == SwipeToDismissBoxValue.EndToStart) {
                    soundManager.play("pop"); viewModel.removeDebtRow(debt); true
                } else false
            })
            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    Box(
                        Modifier.fillMaxSize().padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(20.dp)).background(JuicyRed),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 24.dp)
                        )
                    }
                }) {
                DebtRowItem(
                    debt,
                    { viewModel.updateDebt(debt.id, it) },
                    { viewModel.removeDebtRow(debt) },
                    soundManager
                )
            }
        }
        item {
            JuicyButton(
                text = "AGREGAR DEUDA",
                color = MaterialTheme.colorScheme.surface,
                shadowColor = MaterialTheme.colorScheme.outline,
                textColor = MaterialTheme.colorScheme.onSurface,
                icon = Icons.Rounded.Add,
                soundManager = soundManager
            ) { viewModel.addDebtRow() }
        }
        item {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 2.dp
            )
        }
        item {
            val totalPagos = state.debts.sumOf { it.payment.toBD() }
            val ajusteInput = state.capacityCNCA.toBD()
            val capacidadFinal = totalPagos.add(ajusteInput)
            val isNegative = ajusteInput < BigDecimal.ZERO

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                JuicyInput(
                    label = "AJUSTE DE LIQUIDEZ (+/-)",
                    value = state.capacityCNCA,
                    onValChange = { viewModel.updateCapacityCNCA(it) },
                    type = KeyboardType.Number,
                    leadingIcon = if (isNegative) Icons.AutoMirrored.Rounded.TrendingDown else Icons.Rounded.AddCircleOutline,
                    isCurrency = true,
                    isError = state.inputError,
                    imeAction = ImeAction.Done
                )
                
                if (totalPagos > BigDecimal.ZERO) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pagos: $${totalPagos.toCurrency()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isNegative) "- $${ajusteInput.abs().toCurrency()}" else "+ $${ajusteInput.toCurrency()}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isNegative) JuicyRed else JuicyGreen
                        )
                        Text(
                            text = "= Capacidad: $${capacidadFinal.toCurrency()}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    JuicyButton(
                        text = "CALCULAR",
                        color = JuicyGreen,
                        shadowColor = JuicyGreenShadow,
                        textColor = Color.White,
                        height = 56.dp,
                        soundManager = soundManager
                    ) { viewModel.calculateRefinance(ctx, soundManager) }
                }; JuicyIconButton(
                icon = Icons.Rounded.DeleteSweep,
                color = JuicyRed,
                shadowColor = JuicyRedShadow,
                iconColor = Color.White,
                size = 56.dp,
                soundManager = soundManager
            ) { viewModel.clearRefinanceData() }
            }
        }

        // Botones de acción para resultados
        if (state.offersCNCA.isNotEmpty() && !state.isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    JuicyButton(
                        text = "COMPARAR OFERTAS",
                        color = JuicyBlue,
                        shadowColor = JuicyBlueShadow,
                        textColor = Color.White,
                        icon = Icons.AutoMirrored.Rounded.CompareArrows,
                        soundManager = soundManager
                    ) {
                        viewModel.toggleOffersComparator(true)
                    }
                }
            }
        }

        if (state.isLoading) {
            item {
                ResultCardSkeletonList(count = 3)
            }
        } else {
            itemsIndexed(state.offersCNCA) { index, offer ->
                val haptics = LocalHapticManager.current
                AnimatedResultItem(index = index) {
                    ResultCard(
                        offer = offer,
                        onClick = {
                            soundManager.play("pop")
                            haptics?.medium()
                            viewModel.onSelectOffer(offer)
                        },
                        onShowAmortization = {
                            soundManager.play("magic")
                            haptics?.success()
                            viewModel.showAmortizationFor(offer)
                        },
                        rewardsState = rewardsState // NUEVO: Aplicar estilos
                    )
                }
            }
            if (state.offersCNCA.isEmpty()) item { 
                // Usar nuevo Empty State Ilustrado
                EmptyStateIllustration(
                    title = "Sin ofertas de refinanciamiento",
                    message = "Ajusta tu capacidad o deudas seleccionadas con 'Detalle'",
                    emoji = "📊"
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun NewCreditScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    soundManager: SoundManager,
    ctx: Context,
    rewardsState: RewardsState = RewardsState() // NUEVO: Para estilos de tarjetas
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            JuicyCard(
                color = MaterialTheme.colorScheme.surface,
                shadowColor = MaterialTheme.colorScheme.outline
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "COTIZADOR NUEVOS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(15.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            JuicyChip(
                                selected = !state.isByAmount,
                                text = "Por capacidad",
                                onClick = {
                                    soundManager.play("pop"); viewModel.toggleCalculationMode(
                                    false
                                )
                                })
                        }
                        Box(Modifier.weight(1f)) {
                            JuicyChip(
                                selected = state.isByAmount,
                                text = "Por monto",
                                onClick = {
                                    soundManager.play("pop"); viewModel.toggleCalculationMode(
                                    true
                                )
                                })
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (state.isByAmount) {
                        JuicySliderInput(
                            label = "¿CUÁNTO NECESITAS?",
                            value = state.newCreditInput,
                            range = 5000f..200000f,
                            steps = 38,
                            onValueChange = { viewModel.updateNewCreditInput(it) },
                            isError = state.inputError
                        )
                    } else {
                        JuicyInput(
                            label = "¿CUÁNTO PUEDES PAGAR?",
                            value = state.newCreditInput,
                            onValChange = { viewModel.updateNewCreditInput(it) },
                            type = KeyboardType.Number,
                            leadingIcon = Icons.Outlined.Savings,
                            isCurrency = true,
                            isError = state.inputError,
                            imeAction = ImeAction.Done
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.toggleRetention(!state.useRetention) }
                            .padding(12.dp)
                    ) {
                        Checkbox(
                            checked = state.useRetention,
                            onCheckedChange = { viewModel.toggleRetention(it) },
                            colors = CheckboxDefaults.colors(checkedColor = JuicyGreen)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Tasa Preferencial",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (state.useRetention) "Solo se mostrarán ofertas con tasa preferencial (60 meses)" else "Habilita para filtrar solo ofertas con tasa preferencial",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            JuicyButton(
                                text = "COTIZAR",
                                color = JuicyGreen,
                                shadowColor = JuicyGreenShadow,
                                textColor = Color.White,
                                height = 56.dp,
                                soundManager = soundManager
                            ) { viewModel.calculateNewCredit(ctx, soundManager) }
                        }
                        JuicyIconButton(
                            icon = Icons.Rounded.DeleteSweep,
                            color = JuicyRed,
                            shadowColor = JuicyRedShadow,
                            iconColor = Color.White,
                            size = 56.dp,
                            soundManager = soundManager
                        ) { viewModel.clearNewCreditData() }
                    }
                }
            }
        }

        // Botones de acción para resultados
        if (state.offersNew.isNotEmpty() && !state.isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    JuicyButton(
                        text = "COMPARAR OFERTAS",
                        color = JuicyBlue,
                        shadowColor = JuicyBlueShadow,
                        textColor = Color.White,
                        icon = Icons.AutoMirrored.Rounded.CompareArrows,
                        soundManager = soundManager
                    ) {
                        viewModel.toggleOffersComparator(true)
                    }
                }
            }
        }

        if (state.isLoading) {
            item {
                ResultCardSkeletonList(count = 3)
            }
        } else {
            itemsIndexed(state.offersNew) { index, offer ->
                AnimatedResultItem(index = index) {
                    ResultCard(
                        offer = offer,
                        onClick = {
                            soundManager.play("pop")
                            viewModel.onSelectOffer(offer)
                        },
                        onShowAmortization = {
                            soundManager.play("magic")
                            viewModel.showAmortizationFor(offer)
                        },
                        rewardsState = rewardsState
                    )
                }
            }
            if (state.offersNew.isEmpty()) item { 
                // Usar nuevo Empty State Ilustrado
                EmptyStateIllustration(
                    title = "Sin ofertas para ti",
                    message = "Ajusta la capacidad de pago o plazo",
                    emoji = "🔍"
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    soundManager: SoundManager,
    ctx: Context
) {
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState() // Estado del scroll
    
    // Auto-scroll cuando hay resultado
    LaunchedEffect(state.insResultStatus) {
        if (state.insResultStatus.isNotEmpty()) {
            delay(500) // Esperar a que la UI se dibuje
            listState.animateScrollToItem(index = 5) // Índice aproximado de la tarjeta de resultado
        }
    }
    
    LazyColumn(
        state = listState, // Asignar el estado aquí
        modifier = Modifier.fillMaxSize().padding(16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            JuicyGradientCard(
                brush = Brush.horizontalGradient(
                    listOf(JuicyPurple, Color(0xFF9C27B0))
                ),
                shadowColor = JuicyPurpleShadow
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Savings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "CALCULADORA DE SEGURO",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "IMSS Pensionados",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        
        item {
            JuicyCard(
                color = MaterialTheme.colorScheme.surface,
                shadowColor = MaterialTheme.colorScheme.outline
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "DATOS DEL CRÉDITO",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(20.dp))
                    
                    
                    // 1. Selector de Financiera con Colores
                    JuicyProviderDropdown(
                        selectedValue = state.insProvider,
                        onSelectionChange = {
                            viewModel.updateInsProvider(it)
                            soundManager.play("pop")
                        }
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // 2. Selector de Plazo con Icono y Estilo
                    JuicyTermDropdown(
                        selectedValue = state.insTerm,
                        onSelectionChange = {
                            viewModel.updateInsTerm(it)
                            soundManager.play("pop")
                        }
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    JuicyInput(
                        label = "MONTO DEL CRÉDITO",
                        value = state.insAmount,
                        onValChange = { viewModel.updateInsAmount(it) },
                        type = KeyboardType.Number,
                        leadingIcon = Icons.Rounded.AttachMoney,
                        isCurrency = true,
                        isError = state.inputError,
                        imeAction = ImeAction.Next
                    )
                }
            }
        }
        
        item {
            JuicyCard(
                color = MaterialTheme.colorScheme.surface,
                shadowColor = MaterialTheme.colorScheme.outline
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "DATOS DEL CLIENTE",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(20.dp))
                    
                    // Selector Tipo de Pensión (FilterChips)
                    Text(
                        "TIPO DE PENSIÓN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Invalidez", "Vejez", "Otro").forEach { type ->
                            FilterChip(
                                selected = state.insPensionType == type,
                                onClick = {
                                    viewModel.updateInsPensionType(type)
                                    soundManager.play("pop")
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                label = { Text(type) },
                                leadingIcon = if (state.insPensionType == type) {
                                    { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JuicyPurple,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Input Edad con Validación Visual
                    val ageInt = state.insAge.toIntOrNull() ?: 0
                    val isAgeWarning = ageInt > 75
                    val isAgeError = ageInt > 85
                    
                    JuicyInput(
                        label = if (isAgeError) "EDAD (EXCEDE LÍMITE)" else "EDAD DEL CLIENTE",
                        value = state.insAge,
                        onValChange = { viewModel.updateInsAge(it) },
                        type = KeyboardType.Number,
                        leadingIcon = if (isAgeError) Icons.Rounded.Close else Icons.Rounded.Person,
                        isCurrency = false,
                        isError = isAgeError,
                        imeAction = if (!state.insIsNewClient) ImeAction.Next else ImeAction.Done
                    )
                    
                    Spacer(Modifier.height(16.dp))

                    // Checkbox Es Cliente Nuevo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = state.insIsNewClient,
                                onValueChange = { 
                                    viewModel.toggleInsNewClient(it)
                                    soundManager.play("pop")
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.insIsNewClient,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = JuicyPurple)
                        )
                        Text(
                            "Es Cliente Nuevo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Input Antigüedad (Condicional)
                    AnimatedVisibility(visible = !state.insIsNewClient) {
                        Column {
                            Spacer(Modifier.height(16.dp))
                            JuicyInput(
                                label = "ANTIGÜEDAD (MESES)",
                                value = state.insAntiquity,
                                onValChange = { viewModel.updateInsAntiquity(it) },
                                type = KeyboardType.Number,
                                leadingIcon = Icons.Rounded.DateRange,
                                isCurrency = false,
                                imeAction = ImeAction.Done
                            )
                        }
                    }
                    
                    // Texto de ayuda para advertencia
                    if (isAgeWarning && !isAgeError) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                tint = JuicyOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "⚠️ Precaución: Edades avanzadas pueden requerir pago total o rechazo",
                                color = JuicyOrange,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    
                    // (Switch "Cliente Nuevo" Eliminado - Ya existe Checkbox arriba)
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    JuicyButton(
                        text = if (state.isLoading) "CALCULANDO..." else "CALCULAR",
                        icon = if (state.isLoading) Icons.Rounded.HourglassEmpty else Icons.Outlined.Savings,
                        color = if (state.isLoading) Color.Gray else JuicyPurple,
                        shadowColor = if (state.isLoading) Color.DarkGray else JuicyPurpleShadow,
                        textColor = Color.White,
                        soundManager = soundManager,
                        onClick = {
                            if (!state.isLoading) { // Prevenir doble click
                                soundManager.play("cash")
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.calculateInsurance(ctx, soundManager)
                            }
                        }
                    )
                }
                
                Box(Modifier.weight(0.5f)) {
                    JuicyButton(
                        text = "LIMPIAR",
                        icon = Icons.Rounded.DeleteSweep,
                        color = Color(0xFF607D8B),
                        shadowColor = Color(0xFF37474F),
                        textColor = Color.White,
                        soundManager = soundManager,
                        onClick = {
                            soundManager.play("pop")
                            viewModel.clearInsuranceData()
                        }
                    )
                }
            }
        }
        
        // Result Card - Diseño Simplificado y Claro
        if (state.insResultStatus.isNotEmpty()) {
            item {
                AnimatedResultItem(index = 0) {
                    val isRejected = state.insResultStatus.contains("RECHAZADO")
                    val isFree = state.insResultStatus.contains("NO APLICA")
                    val hasCost = state.insCost1Year > BD_ZERO || state.insCostTotal > BD_ZERO
                    
                    JuicyCard(
                        color = MaterialTheme.colorScheme.surface,
                        shadowColor = state.insResultColor
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            // ========== DICTAMEN PRINCIPAL ==========
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Icono de estado
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            state.insResultColor.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isRejected -> Icons.Rounded.Close
                                            isFree -> Icons.Rounded.CheckCircle
                                            hasCost -> Icons.Rounded.AttachMoney
                                            else -> Icons.AutoMirrored.Rounded.HelpOutline
                                        },
                                        contentDescription = null,
                                        tint = state.insResultColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                Spacer(Modifier.width(16.dp))
                                
                                // Texto del dictamen
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "RESULTADO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = when {
                                            isRejected -> "RECHAZADO"
                                            isFree -> "SIN COSTO"
                                            hasCost -> "CON COSTO"
                                            else -> "PROCESADO"
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = state.insResultColor,
                                        fontSize = 24.sp
                                    )
                                }
                            }

                            
                            // ========== COSTO (SI APLICA) ==========
                            if (hasCost) {
                                Spacer(Modifier.height(24.dp))
                                
                                // Tarjeta de costo destacada
                                Surface(
                                    color = state.insResultColor,
                                    shape = RoundedCornerShape(16.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "COSTO DEL SEGURO",
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.2.sp
                                        )
                                        
                                        Spacer(Modifier.height(8.dp))
                                        
                                        val finalCost = if (state.insCostTotal > BD_ZERO) 
                                            state.insCostTotal 
                                        else 
                                            state.insCost1Year
                                        
                                        Text(
                                            text = "$${finalCost.toCurrency()}",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            fontSize = 36.sp,
                                            letterSpacing = (-1).sp
                                        )
                                        
                                        // Indicador de tipo de pago
                                        if (state.insCostTotal > BD_ZERO) {
                                            Spacer(Modifier.height(4.dp))
                                            Surface(
                                                color = Color.White.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Text(
                                                    text = "PAGO TOTAL PLAZO",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                )
                                            }
                                        } else {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = "Costo 1er Año",
                                                color = Color.White.copy(alpha = 0.8f),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 12.sp
                                            )
                                        }
                                        
                                        // PAGO MENSUAL ESTIMADO
                                        if (state.insMonthlyPayment > BD_ZERO) {
                                            Spacer(Modifier.height(16.dp))
                                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                            Spacer(Modifier.height(12.dp))
                                            
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    "MENSUALIDAD APROXIMADA",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    "$${state.insMonthlyPayment.toCurrency()}",
                                                    color = Color.White,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // ========== DETALLES EXPANDIBLES ==========
                            var showDetails by remember { mutableStateOf(false) }
                            
                            Spacer(Modifier.height(20.dp))
                            
                            // Botón para mostrar/ocultar detalles
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        showDetails = !showDetails
                                        soundManager.play("pop")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (showDetails) "Ocultar análisis" else "Ver análisis detallado",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = if (showDetails) 
                                            Icons.Rounded.Close 
                                        else 
                                            Icons.Rounded.AddCircleOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            // Contenido expandible
                            AnimatedVisibility(
                                visible = showDetails,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Análisis del Dictamen",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                fontSize = 11.sp,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = state.insResultStatus,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        
        item { Spacer(Modifier.height(80.dp)) }
    }
}
@Composable

// =================================================================================================
// 14. REUSABLE COMPONENTS
// =================================================================================================
fun ClientFormContent(
    viewModel: CalculatorViewModel,
    onGenerate: (String) -> Unit,
    onGenerateTicket: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val d = state.clientData
    val o = state.selectedOfferForLayout

    Column(
        modifier = Modifier
            .padding(16.dp)
            .navigationBarsPadding()
            .fillMaxWidth()
    ) {
        Text(
            "Formulario de Captura",
            style = JuicyTypography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            "Completa todos los campos para generar el layout",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========== DATOS DEL CLIENTE ==========
            item {
                Text(
                    "DATOS DEL CLIENTE",
                    style = MaterialTheme.typography.labelMedium,
                    color = JuicyOrange,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                JuicyInput(
                    "Nombre Completo",
                    d.name,
                    { viewModel.updateClientData(d.copy(name = it)) },
                    KeyboardType.Text,
                    Icons.Rounded.Person
                )
            }
            
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) {
                                JuicyInput(
                                    "NSS",
                                    d.nss,
                                    { if (it.length <= 11) viewModel.updateClientData(d.copy(nss = it)) },
                                    KeyboardType.Number,
                                    Icons.Rounded.Numbers
                                )
                            }
                            if (d.nss.isNotEmpty()) {
                                Spacer(Modifier.width(8.dp))
                                NSSValidationIndicator(d.nss)
                            }
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        JuicyInput(
                            "CURP",
                            d.curp,
                            { if (it.length <= 18) viewModel.updateClientData(d.copy(curp = it)) },
                            KeyboardType.Text,
                            Icons.Rounded.Badge
                        )
                    }
                }
            }
            
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        JuicyInput(
                            "RFC (con homoclave)",
                            d.rfc,
                            { if (it.length <= 13) viewModel.updateClientData(d.copy(rfc = it)) },
                            KeyboardType.Text,
                            Icons.Rounded.Badge
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        JuicyInput(
                            "Edad",
                            d.age,
                            { if (it.length <= 3) viewModel.updateClientData(d.copy(age = it)) },
                            KeyboardType.Number,
                            Icons.Rounded.CalendarMonth
                        )
                    }
                }
            }
            
            item {
                JuicyInput(
                    "Correo Electrónico",
                    d.email,
                    { viewModel.updateClientData(d.copy(email = it)) },
                    KeyboardType.Email,
                    Icons.Rounded.Person
                )
            }
            
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        JuicyInput(
                            "Teléfono",
                            d.phone,
                            { if (it.length <= 10) viewModel.updateClientData(d.copy(phone = it)) },
                            KeyboardType.Phone,
                            Icons.Rounded.Phone
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        JuicyInput(
                            "Tipo de Pensión",
                            d.pensionType,
                            { viewModel.updateClientData(d.copy(pensionType = it)) },
                            KeyboardType.Text,
                            Icons.Rounded.Person
                        )
                    }
                }
            }
            
            item {
                JuicyInput(
                    "CLABE",
                    d.clabe,
                    { if (it.length <= 18) viewModel.updateClientData(d.copy(clabe = it)) },
                    KeyboardType.Number,
                    Icons.Rounded.CreditCard
                )
            }
            
            item {
                JuicyInput(
                    "Banco",
                    d.bank,
                    { viewModel.updateClientData(d.copy(bank = it)) },
                    KeyboardType.Text,
                    Icons.Rounded.AccountBalance
                )
            }
            
            // ========== CUESTIONARIO MÉDICO ==========
            item {
                Text(
                    "CUESTIONARIO MÉDICO",
                    style = MaterialTheme.typography.labelMedium,
                    color = JuicyBlue,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            item {
                JuicyInput(
                    "Enfermedad",
                    d.disease,
                    { viewModel.updateClientData(d.copy(disease = it)) },
                    KeyboardType.Text,
                    Icons.Rounded.LocalFireDepartment
                )
            }
            
            item {
                JuicyInput(
                    "Fecha de Inicio de Padecimiento",
                    d.diseaseDate,
                    { viewModel.updateClientData(d.copy(diseaseDate = it)) },
                    KeyboardType.Text,
                    Icons.Rounded.CalendarMonth
                )
            }
            
            // ========== TIPO DE TRÁMITE ==========
            item {
                Text(
                    "TIPO DE TRÁMITE",
                    style = MaterialTheme.typography.labelMedium,
                    color = JuicyPurple,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            item {
                TransactionTypeSelector(
                    selected = d.transactionType,
                    onSelect = { viewModel.updateClientData(d.copy(transactionType = it)) }
                )
            }
            
            // Campos condicionales para CNCA/2ª DISP/LCOM
            if (d.transactionType != "NUEVO") {
                item {
                    JuicyInput(
                        "ID (Crédito a Liquidar)",
                        d.creditId,
                        { viewModel.updateClientData(d.copy(creditId = it)) },
                        KeyboardType.Text,
                        Icons.Rounded.Numbers
                    )
                }
                
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "Descuento",
                                d.discountAmount,
                                { viewModel.updateClientData(d.copy(discountAmount = it)) },
                                KeyboardType.Text,
                                Icons.Rounded.AttachMoney
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "Núm. Descuentos",
                                d.numDiscounts,
                                { viewModel.updateClientData(d.copy(numDiscounts = it)) },
                                KeyboardType.Number,
                                Icons.Rounded.Numbers
                            )
                        }
                    }
                }
                
                item {
                    JuicyInput(
                        "Marca que Liquida",
                        d.liquidatingBrand,
                        { viewModel.updateClientData(d.copy(liquidatingBrand = it)) },
                        KeyboardType.Text,
                        Icons.Rounded.Business
                    )
                }
            }
            
            // ========== REFERENCIAS ==========
            item {
                Text(
                    "REFERENCIAS",
                    style = MaterialTheme.typography.labelMedium,
                    color = JuicyGreen,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            item {
                Text(
                    "1ª Referencia",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                JuicyInput(
                    "Nombre Completo",
                    d.ref1Name,
                    { viewModel.updateClientData(d.copy(ref1Name = it)) },
                    KeyboardType.Text,
                    Icons.Rounded.Person
                )
            }
            
            item {
                JuicyInput(
                    "Teléfono",
                    d.ref1Phone,
                    { if (it.length <= 10) viewModel.updateClientData(d.copy(ref1Phone = it)) },
                    KeyboardType.Phone,
                    Icons.Rounded.Phone
                )
            }
            
            item {
                Text(
                    "2ª Referencia",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                JuicyInput(
                    "Nombre Completo",
                    d.ref2Name,
                    { viewModel.updateClientData(d.copy(ref2Name = it)) },
                    KeyboardType.Text,
                    Icons.Rounded.Person
                )
            }
            
            item {
                JuicyInput(
                    "Teléfono",
                    d.ref2Phone,
                    { if (it.length <= 10) viewModel.updateClientData(d.copy(ref2Phone = it)) },
                    KeyboardType.Phone,
                    Icons.Rounded.Phone,
                    imeAction = ImeAction.Done
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Solo botón de Copiar Texto
        JuicyButton(
            text = "COPIAR TEXTO AL PORTAPAPELES",
            color = JuicyGreen,
            shadowColor = JuicyGreenShadow,
            textColor = Color.White,
            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
            onClick = { onGenerate(viewModel.generateLayout()) }
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Botón Ticket Dorado
        JuicyButton(
            text = "COMPARTIR TICKET DORADO",
            color = Color(0xFFFFD700),
            shadowColor = Color(0xFFDAA520),
            textColor = Color.Black,
            icon = Icons.Rounded.Share,
            isLoading = state.isGeneratingTicket,
            onClick = onGenerateTicket
        )
    }
}

@Composable
fun TransactionTypeSelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    val options = listOf("NUEVO", "CNCA", "2ª DISP", "LCOM")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(option) },
                color = if (isSelected) JuicyPurple else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    2.dp,
                    if (isSelected) JuicyPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (isSelected) Icons.Rounded.EmojiEvents else Icons.Rounded.Loop,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        option,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun JuicyButton(
    text: String,
    color: Color,
    shadowColor: Color,
    textColor: Color,
    height: Dp = 56.dp,
    icon: ImageVector? = null,
    iconColor: Color? = null,
    soundManager: SoundManager? = null,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    // Estado de presión para efecto 3D
    var isPressed by remember { mutableStateOf(false) }
    
    // Optimización: Cachear animationSpec
    val animSpec = remember { 
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }
    
    // Animación de desplazamiento (Hundimiento)
    val pressTranslation by animateFloatAsState(
        targetValue = if (isPressed) 6f else 0f, // Baja 6dp al presionar
        label = "pressTranslation",
        animationSpec = animSpec
    )
    
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .pointerInput(isLoading) {
                if (!isLoading) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        val up = waitForUpOrCancellation()
                        isPressed = false
                        if (up != null) {
                            soundManager?.play("pop")
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // CAPA SOMBRA (FONDO)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 6.dp) // Desplazada hacia abajo para simular profundidad
                .background(shadowColor, RoundedCornerShape(16.dp))
        )
        
        // CAPA PRINCIPAL (FRENTE)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 6.dp) // Deja espacio abajo para mostrar la sombra
                .graphicsLayer {
                    translationY = pressTranslation * density.density // Convertir dp a pixels para translationY
                }
                .background(color, RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = textColor,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.width(12.dp))
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor ?: textColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    text = if (isLoading) "PROCESANDO..." else text,
                    color = textColor,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun JuicyIconButton(
    icon: ImageVector,
    color: Color,
    shadowColor: Color? = null,
    iconColor: Color,
    size: Dp = 56.dp,
    soundManager: SoundManager? = null,
    onClick: () -> Unit
) {
    val realShadow = shadowColor ?: color.copy(alpha = 0.5f)
    
    // Estado de presión para efecto 3D
    var isPressed by remember { mutableStateOf(false) }
    
    val animSpec = remember { 
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }
    
    val pressTranslation by animateFloatAsState(
        targetValue = if (isPressed) 4f else 0f, 
        label = "pressTranslation",
        animationSpec = animSpec
    )
    
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        soundManager?.play("pop")
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // CAPA SOMBRA (FONDO)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .background(realShadow, CircleShape)
        )
        // CAPA PRINCIPAL (FRENTE)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp)
                .graphicsLayer {
                    translationY = pressTranslation * density.density
                }
                .background(color, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
fun JuicyInput(
    label: String,
    value: String,
    onValChange: (String) -> Unit,
    type: KeyboardType,
    leadingIcon: ImageVector,
    isCurrency: Boolean = false,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    placeholder: String? = null, // NUEVO: Placeholder opcional
    maxLength: Int? = null // NUEVO: Límite de caracteres
) {
    // Generar placeholder inteligente basado en el label si no se proporciona uno
    val smartPlaceholder = placeholder ?: when {
        isCurrency -> "10,000" // Sin $ porque CurrencyVisualTransformation lo agrega
        label.contains("NSS", ignoreCase = true) -> "12345678901"
        label.contains("Teléfono", ignoreCase = true) || label.contains("Celular", ignoreCase = true) -> "5512345678"
        label.contains("Nombre", ignoreCase = true) -> "Juan Pérez"
        label.contains("Monto", ignoreCase = true) -> "50000"
        label.contains("Plazo", ignoreCase = true) || label.contains("Meses", ignoreCase = true) -> "60"
        label.contains("Edad", ignoreCase = true) -> "35"
        label.contains("Antigüedad", ignoreCase = true) -> "10"
        type == KeyboardType.Number || type == KeyboardType.Decimal -> "0"
        else -> "Ingresa $label"
    }
    
    // ========== ANIMATED FOCUS STATE ==========
    val focusManager = LocalFocusManager.current
    val hapticManager = LocalHapticManager.current
    var isFocused by remember { mutableStateOf(false) }
    
    // Ring scale animation
    val ringScale by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ringScale"
    )
    
    // Ring pulse animation (infinite when focused)
    val infiniteTransition = rememberInfiniteTransition(label = "focusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val borderColor = when {
        isError -> JuicyRed
        isFocused -> JuicyBlue
        else -> MaterialTheme.colorScheme.outline
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) JuicyRed else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            
            // Character counter
            if (maxLength != null && isFocused) {
                val remaining = maxLength - value.length
                Text(
                    text = "$remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (remaining < 5) JuicyOrange else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = if (remaining < 5) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                )
            }
        }
        
        // Animated focus ring container (Premium Ghost Style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = 1f // No escalar todo el input, solo el background si se desea
                    scaleY = 1f
                }
        ) {
            // Pulsing glow background (only visible when focused)
            if (isFocused && !isError) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    JuicyBlue.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
            
            // Input Surface
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = if (isFocused) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = if (isFocused) 1.dp else 0.dp,
                        color = if (isError) JuicyRed else if (isFocused) JuicyBlue.copy(alpha = 0.3f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused != isFocused) {
                            isFocused = focusState.isFocused
                            if (focusState.isFocused) {
                                hapticManager?.light()
                            }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(start = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        leadingIcon,
                        null,
                        tint = if (isError) JuicyRed else if (isFocused) JuicyBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Native BasicTextField for total control
                BasicTextField(
                    value = value,
                    onValueChange = { input ->
                        // Play haptic on input
                        if (input != value) {
                            hapticManager?.tick()
                        }
                        
                        // Enforce max length
                        if (maxLength != null && input.length > maxLength) return@BasicTextField
                        
                        val isValidChar = input.all { it.isDigit() || it == '.' || it == '-' }
                        val singleDot = input.count { it == '.' } <= 1
                        val validMinus = if (input.contains('-')) input.indexOf('-') == 0 && input.count { it == '-' } == 1 else true

                        if (type == KeyboardType.Number || type == KeyboardType.Decimal) {
                            if (isValidChar && singleDot && validMinus) {
                                onValChange(input)
                            }
                        } else {
                            onValChange(input)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    textStyle = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium, // Menos bold, más elegante
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface 
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isCurrency) KeyboardType.Decimal else type,
                        imeAction = imeAction
                    ),
                    singleLine = true,
                    visualTransformation = if (isCurrency) CurrencyVisualTransformation() else VisualTransformation.None,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    smartPlaceholder,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                // Trailing Error Icon (Optional)
                if (isError) {
                     Icon(
                        Icons.Rounded.Error,
                        null,
                        tint = JuicyRed,
                        modifier = Modifier.padding(horizontal = 12.dp).size(20.dp)
                    )
                }
            }
            
            // Bottom Indicator Line (Animated)
             Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        color = if (isError) JuicyRed else if (isFocused) JuicyBlue else Color.Transparent
                    )
                    .graphicsLayer {
                         scaleX = if (isFocused || isError) 1f else 0f
                         transformOrigin = TransformOrigin(0.5f, 1f)
                    }
            )

        }
    }
}

@Composable
fun JuicyCard(
    color: Color, 
    shadowColor: Color, 
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.matchParentSize().padding(top = 4.dp)
                .background(shadowColor, RoundedCornerShape(20.dp))
        )
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                .background(color, RoundedCornerShape(20.dp))
                .border(2.dp, shadowColor, RoundedCornerShape(20.dp))
        ) {
            content()
        }
    }
}

@Composable
fun JuicyGradientCard(brush: Brush, shadowColor: Color, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.matchParentSize().padding(top = 4.dp)
                .background(shadowColor, RoundedCornerShape(20.dp))
        )
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                .background(brush, RoundedCornerShape(20.dp))
                .border(2.dp, shadowColor, RoundedCornerShape(20.dp))
        ) {
            content()
        }
    }
}

@Composable
fun JuicyChip(selected: Boolean, text: String, onClick: () -> Unit) {
    val bgColor = if (selected) JuicyBlue else MaterialTheme.colorScheme.surface
    val shadowColor = if (selected) JuicyBlueShadow else MaterialTheme.colorScheme.outline
    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    Box(modifier = Modifier.height(40.dp).fillMaxWidth().bouncyClick(onClick = onClick)) {
        Box(
            modifier = Modifier.matchParentSize().padding(top = 3.dp)
                .background(shadowColor, RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier.matchParentSize().padding(bottom = 3.dp)
                .background(bgColor, RoundedCornerShape(12.dp))
                .border(2.dp, shadowColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun EmptyState(
    viewModel: CalculatorViewModel,
    soundManager: SoundManager,
    isRefinanceTab: Boolean = true
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    var showLottie by remember { mutableStateOf(false) }
    var showTexts by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        
        showLottie = true
        delay(200)
        showTexts = true
        delay(400)
        showButton = true
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = showLottie,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.lottie_empty_search)
            )
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
            
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(240.dp)
                    .semantics {
                        contentDescription = context.getString(R.string.search_empty_content_description)
                    }
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        AnimatedVisibility(
            visible = showTexts,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ) + slideInVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 4 }
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.search_empty_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    text = stringResource(R.string.search_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        ) {
            EmptyStateActionButton(
                viewModel = viewModel,
                soundManager = soundManager,
                isRefinanceTab = isRefinanceTab,
                haptic = haptic
            )
        }
    }
}

@Composable
private fun EmptyStateActionButton(
    viewModel: CalculatorViewModel,
    soundManager: SoundManager,
    isRefinanceTab: Boolean,
    haptic: HapticFeedback
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            soundManager.play("pop")
            
            if (isRefinanceTab) {
                viewModel.clearRefinanceData()
            } else {
                viewModel.clearNewCreditData()
            }
        },
        modifier = Modifier
            .heightIn(min = 48.dp)
            .widthIn(min = 200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            Icons.Rounded.DeleteSweep,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.search_empty_action),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable

// =================================================================================================
// 15. OVERLAYS & DIALOGS
// =================================================================================================
fun SuccessOverlay(onDismiss: () -> Unit) {
    var offsetY by remember { mutableStateOf(-100f) }
    var alpha by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        // Animación de entrada
        animate(
            -100f,
            0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) { value, _ -> offsetY = value }
        
        animate(
            0f,
            1f,
            animationSpec = tween(300)
        ) { value, _ -> alpha = value }
        
        // Esperar antes de salir
        delay(2000)
        
        // Animación de salida
        animate(
            0f,
            -100f,
            animationSpec = tween(300)
        ) { value, _ -> offsetY = value }
        
        animate(
            1f,
            0f,
            animationSpec = tween(200)
        ) { value, _ -> alpha = value }
        
        onDismiss()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .offset(y = offsetY.dp)
                .graphicsLayer { this.alpha = alpha },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, JuicyGreen.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icono de check con fondo circular
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            JuicyGreen.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Éxito",
                        tint = JuicyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Texto
                Text(
                    text = "Copiado al portapapeles",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun RollingCurrency(
    value: BigDecimal,
    style: TextStyle,
    color: Color = Color.Unspecified
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(value) {
        animatable.snapTo(0f)
        animatable.animateTo(
            targetValue = value.toFloat(),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Text(
        text = "$${animatable.value.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toCurrency()}",
        style = style,
        color = color
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferPreviewModal(
    offer: LoanOffer,
    viewModel: CalculatorViewModel,
    soundManager: SoundManager,
    context: Context,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0) }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Header con badge de mejor opción
                if (offer.isBest) {
                    Surface(
                        color = JuicyOrange,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "MEJOR OPCIÓN",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                // Producto y tipo de tasa
                Text(
                    text = offer.product,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = offer.tipoTasa,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JuicyBlue,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(32.dp))
                
                // Hero Section - Liquidez con animación de entrada
                Text(
                    text = if(offer.amount > offer.mainValue) "REMANENTE" else "MONTO A RECIBIR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )
                
                Spacer(Modifier.height(8.dp))
                
                // ✨ HERO ANIMATION: Scale + Fade In
                var heroScaleTarget by remember { mutableFloatStateOf(0.8f) }
                var heroAlphaTarget by remember { mutableFloatStateOf(0f) }
                
                LaunchedEffect(Unit) {
                    heroScaleTarget = 1f
                    heroAlphaTarget = 1f
                }
                
                val heroScale by animateFloatAsState(
                    targetValue = heroScaleTarget,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "heroScale"
                )
                
                val heroAlpha by animateFloatAsState(
                    targetValue = heroAlphaTarget,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    label = "heroAlpha"
                )
                
                val productColors = remember(offer.product, offer.tipoTasa) {
                    ProductBranding.getColors(
                        offer.product, 
                        offer.tipoTasa == "RETENCION"
                    )
                }
                
                Text(
                    text = "$${offer.mainValue.toCurrency()}",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 56.sp,
                        letterSpacing = (-2).sp
                    ),
                    color = productColors.primary, // 🎨 Usa color del producto
                    modifier = Modifier.graphicsLayer {
                        scaleX = heroScale
                        scaleY = heroScale
                        alpha = heroAlpha
                    }
                )
                
                // Pill secundaria (Monto Bruto)
                if (offer.amount > offer.mainValue) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Monto total: $${offer.amount.toCurrency()}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(40.dp))
                
                // Detalles en tarjetas
                JuicyCard(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowColor = MaterialTheme.colorScheme.outline
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Mensualidad
                        DetailRow(
                            icon = Icons.Rounded.AttachMoney,
                            label = "MENSUALIDAD",
                            value = "$${offer.monthlyPayment.toCurrency()}",
                            valueColor = JuicyOrange
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // Plazo
                        DetailRow(
                            icon = Icons.Rounded.CalendarMonth,
                            label = "PLAZO",
                            value = "${offer.term} Meses"
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        
                        // CAT
                        DetailRow(
                            icon = Icons.AutoMirrored.Rounded.TrendingDown,
                            label = "CAT",
                            value = offer.cat
                        )
                    }
                }
                
                // Razones de aprobación (si existen)
                if (offer.reasons.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        "RAZONES DE APROBACIÓN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    offer.reasons.forEach { reason ->
                        Surface(
                            color = JuicyGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.EmojiEmotions,
                                    contentDescription = null,
                                    tint = JuicyGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    reason,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Generar JPEG
                    OutlinedButton(
                        onClick = {
                            soundManager.play("magic")
                            viewModel.generateTicketBackground(context)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .bouncyClick(soundManager) {},
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = JuicyBlue
                        ),
                        border = BorderStroke(2.dp, JuicyBlue)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "GENERAR\nJPEG",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                    
                    // Botón Continuar
                    Button(
                        onClick = {
                            soundManager.play("cash")
                            onContinue()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .bouncyClick(soundManager) {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JuicyGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "CONTINUAR",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = valueColor,
            fontSize = 20.sp
        )
    }
}

@Composable
fun ResultCard(
    offer: LoanOffer,
    onClick: () -> Unit,
    onShowAmortization: (() -> Unit)? = null, // NUEVO: Callback para simulador
    rewardsState: RewardsState = RewardsState() // Default para compatibilidad
) {
    // ========== OBTENER ESTILO ACTIVO (CACHEADO) ==========
    val activeCardStyle = remember(rewardsState.active.currentCardStyle) {
        CardStyleCatalog.getCardStyleById(rewardsState.active.currentCardStyle)
            ?: CardStyleCatalog.ALL_CARD_STYLES.first()
    }
    // ===========================================
    
    // ========== OBTENER COLORES POR PRODUCTO ==========
    val usePremiumDesign = offer.tipoTasa == "RETENCION"
    val productColors = remember(offer.product, usePremiumDesign) {
        ProductBranding.getColors(offer.product, usePremiumDesign)
    }
    
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    // Usar colores del producto para tarjetas destacadas
    val actualColor = when {
        usePremiumDesign -> productColors.primary
        offer.isBest -> productColors.primary
        else -> surfaceColor
    }
    val shadow = when {
        usePremiumDesign || offer.isBest -> productColors.shadow
        else -> outlineColor
    }
    val contentColor = if (usePremiumDesign || offer.isBest) Color.White else onSurfaceColor
    // =================================================

    // Optimización: Solo crear animaciones infinitas cuando son necesarias
    val glowAlpha: Float
    val glowOffset: Float
    
    if (usePremiumDesign || offer.isBest) {
        val infiniteTransition = rememberInfiniteTransition(label = "bestOfferGlow")
        glowAlpha = infiniteTransition.animateFloat(
            initialValue = 0.3f, targetValue = 0.8f,
            animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "glowAlpha"
        ).value
        glowOffset = infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
            label = "glowOffset"
        ).value
    } else {
        glowAlpha = 0f
        glowOffset = 0f
    }
    
    // Optimización: Solo crear shimmer cuando está activo
    val shimmerOffset: Float
    
    if (activeCardStyle.hasShimmer && !offer.isBest) {
        val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
        shimmerOffset = shimmerTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerOffset"
        ).value
    } else {
        shimmerOffset = 0f
    }
    // =======================================

    JuicyCard(
        color = if (activeCardStyle.hasGlassmorphism && !offer.isBest) {
            actualColor.copy(alpha = 0.7f)
        } else {
            actualColor
        },
        shadowColor = if (activeCardStyle.hasGlow) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
            shadow
        },
        modifier = Modifier
            .then(
                if (usePremiumDesign || offer.isBest) {
                    Modifier.drawWithContent {
                        drawContent()
                        val glowColors = listOf<Color>(
                            productColors.primary.copy(alpha = glowAlpha),
                            productColors.accent.copy(alpha = glowAlpha * 0.8f),
                            productColors.primary.copy(alpha = glowAlpha)
                        )
                        val glowBrush = Brush.linearGradient(
                            colors = glowColors,
                            start = Offset(size.width * glowOffset, 0f),
                            end = Offset(size.width * glowOffset + size.width * 0.5f, size.height)
                        )
                        drawRoundRect(
                            brush = glowBrush,
                            alpha = 0.5f,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                        )
                    }
                } else Modifier
            )
            .then(
                // ========== APLICAR SHIMMER CON COLORES DEL PRODUCTO ==========
                if (activeCardStyle.hasShimmer && !offer.isBest) {
                    Modifier.drawWithContent {
                        drawContent()
                        val shimmerBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                productColors.shimmer, // 🎨 Usa color del producto
                                productColors.accent.copy(alpha = 0.4f), // 🎨 Acento del producto
                                Color.Transparent
                            ),
                            start = Offset(size.width * shimmerOffset - size.width * 0.3f, 0f),
                            end = Offset(size.width * shimmerOffset, size.height)
                        )
                        drawRect(brush = shimmerBrush)
                    }
                } else Modifier
                // ==============================================================
            )
            .then(
                // ========== APLICAR GLASSMORPHISM ==========
                if (activeCardStyle.hasGlassmorphism && !offer.isBest) {
                    Modifier.border(
                        width = activeCardStyle.borderWidth.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else if (activeCardStyle.borderWidth > 1f) {
                    Modifier.border(
                        width = activeCardStyle.borderWidth.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
                // ===========================================
            )
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .padding(20.dp), // Más espacio interno
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. HEADER ---
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = offer.product,
                        fontWeight = FontWeight.Black,
                        color = contentColor,
                        fontSize = 14.sp
                    )
                    Text(
                        text = offer.tipoTasa,
                        fontWeight = FontWeight.Bold,
                        color = if (usePremiumDesign) Color.White.copy(alpha = 0.9f) 
                               else if (offer.isBest) Color.White.copy(alpha = 0.8f) 
                               else JuicyBlue,
                        fontSize = 10.sp
                    )
                }
                if (offer.tipoTasa == "RETENCION") {
                    // Badge premium con gradiente
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF9C27B0),
                                        Color(0xFF7B1FA2)
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                tint = JuicyYellow,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "TASA PREFERENCIAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                } else if (offer.isBest) {
                    Surface(color = Color.White, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "MEJOR OPCIÓN", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = JuicyOrange, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // --- CAT Badge ---
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                CatBadge(
                    cat = offer.cat,
                    isPreferencial = usePremiumDesign
                )
            }
            
            // --- Indicador de Ahorro (solo para ofertas preferenciales) ---
            if (usePremiumDesign) {
                Spacer(Modifier.height(12.dp))
                SavingsIndicator(
                    traditionalRate = 44.2f, // CAT Tradicional referencia
                    preferentialRate = offer.cat.replace("%", "").toFloatOrNull() ?: 36f
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- 2. HERO SECTION (Liquidez) ---
            Text(
                text = if(offer.amount > offer.mainValue) "REMANENTE" else "MONTO A RECIBIR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.7f)
            )

            // Rolling Currency Gigante
            RollingCurrency(
                value = offer.mainValue,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp, // Tamaño Hero
                    letterSpacing = (-1.5).sp
                ),
                color = contentColor
            )

            // --- 3. PILL SECUNDARIA (Monto Bruto) ---
            // Solo mostrar si hay diferencia (CNCA)
            if (offer.amount > offer.mainValue) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = if(offer.isBest) Color.Black.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(50),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ReceiptLong, null, 
                            tint = contentColor.copy(alpha = 0.8f), 
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Monto total: $${offer.amount.toCurrency()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.15f))
            Spacer(Modifier.height(12.dp))

            // --- 4. FOOTER (Pago y Plazo) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MENSUALIDAD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = contentColor.copy(alpha = 0.7f))
                    Text("$${offer.monthlyPayment.toCurrency()}", fontWeight = FontWeight.ExtraBold, color = contentColor, fontSize = 18.sp)
                }
                
                // Línea vertical separadora
                Box(Modifier.width(1.dp).height(24.dp).background(contentColor.copy(alpha = 0.2f)))

                Column(horizontalAlignment = Alignment.End) {
                    Text("PLAZO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = contentColor.copy(alpha = 0.7f))
                    Text("${offer.term} Meses", fontWeight = FontWeight.ExtraBold, color = contentColor, fontSize = 18.sp)
                }
            }
            
            // Botón de Amortización (si está disponible)
            if (onShowAmortization != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowAmortization() },
                    shape = RoundedCornerShape(8.dp),
                    color = if (offer.isBest) Color.White.copy(alpha = 0.2f) else JuicyBlue.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.TableChart,
                            contentDescription = "Ver Amortización",
                            tint = if (offer.isBest) Color.White else JuicyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Ver Tabla de Amortización",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (offer.isBest) Color.White else JuicyBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebtRowItem(
    debt: Debt,
    onUpdate: (Debt) -> Unit,
    onRemove: () -> Unit,
    soundManager: SoundManager?
) {
    JuicyCard(
        color = MaterialTheme.colorScheme.surface,
        shadowColor = MaterialTheme.colorScheme.outline
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.background(JuicyBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ReceiptLong,
                        null,
                        tint = JuicyBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                DropdownCompact(
                    label = "Cotizador",
                    options = FinancialConfig.PRODUCTS.keys.toList(),
                    selectedValue = debt.product,
                    modifier = Modifier.weight(1f),
                    soundManager = soundManager
                ) { onUpdate(debt.copy(product = it)) }
                JuicyIconButton(
                    icon = Icons.Rounded.Close,
                    color = JuicyRed.copy(alpha = 0.1f),
                    shadowColor = Color.Transparent,
                    iconColor = JuicyRed,
                    size = 32.dp,
                    onClick = onRemove,
                    soundManager = soundManager
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactInput(
                    value = debt.balance,
                    label = "ADEUDA",
                    modifier = Modifier.weight(1f),
                    inputType = KeyboardType.Decimal,
                    isCurrency = true
                ) { onUpdate(debt.copy(balance = it)) }
                CompactInput(
                    value = debt.payment,
                    label = "PAGO MENSUAL",
                    modifier = Modifier.weight(1f),
                    inputType = KeyboardType.Decimal,
                    isCurrency = true
                ) { onUpdate(debt.copy(payment = it)) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactInput(
                    value = debt.rate,
                    label = "TASA %",
                    modifier = Modifier.weight(1f),
                    inputType = KeyboardType.Decimal
                ) { onUpdate(debt.copy(rate = it)) }
                DropdownCompact(
                    label = "PLAZO",
                    options = FinancialConfig.STANDARD_TERMS,
                    selectedValue = debt.originalTerm,
                    modifier = Modifier.weight(1f),
                    soundManager = soundManager
                ) { onUpdate(debt.copy(originalTerm = it)) }
                CompactInput(
                    value = debt.paymentsMade,
                    label = "PAGADOS",
                    modifier = Modifier.weight(1f),
                    inputType = KeyboardType.Number
                ) { onUpdate(debt.copy(paymentsMade = it)) }
            }
        }
    }
}

@Composable
fun CompactInput(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    inputType: KeyboardType = KeyboardType.Text,
    isCurrency: Boolean = false,
    onChange: (String) -> Unit
) {
    Column(modifier) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                if (inputType == KeyboardType.Number || inputType == KeyboardType.Decimal) {
                    if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                        onChange(input)
                    }
                } else {
                    onChange(input)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isCurrency) KeyboardType.Decimal else inputType,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            visualTransformation = if (isCurrency) CurrencyVisualTransformation() else VisualTransformation.None,
            prefix = if (isCurrency) {
                { Text("$", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = JuicyBlue,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownCompact(
    label: String,
    options: List<String>,
    selectedValue: String,
    modifier: Modifier = Modifier,
    soundManager: SoundManager? = null,
    onSelectionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Función para obtener color de marca (igual que en JuicyProviderDropdown)
    fun getBrandColor(name: String): Color {
        return when (name) {
            "CONSUBANCO" -> JuicyBlue
            "CONSUPAGO" -> JuicyOrange
            "OPCIPRES" -> JuicyGreen
            "MASNOMINA" -> JuicyRed
            else -> JuicyBlue // Default
        }
    }
    
    // Detectar si es un dropdown de financiera
    val isFinancialProvider = options.any { it in listOf("CONSUBANCO", "CONSUPAGO", "OPCIPRES", "MASNOMINA") }
    val selectedColor = if (isFinancialProvider) getBrandColor(selectedValue) else JuicyBlue
    
    Column(modifier) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { 
                expanded = it
                soundManager?.play(if (it) "pop" else "success")
            }) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                leadingIcon = if (isFinancialProvider) {
                    {
                        // Círculo de color de marca
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(selectedColor, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                } else null,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth().height(50.dp),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = if (isFinancialProvider) selectedColor else MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = selectedColor // Color dinámico
                ),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded, 
                onDismissRequest = { 
                    expanded = false
                    soundManager?.play("success")
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .animateContentSize()
            ) {
                val haptic = LocalHapticFeedback.current // Mover fuera del forEach
                
                options.forEach { item ->
                    val itemColor = if (isFinancialProvider) getBrandColor(item) else MaterialTheme.colorScheme.onSurface
                    
                    DropdownMenuItem(
                        text = { 
                            Text(
                                item, 
                                fontWeight = FontWeight.Bold,
                                color = if (item == selectedValue && isFinancialProvider) itemColor else MaterialTheme.colorScheme.onSurface
                            ) 
                        },
                        leadingIcon = if (isFinancialProvider) {
                            {
                                Icon(
                                    Icons.Rounded.Business,
                                    null,
                                    tint = itemColor
                                )
                            }
                        } else null,
                        onClick = { 
                            onSelectionChange(item)
                            expanded = false
                            soundManager?.play("magic")
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.bouncyClick(soundManager) {}
                    )
                    // Separador entre opciones
                    if (item != options.last() && isFinancialProvider) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigFormContent(
    viewModel: CalculatorViewModel,
    ctx: Context,
    soundManager: SoundManager,
    isMandatory: Boolean = false,
    onSaved: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var tempName by remember { mutableStateOf(state.userProfile.name) }
    var tempUser by remember { mutableStateOf(state.userProfile.username) }
    var tempPwd by remember { mutableStateOf(state.userProfile.password) }
    var tempSup by remember { mutableStateOf(state.userProfile.supervisor) }
    
    // Validación para botón habilitado
    val isValid = tempName.isNotBlank() && tempUser.isNotBlank() && 
                 tempPwd.isNotBlank() && tempSup.isNotBlank()

    Column(
        Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isMandatory) {
            Image(
                painter = painterResource(id = R.drawable.avatar_mascot),
                contentDescription = "Buddy",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(JuicyOrange, CircleShape)
                    .border(3.dp, JuicyOrange, CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "¡Bienvenido Asesor!",
                style = MaterialTheme.typography.headlineMedium,
                color = JuicyBlue,
                fontWeight = FontWeight.Black
            )
            Text(
                "Para comenzar, necesito configurar tu perfil.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        } else {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = null,
                tint = JuicyBlue,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "CONFIGURACIÓN",
                style = MaterialTheme.typography.titleLarge,
                color = JuicyBlue,
                fontWeight = FontWeight.Black
            )
            Text(
                "Datos del Asesor",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(24.dp))
        }

        // Nombre completo
        JuicyInput(
            "Nombre Completo",
            tempName,
            { tempName = it },
            KeyboardType.Text,
            Icons.Rounded.Person
        )

        Spacer(Modifier.height(12.dp))

        // Usuario (BP)
        JuicyInput(
            "Usuario (BP)",
            tempUser,
            { tempUser = it },
            KeyboardType.Text,
            Icons.Rounded.Badge
        )

        Spacer(Modifier.height(12.dp))

        // Contraseña
        JuicyInput(
            "Contraseña del Sistema",
            tempPwd,
            { tempPwd = it },
            KeyboardType.Text,
            Icons.Rounded.Business
        )

        Spacer(Modifier.height(12.dp))

        // Supervisor
        JuicyInput(
            "Nombre del Supervisor",
            tempSup,
            { tempSup = it },
            KeyboardType.Text,
            Icons.Rounded.Person,
            imeAction = ImeAction.Done
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Estos datos se usarán automáticamente en el layout de captura",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        JuicyButton(
            text = if (isMandatory) "¡COMENZAR AHORA!" else "GUARDAR CONFIGURACIÓN",
            color = if (isValid) JuicyGreen else Color.Gray,
            shadowColor = if (isValid) JuicyGreenShadow else Color.DarkGray,
            textColor = Color.White,
            icon = if (isMandatory) Icons.Rounded.PlayArrow else Icons.Rounded.EmojiEvents,
            soundManager = soundManager
        ) {
            if (isValid) {
                viewModel.saveProfile(
                    ctx,
                    UserProfile(tempName, tempUser, tempPwd, tempSup)
                )
                onSaved()
            }
        }
    }
}

@Composable
fun ConfigDialog(viewModel: CalculatorViewModel, ctx: Context, soundManager: SoundManager) {
    Dialog(onDismissRequest = { viewModel.toggleConfigDialog(false) }) {
        JuicyCard(
            color = MaterialTheme.colorScheme.surface,
            shadowColor = MaterialTheme.colorScheme.outline
        ) {
            ConfigFormContent(viewModel, ctx, soundManager)
        }
    }
}

@Composable
fun MandatoryConfigScreen(viewModel: CalculatorViewModel, ctx: Context, soundManager: SoundManager) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        JuicyCard(
            color = MaterialTheme.colorScheme.surface,
            shadowColor = MaterialTheme.colorScheme.outline
        ) {
            ConfigFormContent(viewModel, ctx, soundManager, isMandatory = true)
        }
    }
}

data class OnboardingStep(val title: String, val desc: String, val icon: ImageVector)

@Composable
fun JuicyOnboarding(onFinished: () -> Unit) {
    val steps = listOf(
        OnboardingStep(
            "¡Hola, soy Buddy!",
            "Soy tu asistente financiero.",
            Icons.Rounded.EmojiEmotions
        ),
        OnboardingStep(
            "Refinanciamientos",
            "Calculo remanentes y normativa.",
            Icons.Rounded.Loop
        )
    )
    var currentStep by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (currentStep == 0) {
                Image(
                    painter = painterResource(id = R.drawable.avatar_mascot),
                    contentDescription = "Buddy",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(JuicyBlue, CircleShape)
                        .border(4.dp, JuicyBlue, CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    steps[currentStep].icon,
                    null,
                    tint = JuicyBlue,
                    modifier = Modifier.size(100.dp)
                )
            }
            Text(steps[currentStep].title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(30.dp))
            JuicyButton(
                text = if (currentStep < steps.size - 1) "SIGUIENTE" else "¡COMENZAR!",
                color = JuicyGreen,
                shadowColor = JuicyGreenShadow,
                textColor = Color.White
            ) {
                if (currentStep < steps.size - 1) currentStep++ else onFinished()
            }
        }
    }
}

@Composable
fun JuicySliderInput(
    label: String,
    value: String,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (String) -> Unit,
    isError: Boolean = false
) {
    val floatValue = value.replace(",", "").replace("$", "").toFloatOrNull() ?: range.start
    
    // 🎮 NUEVO: Haptic Manager para feedback en slider
    val hapticManager = LocalHapticManager.current
    var lastSnappedValue by remember { mutableStateOf(floatValue) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        JuicyInput(
            label = label,
            value = value,
            onValChange = onValueChange,
            type = KeyboardType.Number,
            leadingIcon = Icons.Rounded.AttachMoney,
            isCurrency = true,
            isError = isError,
            imeAction = ImeAction.Done
        )

        Spacer(Modifier.height(4.dp))

        Slider(
            value = floatValue,
            onValueChange = { newVal ->
                val snappedValue = newVal.toInt()
                // ✨ Haptic tick solo cuando cambia el valor snapped
                if (snappedValue.toFloat() != lastSnappedValue) {
                    hapticManager?.tick()
                    lastSnappedValue = snappedValue.toFloat()
                }
                onValueChange(snappedValue.toString())
            },
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = JuicyBlue,
                activeTrackColor = JuicyBlue,
                inactiveTrackColor = JuicyBlue.copy(alpha = 0.2f)
            )
        )
    }
}

data class RPGStep(val text: String, val focusArea: String)

@Composable
fun RPGDialogOverlay(
    stepIndex: Int,
    soundManager: SoundManager,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val script = listOf(
        RPGStep(
            "¡Hola Asesor! Soy Buddy 🐻. ¿Listo para vender más? Te enseñaré cómo funciona esto.",
            "Intro"
        ),
        RPGStep(
            "Lo primero que verás son las celdas de entrada. ¡Escribe aquí los datos del cliente!",
            "Inputs"
        ),
        RPGStep(
            "Usa los menús desplegables (dropdowns) para seleccionar opciones rápidas como plazos o convenios.",
            "Dropdowns"
        ),
        RPGStep(
            "Los botones de acción son tus mejores amigos. Úsalos para Calcular, Borrar o Agregar Deudas.",
            "Buttons"
        ),
        RPGStep(
            "Abajo tienes las pestañas 'Refinanciar' (CNCA) y 'Nuevo'. ¡Cambia entre ellas según lo que necesites!",
            "Tabs"
        ),
        RPGStep(
            "¡Yo hago la magia! Calculo redondeo SIPRE, mejora de tasa y liquidez automáticamente.",
            "Intelligence"
        ),
        RPGStep(
            "Al final, toca una oferta para ver la Vista Previa y generar el texto para WhatsApp.",
            "End"
        )
    )

    if (stepIndex >= script.size) {
        LaunchedEffect(Unit) { onFinish() }
        return
    }

    val currentData = script[stepIndex]
    
    var typingProgress by remember(currentData.text, stepIndex) { mutableStateOf(0) }
    var isTyping by remember { mutableStateOf(true) }
    
    val displayedText by remember(currentData.text, stepIndex) {
        androidx.compose.runtime.derivedStateOf {
            currentData.text.take(typingProgress)
        }
    }

    LaunchedEffect(currentData.text, stepIndex) {
        isTyping = true
        typingProgress = 0
        delay(300)

        currentData.text.forEachIndexed { index, char ->
            typingProgress = index + 1

            if (char != ' ') {
                soundManager.play("villager")
            }

            delay(40)
        }
        isTyping = false
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .offset(y = (-20).dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
                    val bounce by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -10f,
                        animationSpec = infiniteRepeatable(
                            tween(500, easing = LinearEasing),
                            RepeatMode.Reverse
                        ),
                        label = "y"
                    )

                    Box(
                        modifier = Modifier
                            .offset(y = bounce.dp)
                            .size(80.dp)
                            .background(JuicyBlue, CircleShape)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_mascot), 

                            contentDescription = "Buddy",
                            modifier = Modifier.size(60.dp).clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Surface(
                        color = JuicyBlue,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    ) {
                        Text(
                            "BUDDY (NPC)",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    ),
                    border = BorderStroke(3.dp, JuicyBlue),
                    shadowElevation = 10.dp
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = displayedText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            minLines = 3
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isTyping) {
                                Button(
                                    onClick = onNext,
                                    colors = ButtonDefaults.buttonColors(containerColor = JuicyGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = ButtonDefaults.buttonElevation(4.dp)
                                ) {
                                    Text(
                                        text = if (stepIndex == script.lastIndex) "¡VAMOS!" else "ENTENDIDO ►",
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            } else {
                                Text("...", color = JuicyBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JuicyLottieSuccess(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_success))
    LottieAnimation(
        composition = composition,
        iterations = 1,
        modifier = modifier
    )
}

@Composable
fun JuicyLottieLoader(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}

/**
 * Overlay de celebración para racha diaria
 * Se muestra cuando el usuario mantiene su racha consecutiva
 */
@Composable
fun StreakOverlay(streak: Int, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A0A00).copy(alpha = 0.98f),
                        Color.Black.copy(alpha = 0.95f)
                    ),
                    radius = 1200f
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Evitar clicks accidentales */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_fire))
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(220.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "¡RACHA DE $streak DÍAS!",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = JuicyOrange,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    JuicyOrangeGlow.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            blendMode = androidx.compose.ui.graphics.BlendMode.Screen
                        )
                    }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "¡Estás on fire! 🔥\nVuelve mañana para mantenerla.",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = JuicyOrangeGlow,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(60.dp)
                    .background(GradientOrange, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = "¡VAMOS!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

/**
 * Overlay de celebración para subida de nivel
 * Se muestra cuando el usuario alcanza un nuevo nivel y desbloquea recompensas
 */
@Composable
fun LevelUpOverlay(level: Int, reward: String?, onDismiss: () -> Unit) {
    val GoldColor = Color(0xFFFFD700)
    val GoldGlow = Color(0xFFFFF176)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        JuicyPurple.copy(alpha = 0.4f),
                        Color(0xFF0A0E27).copy(alpha = 0.98f),
                        Color.Black.copy(alpha = 0.95f)
                    ),
                    radius = 1500f
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Evitar clicks accidentales */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_levelup))
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(240.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "¡NIVEL $level ALCANZADO!",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = GoldColor,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    GoldGlow.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            blendMode = androidx.compose.ui.graphics.BlendMode.Screen
                        )
                    }
            )

            Spacer(Modifier.height(32.dp))

            if (reward != null) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1A1F3A),
                    border = BorderStroke(3.dp, GoldColor),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1A1F3A),
                                    Color(0xFF0F1426)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(28.dp)
                    ) {
                        Icon(
                            Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = GoldColor,
                            modifier = Modifier.size(56.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Nuevo Título Desbloqueado",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldGlow,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = reward,
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldColor,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(Modifier.height(36.dp))
            } else {
                Spacer(Modifier.height(16.dp))
            }

            val buttonGradient = if (reward != null) {
                Brush.linearGradient(
                    colors = listOf(GoldColor, Color(0xFFFFB300))
                )
            } else {
                GradientGreen
            }
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(64.dp)
                    .background(buttonGradient, RoundedCornerShape(18.dp))
            ) {
                Text(
                    text = if (reward != null) "RECLAMAR" else "¡CONTINUAR!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (reward != null) Color(0xFF1A0F00) else Color.White,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
// ==========================================
// NUEVOS DROPDOWNS "JUICY" CON COLOR
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuicyProviderDropdown(
    selectedValue: String,
    onSelectionChange: (String) -> Unit
) {
    val options = listOf("CONSUBANCO", "CONSUPAGO", "OPCIPRES", "MASNOMINA")
    var expanded by remember { mutableStateOf(false) }

    // Mapa de colores por marca
    fun getBrandColor(name: String): Color {
        return when (name) {
            "CONSUBANCO" -> JuicyBlue      // Azul
            "CONSUPAGO" -> JuicyOrange     // Naranja
            "OPCIPRES" -> JuicyGreen       // Verde
            "MASNOMINA" -> JuicyRed        // Rojo
            else -> Color.Gray
        }
    }

    val selectedColor = getBrandColor(selectedValue)

    Column {
        Text(
            "FINANCIERA",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            // Campo de Texto Principal
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth()
                    .height(56.dp), // Un poco más alto para elegancia
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black, // Letra más gruesa
                    fontFamily = FontFamily.SansSerif,
                    color = selectedColor // El texto toma el color de la marca
                ),
                leadingIcon = {
                    // Círculo de color de la marca
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(selectedColor, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp), // Bordes más redondeados
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedColor, // Borde del color de la marca
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Lista Desplegable Estilizada
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                options.forEach { option ->
                    val brandColor = getBrandColor(option)
                    
                    DropdownMenuItem(
                        text = { 
                            Text(
                                option, 
                                fontWeight = FontWeight.Bold,
                                color = if (option == selectedValue) brandColor else MaterialTheme.colorScheme.onSurface
                            ) 
                        },
                        leadingIcon = {
                            // Icono corporativo (Maletín o Edificio)
                            Icon(
                                Icons.Rounded.Business, 
                                null, 
                                tint = brandColor 
                            )
                        },
                        onClick = {
                            onSelectionChange(option)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                    // Separador sutil entre opciones
                    if (option != options.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuicyTermDropdown(
    selectedValue: Int,
    onSelectionChange: (Int) -> Unit
) {
    val options = listOf(12, 18, 24, 36, 48, 54, 60)
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            "PLAZO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = "$selectedValue Meses",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth()
                    .height(56.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                ),
                leadingIcon = {
                    Icon(Icons.Rounded.CalendarMonth, null, tint = JuicyPurple)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JuicyPurple,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                options.forEach { term ->
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$term Meses", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                // Barra visual de duración (más larga si el plazo es mayor)
                                val widthFraction = term / 60f
                                Box(
                                    modifier = Modifier
                                        .height(4.dp)
                                        .width(40.dp * widthFraction) // Ancho dinámico
                                        .background(JuicyPurple.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                                )
                            }
                        },
                        onClick = {
                            onSelectionChange(term)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun AcademyScreen(viewModel: CalculatorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.School,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Academia ConsuCalcu",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Módulos de capacitación próximamente.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun RewardUnlockOverlay(
    unlockedItems: List<Any>,
    onDismiss: () -> Unit
) {
    // Placeholder interaction
    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Auto dismiss for now to unblock
        kotlinx.coroutines.delay(100)
        onDismiss()
    }
}

@Composable
fun RewardsGalleryScreen(viewModel: CalculatorViewModel, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Galería de Recompensas", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        JuicyButton(
            text = "VOLVER", 
            color = MaterialTheme.colorScheme.primary, 
            shadowColor = MaterialTheme.colorScheme.primary, 
            textColor = android.graphics.Color.WHITE.let { androidx.compose.ui.graphics.Color(it) }, 
            onClick = onBack
        )
    }
}

@Composable
fun ThemeSelectorScreen(viewModel: CalculatorViewModel, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
        Text("Selector de Temas")
        JuicyButton(text="VOLVER", color=MaterialTheme.colorScheme.primary, shadowColor=MaterialTheme.colorScheme.primary, textColor=androidx.compose.ui.graphics.Color.White, onClick=onBack)
    }
}

@Composable
fun ParticleSelectorScreen(viewModel: CalculatorViewModel, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
        Text("Selector de Partículas")
        JuicyButton(text="VOLVER", color=MaterialTheme.colorScheme.primary, shadowColor=MaterialTheme.colorScheme.primary, textColor=androidx.compose.ui.graphics.Color.White, onClick=onBack)
    }
}

@Composable
fun AvatarSelectorScreen(viewModel: CalculatorViewModel, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
        Text("Selector de Avatares")
        JuicyButton(text="VOLVER", color=MaterialTheme.colorScheme.primary, shadowColor=MaterialTheme.colorScheme.primary, textColor=androidx.compose.ui.graphics.Color.White, onClick=onBack)
    }
}

@Composable
fun CardStyleSelectorScreen(viewModel: CalculatorViewModel, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
        Text("Selector de Estilos")
        JuicyButton(text="VOLVER", color=MaterialTheme.colorScheme.primary, shadowColor=MaterialTheme.colorScheme.primary, textColor=androidx.compose.ui.graphics.Color.White, onClick=onBack)
    }
}

// =================================================================================================
// NEW FEATURES UI COMPONENTS
// =================================================================================================

/**
 * Comparador Side-by-Side de Ofertas
 * Muestra una tabla comparativa de múltiples ofertas de crédito
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersComparator(
    offers: List<LoanOffer>,
    onDismiss: () -> Unit,
    onSelectOffer: (LoanOffer) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Comparador de Ofertas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Header de la tabla
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Producto", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Monto", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                Text("Mensualidad", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                Text("Plazo", Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Filas de ofertas
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                itemsIndexed(offers) { index, offer ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectOffer(offer) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (offer.isBest) JuicyGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (offer.isBest) BorderStroke(2.dp, JuicyGreen) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1.2f)) {
                                Text(offer.product, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                if (offer.isBest) {
                                    Text("⭐ Mejor Opción", fontSize = 9.sp, color = JuicyGreen)
                                }
                            }
                            Text("$${offer.amount.toCurrency()}", Modifier.weight(1f), fontSize = 13.sp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                            Text("$${offer.monthlyPayment.toCurrency()}", Modifier.weight(1.2f), fontSize = 13.sp, textAlign = TextAlign.End)
                            Text("${offer.term}m", Modifier.weight(0.8f), fontSize = 11.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            JuicyButton(
                text = "CERRAR",
                color = MaterialTheme.colorScheme.secondary,
                shadowColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                textColor = Color.White,
                onClick = onDismiss
            )
        }
    }
}

/**
 * Simulador de Amortización
 * Muestra la tabla de amortización completa con desglose mes a mes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmortizationSimulator(
    offer: LoanOffer,
    amortizationTable: List<AmortizationRow>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Text(
                "Tabla de Amortización",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${offer.product} - ${offer.term} meses",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Resumen visual
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = JuicyBlue.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Monto", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("$${offer.amount.toCurrency()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JuicyBlue)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Mensualidad", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("$${offer.monthlyPayment.toCurrency()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JuicyGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total a Pagar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        val totalPaid = amortizationTable.sumOf { it.payment }
                        Text("$${totalPaid.toCurrency()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JuicyOrange)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Tabla de amortización
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mes", Modifier.weight(0.6f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text("Interés", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.End)
                        Text("Capital", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.End)
                        Text("Saldo", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.End)
                    }
                }
                
                // Rows
                itemsIndexed(amortizationTable) { index, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${row.month}", Modifier.weight(0.6f), fontSize = 11.sp)
                        Text("$${row.interest.toCurrency()}", Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.End, color = JuicyRed.copy(alpha = 0.7f))
                        Text("$${row.principal.toCurrency()}", Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.End, color = JuicyGreen.copy(alpha = 0.7f))
                        Text("$${row.balance.toCurrency()}", Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            JuicyButton(
                text = "CERRAR",
                color = MaterialTheme.colorScheme.secondary,
                shadowColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                textColor = Color.White,
                onClick = onDismiss
            )
        }
    }
}

/**
 * Indicador de validación de NSS
 * Se integra en el JuicyInput existente
 */
@Composable
fun NSSValidationIndicator(nss: String) {
    if (nss.length == 11) {
        val isValid = nss.isValidNSS()
        Icon(
            imageVector = if (isValid) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
            contentDescription = if (isValid) "NSS Válido" else "NSS Inválido",
            tint = if (isValid) JuicyGreen else JuicyRed,
            modifier = Modifier.size(24.dp)
        )
    }
}
