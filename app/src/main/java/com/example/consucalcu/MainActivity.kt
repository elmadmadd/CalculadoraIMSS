package com.example.consucalcu

import kotlinx.coroutines.Job

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap

import android.graphics.Paint
import android.graphics.Typeface
import android.media.AudioAttributes

import android.os.Build
import android.os.Bundle
import android.graphics.Canvas
import android.media.SoundPool
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
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Savings
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
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.Vibration
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

// ========== ACADEMY SYSTEM IMPORTS ==========
import com.example.consucalcu.academy.*
import com.example.consucalcu.ui.*
import com.example.consucalcu.features.*
import androidx.compose.material.icons.rounded.History

// ========== HAPTIC FEEDBACK SYSTEM ==========
val LocalHapticManager = compositionLocalOf<HapticManager?> { null }
// ========== HAPTIC FEEDBACK SYSTEM ==========
// =================================================================================================
// =================================================================================================
// =================================================================================================
// MAIN ACTIVITY
// =================================================================================================
class SettingsAwareHapticFeedback(
    private val delegate: androidx.compose.ui.hapticfeedback.HapticFeedback,
    private val isHapticEnabled: () -> Boolean
) : androidx.compose.ui.hapticfeedback.HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: androidx.compose.ui.hapticfeedback.HapticFeedbackType) {
        if (isHapticEnabled()) {
            delegate.performHapticFeedback(hapticFeedbackType)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            // Inicializar Sistemas Premium
            val context = LocalContext.current
            val hapticManager = remember { HapticManager(context) }
            
            // Create ViewModel instance
            val viewModel: CalculatorViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()

            // Sincronizar estado de vibración con HapticManager
            LaunchedEffect(state.isHapticEnabled) {
                hapticManager.isHapticEnabled = state.isHapticEnabled
            }

            // Envolver HapticFeedback nativo de Compose
            val standardHaptic = androidx.compose.ui.platform.LocalHapticFeedback.current
            val customHapticFeedback = remember(standardHaptic, state.isHapticEnabled) {
                SettingsAwareHapticFeedback(standardHaptic) { state.isHapticEnabled }
            }
            
            // Pre-load Lottie
            LaunchedEffect(Unit) {
               LottieCache.preload(context, listOf(
                   R.raw.lottie_fire, 
                   R.raw.lottie_levelup
               ))
            }

            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            androidx.compose.runtime.CompositionLocalProvider(
                LocalHapticManager provides hapticManager,
                androidx.compose.ui.platform.LocalHapticFeedback provides customHapticFeedback
            ) {
            

            
            // Create dynamic color scheme based on active theme with animated colors
            // ===== ESQUEMA DE COLOR MONOCROMÁTICO PREMIUM =====
            val dynamicColors = if (isDarkTheme) {
                darkColorScheme(
                    primary = Color.White,              // Textos e íconos principales = blancos
                    onPrimary = Color.Black,
                    primaryContainer = Color(0xFF2C2C2E),
                    onPrimaryContainer = Color.White,
                    secondary = GoldAccent,             // Dorado como secondary
                    onSecondary = Color.Black,
                    secondaryContainer = Color(0xFF3A3A3C),
                    onSecondaryContainer = Color.White,
                    tertiary = GoldAccentLight,
                    onTertiary = Color.Black,
                    background = BgDark,                // Negro puro
                    onBackground = TextLight,
                    surface = SurfaceDark,              // Gris oscuro neutro
                    onSurface = TextLight,
                    surfaceVariant = Color(0xFF2C2C2E),
                    onSurfaceVariant = TextLight.copy(alpha = 0.7f),
                    outline = BorderDark,
                    error = ErrorRed,
                    onError = Color.White
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF1C1C1E),        // Textos e íconos principales = oscuros
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFE5E5EA),
                    onPrimaryContainer = TextDark,
                    secondary = GoldAccentDark,         // Dorado oscuro como secondary
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFF2F2F7),
                    onSecondaryContainer = TextDark,
                    tertiary = GoldAccent,
                    onTertiary = Color.White,
                    background = BgLight,               // Gris claro
                    onBackground = TextDark,
                    surface = SurfaceLight,             // Blanco puro
                    onSurface = TextDark,
                    surfaceVariant = Color(0xFFF2F2F7),
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

val BD_ZERO = BigDecimal.ZERO
val BD_500 = BigDecimal("500")
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
    val iva: BigDecimal,
    val principal: BigDecimal,
    val balance: BigDecimal
)

/**
 * Genera la tabla de amortización asegurando que se respete el pago mensual fijo.
 * Calcula implícitamente la tasa de interés que cuadra exactamente la operación.
 * @param amount Monto del préstamo
 * @param term Plazo en meses
 * @param fixedPayment Pago mensual fijo determinado por el cotizador
 * @return Lista de filas de amortización
 */
fun calculateAmortizationTable(
    amount: BigDecimal,
    term: Int,
    fixedPayment: BigDecimal
): List<AmortizationRow> {
    if (amount <= BigDecimal.ZERO || term <= 0 || fixedPayment <= BigDecimal.ZERO) return emptyList()

    // 1. Algoritmo de Bisección (Binary Search) para encontrar la tasa de interés interna exacta
    // Buscamos una tasa 'r' (sin IVA) tal que al aplicar (Interés + IVA + Capital) el saldo llegue a 0.
    var low = 0.0
    var high = 1.0 // 100% de interés mensual como cota superior
    var monthlyRate = 0.0
    val targetError = 0.01 // Precisión deseada de 1 centavo
    
    for (i in 0..100) {
        monthlyRate = (low + high) / 2.0
        var tempBalance = amount.toDouble()
        
        for (m in 1..term) {
            val interest = tempBalance * monthlyRate
            val iva = interest * 0.16
            val principal = fixedPayment.toDouble() - interest - iva
            tempBalance -= principal
        }
        
        if (Math.abs(tempBalance) <= targetError) {
            break
        } else if (tempBalance > 0) {
            // Tasa es muy alta (genera mucho interés y se abona poco capital)
            high = monthlyRate
        } else {
            // Tasa es muy baja (se abona demasiado capital y el saldo queda negativo)
            low = monthlyRate
        }
    }
    
    // 2. Construir la tabla usando la tasa exacta calculada
    val finalMonthlyRate = BigDecimal(monthlyRate.toString()).setScale(10, RoundingMode.HALF_UP)
    
    val rows = mutableListOf<AmortizationRow>()
    var balance = amount
    
    for (month in 1..term) {
        val interest = balance.multiply(finalMonthlyRate).setScale(2, RoundingMode.HALF_UP)
        val iva = interest.multiply(BigDecimal("0.16")).setScale(2, RoundingMode.HALF_UP)
        
        var payment = fixedPayment
        var principal = payment.subtract(interest).subtract(iva)
        
        // Ajuste fino en el último mes para cuadrar a 0.00 exactos
        if (month == term) {
            principal = balance
            payment = principal.add(interest).add(iva).setScale(2, RoundingMode.HALF_UP)
        } else if (principal > balance) {
            principal = balance
            payment = principal.add(interest).add(iva).setScale(2, RoundingMode.HALF_UP)
        }
        
        balance = balance.subtract(principal).max(BigDecimal.ZERO)
        
        rows.add(
            AmortizationRow(
                month = month,
                payment = payment,
                interest = interest,
                iva = iva,
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

// AZUL PREMIUM - Más profundo y profesional (reemplazado por Zinc 900 / Slate 800)
val JuicyBlue = Color(0xFF18181B)
val JuicyBlueShadow = Color(0xFF27272A)


// VERDE ESMERALDA - Menos neón, más premium (reemplazado por Slate 600)
val JuicyGreen = Color(0xFF475569)
val JuicyGreenShadow = Color(0xFF334155)


// NARANJA CÁLIDO - Más sofisticado (reemplazado por Slate 500)
val JuicyOrange = Color(0xFF64748B)
val JuicyOrangeShadow = Color(0xFF475569)
val JuicyOrangeGlow = Color(0xFF94A3B8)

// ROJO CORAL - Más elegante (reemplazado por Rose/Red elegante plano)
val JuicyRed = Color(0xFFBE123C)
val JuicyRedShadow = Color(0xFF9F1239)
val JuicyRedGlow = Color(0xFFFDA4AF)

// PÚRPURA REFINADO - Más profundo (reemplazado por Slate)
val JuicyPurple = Color(0xFF334155)
val JuicyPurpleShadow = Color(0xFF1E293B)
val JuicyPurpleGlow = Color(0xFF64748B)

// COLORES SECUNDARIOS - También refinados
val JuicyCyan = Color(0xFF475569)
val JuicyPink = Color(0xFF334155)
val JuicyYellow = Color(0xFF64748B)
val JuicyLime = Color(0xFF475569)

// GRADIENTES REFINADOS
val GradientBlue = Brush.linearGradient(
    colors = listOf(Color(0xFF09090B), Color(0xFF27272A))
)
val GradientGreen = Brush.linearGradient(
    colors = listOf(Color(0xFF27272A), Color(0xFF18181B))
)
val GradientOrange = Brush.linearGradient(
    colors = listOf(Color(0xFF18181B), Color(0xFF09090B))
)
val GradientPurple = Brush.linearGradient(
    colors = listOf(Color(0xFF09090B), Color(0xFF18181B))
)
val GradientSunset = Brush.linearGradient(
    colors = listOf(Color(0xFF09090B), Color(0xFF27272A), Color(0xFF18181B))
)

// ===== PALETA MONOCROMÁTICA PREMIUM =====
val BgLight = Color(0xFFFAFAFA)       // Gris muy claro Zinc-50
val BgDark = Color(0xFF09090B)        // Negro Zinc-950

val SurfaceLight = Color(0xFFFFFFFF)  // Blanco puro
val SurfaceDark = Color(0xFF18181B)   // Gris Zinc-900

val TextDark = Color(0xFF09090B)      // Negro Zinc-950
val TextLight = Color(0xFFF4F4F5)     // Blanco suave Zinc-100

val BorderLight = Color(0xFFE4E4E7)   // Gris claro Zinc-200
val BorderDark = Color(0xFF27272A)    // Gris oscuro Zinc-800

// DORADO PREMIUM (acento principal de la app) -> Reemplazado por Champagne/Arena sutil
val GoldAccent = Color(0xFF8E8A7F)        // Champagne sutil
val GoldAccentLight = Color(0xFFB5A99B)   // Arena claro
val GoldAccentDark = Color(0xFF5F5B54)    // Champagne profundo

// COLORES DE ESTADO (funcionales, no identidad visual)
val SuccessGreen = Color(0xFF10B981)  // Esmeralda limpio
val WarningOrange = Color(0xFFF59E0B) // Ámbar limpio
val ErrorRed = Color(0xFFEF4444)      // Rojo limpio
val InfoBlue = Color(0xFF3B82F6)      // Azul limpio

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
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
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
 * Animated wrapper for debt items when they are added or removed
 */
@Composable
fun AnimatedDebtItem(
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + expandVertically(tween(300, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.95f),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)) + scaleOut(targetScale = 0.95f)
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
 * Custom Navigation Bar Item con animaciones suaves y haptic feedback (Segmented Style)
 */
@Composable
fun NavBarItem(
    icon: ImageVector,
    label: String, // Label mantenido para accesibilidad pero ya no visualizado
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val floatAnimSpec = remember { tween<Float>(250, easing = FastOutSlowInEasing) }
    
    val hapticManager = LocalHapticManager.current
    
    // Monocromático: ícono seleccionado es blanco (dark) o negro (light)
    // No se usa el parámetro 'color' original — el color ahora es el del texto del tema
    val selectedColor = MaterialTheme.colorScheme.onSurface
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    
    val animatedColor by animateColorAsState(
        targetValue = if (selected) selectedColor else unselectedColor,
        animationSpec = tween(250),
        label = "navItemColor"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 0.9f,
        animationSpec = floatAnimSpec,
        label = "navItemScale"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Sin ripple para mejor rendimiento visual en el slider
                onClick = {
                    if (selected) {
                        hapticManager?.light()
                    } else {
                        hapticManager?.medium()
                    }
                    onClick()
                }
            )
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = animatedColor,
            modifier = Modifier.size(28.dp)
        )
    }
}


// =================================================================================================
// 6. SERVICES (SOUND & TICKET)
// =================================================================================================
class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    var isSoundEnabled: Boolean = true

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
        if (!isSoundEnabled) return
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
    var isHapticEnabled: Boolean = true

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
        if (!isHapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(10, 50))
        }
    }

    /**
     * Medium haptic - Para clicks, toggles, acciones estándar
     */
    fun medium() {
        if (!isHapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, 100))
        }
    }

    /**
     * Heavy haptic - Para confirmaciones importantes, errores
     */
    fun heavy() {
        if (!isHapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(40, 200))
        }
    }

    /**
     * Success haptic - Patrón de doble pulse para acciones exitosas
     */
    fun success() {
        if (!isHapticEnabled) return
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
        if (!isHapticEnabled) return
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
        if (!isHapticEnabled) return
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
    val supervisor: String = ""
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
        "CONSUBANCO TRADICIONAL" to ProductData(
            mapOf(
                60 to BigDecimal("34.47786"),
                54 to BigDecimal("36.01578"),
                48 to BigDecimal("38.34620"),
                36 to BigDecimal("45.12133")
            ),
            RetentionRule(
                mapOf(60 to BigDecimal("30.95418")),
                BigDecimal("100000.0")
            ),
            ProductMeta(2.50, "39.03%", "31.50%"),
            BigDecimal("50000"),
            BigDecimal("1000"),
            mapOf(
                60 to BigDecimal("50000"),
                54 to BigDecimal("75000"),
                48 to BigDecimal("100000"),
                36 to BigDecimal("100000")
            )
        ),
        "CONSUBANCO ESPECIAL" to ProductData(
            mapOf(60 to BigDecimal("31.62827")),
            RetentionRule(emptyMap(), BigDecimal("9999999")),
            ProductMeta(2.50, "32.94%", "31.50%"),
            BigDecimal("100000"),
            BigDecimal("1000"),
            mapOf(60 to BigDecimal("100000"))
        ),
        "CONSUBANCO VIP" to ProductData(
            mapOf(60 to BigDecimal("30.95419")),
            RetentionRule(emptyMap(), BigDecimal("9999999")),
            ProductMeta(2.50, "39.03%", "31.50%"),
            BigDecimal("150000"),
            BigDecimal("1000"),
            mapOf(60 to BigDecimal("150000"))
        ),
        "CONSUPAGO TRADICIONAL" to ProductData(
            factorsTrad = mapOf(
                60 to BigDecimal("36.61922"),
                54 to BigDecimal("38.09840"),
                48 to BigDecimal("40.07602"),
                36 to BigDecimal("46.26983"),
                24 to BigDecimal("59.51904"),
                12 to BigDecimal("100.90633")
            ),
            retention = RetentionRule(
                 mapOf(60 to BigDecimal("33.25726")),
                 BigDecimal("50000.0")
            ),
            meta = ProductMeta(2.73, "43.62%", "36.42%"),
            minGlobal = BigDecimal("5000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(
                60 to BigDecimal("5000"), 54 to BigDecimal("5000"),
                48 to BigDecimal("10000"), 36 to BigDecimal("10000"),
                24 to BigDecimal("25000"), 12 to BigDecimal("25000")
            )
        ),
        "CONSUPAGO ESPECIAL" to ProductData(
            factorsTrad = mapOf(60 to BigDecimal("33.25726")),
            retention = RetentionRule(emptyMap(), BigDecimal("9999999")),
            meta = ProductMeta(2.73, "43.62%", "36.42%"),
            minGlobal = BigDecimal("50000"),
            minInc = BigDecimal("500"),
            minTermMap = mapOf(60 to BigDecimal("50000"))
        ),
        "OPCIPRES TRADICIONAL" to ProductData(
            mapOf(
                60 to BigDecimal("35.98563"),
                54 to BigDecimal("37.65908"),
                48 to BigDecimal("39.86519"),
                36 to BigDecimal("46.31088")
            ),
            RetentionRule(
                mapOf(60 to BigDecimal("32.22283")),
                BigDecimal("75000.0")
            ),
            ProductMeta(2.67, "42.26%", "34.21%"),
            BigDecimal("30000"),
            BigDecimal("750"),
            mapOf(
                60 to BigDecimal("30000"),
                54 to BigDecimal("45000"),
                48 to BigDecimal("50000"),
                36 to BigDecimal("70000")
            )
        ),
        "OPCIPRES ESPECIAL" to ProductData(
            mapOf(60 to BigDecimal("32.22283")),
            RetentionRule(emptyMap(), BigDecimal("9999999")),
            ProductMeta(2.67, "42.26%", "34.21%"),
            BigDecimal("75000"),
            BigDecimal("750"),
            mapOf(60 to BigDecimal("75000"))
        ),
        "MASNOMINA" to ProductData(
            factorsTrad = mapOf(
                60 to BigDecimal("36.77748"),
                54 to BigDecimal("36.01578"),
                48 to BigDecimal("40.18578"),
                36 to BigDecimal("46.37244"),
                24 to BigDecimal("59.57626")
            ),
            retention = RetentionRule(emptyMap(), BigDecimal("999999999.0")),
            meta = ProductMeta(2.73, "43.96%", "46.50%"),
            minGlobal = BigDecimal("10000"),
            minInc = BigDecimal("0"),
            minTermMap = mapOf(
                60 to BigDecimal("10000"),
                54 to BigDecimal("75000"),
                48 to BigDecimal("15000"),
                36 to BigDecimal("15000"),
                24 to BigDecimal("20000")
            )
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
        "CONSUBANCO TRADICIONAL" to BigDecimal("200000"),
        "OPCIPRES TRADICIONAL" to BigDecimal("125000"),
        "CONSUPAGO TRADICIONAL" to BigDecimal("100000"),
        "MASNOMINA" to BigDecimal("999999999")
    )
    
    // Montos mínimos para Tasa Preferencial - REFINANCIAMIENTO (CNCA)
    // Requiere además: 24 pagos mínimos realizados
    val MIN_RET_CNCA = mapOf(
        "CONSUBANCO TRADICIONAL" to BigDecimal("100000"),
        "OPCIPRES TRADICIONAL" to BigDecimal("75000"),
        "CONSUPAGO TRADICIONAL" to BigDecimal("50000"),
        "MASNOMINA" to BigDecimal("999999999")
    )
    
    // Factores Globales SIPRE para Liquidación de Deuda
    val SIPRE_FACTORS = mapOf(
        "CONSUBANCO TRADICIONAL" to BigDecimal("1.857251"),
        "OPCIPRES TRADICIONAL" to BigDecimal("1.933371"),
        "CONSUPAGO TRADICIONAL" to BigDecimal("1.9954353")
    )

    // Reglas de Validación de Plazo y Monto Mínimo (2026)
    fun isValidTermAndAmount(product: String, term: Int, amount: BigDecimal): Boolean {
        if (amount <= BigDecimal.ZERO) return false
        val config = FinancialConfig.PRODUCTS[product] ?: return false
        
        // Ensure term is allowed
        if (!config.factorsTrad.containsKey(term)) return false
        
        // Ensure minimum amount
        val minAmt = config.minTermMap[term] ?: config.minGlobal
        return amount >= minAmt
    }

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
        
        val totalPaymentsOriginales = debts.sumOf { it.payment.toBD() }
        val increaseDescuento = offer.monthlyPayment.subtract(totalPaymentsOriginales)
        
        // Validación de liquidez positiva (obligatoria para todos)
        if (remnant <= BigDecimal.ZERO) return Pair(false, listOf("Rechazo: Sin Liquidez"))
        if (debts.isEmpty()) return Pair(true, listOf("Crédito Nuevo"))

        // MASNOMINA: Sin restricciones, solo requiere liquidez positiva
        if (prodKey.startsWith("MASNOMINA")) {
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
            when {
                prodKey.startsWith("CONSUPAGO") -> v1Ok = true  // Sin restricción de tasa
                prodKey.startsWith("OPCIPRES") -> if (dRate <= 2.57) v1Ok = true
                prodKey.startsWith("CONSUBANCO") -> if (dRate <= 2.40) v1Ok = true
            }
            if (v1Ok) cVal1++

            // ========== VALIDACIÓN 2: PAGOS ACUMULADOS Y TASA DE REFERENCIA ==========
            // Requiere 16 pagos aplicados independientemente del plazo original
            var v2Ok = false
            when {
                prodKey.startsWith("CONSUPAGO") -> v2Ok = true  // Sin restricción de tasa ni pagos
                prodKey.startsWith("OPCIPRES") -> if (dPaid >= 16 && dRate <= 2.67) v2Ok = true
                prodKey.startsWith("CONSUBANCO") -> if (dPaid >= 16 && dRate <= 2.57) v2Ok = true
            }
            if (v2Ok) cVal2++

            // ========== VALIDACIÓN 3: INCREMENTO DE DESCUENTO ==========
            var v3Ok = false
            when {
                prodKey.startsWith("CONSUPAGO") -> v3Ok = true  // Sin restricción
                prodKey.startsWith("OPCIPRES") -> if (increaseDescuento >= BigDecimal("750") && dRate <= 2.67) v3Ok = true
                prodKey.startsWith("CONSUBANCO") -> if (increaseDescuento >= BigDecimal("1000") && dRate <= 2.57) v3Ok = true
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
// =================================================================================================
data class CalculationDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val debts: List<Debt>, // Snapshot
    val capacity: String,
    val type: String = "Refinanciamiento"
)

data class CalculatorUiState(
    val selectedTab: Int = 0,
    val debts: List<Debt> = listOf(Debt()),
    val history: List<CalculationDraft> = emptyList(), // Historial
    val showHistory: Boolean = false, // Modal Historial
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
    val isLoading: Boolean = false,
    val useRetention: Boolean = false,
    val mandatoryConfigNeeded: Boolean = false,
    
    // Ticket Generation State
    val isGeneratingTicket: Boolean = false,
    
    // New Feature: Shareable Ticket
    val showTicketPreview: Boolean = false,
    val activeTicketData: TicketData? = null,
    
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

    // Filtro de resultados premium
    val showPremiumOffers: Boolean = true,

    // Ajustes globales de sonido y vibración
    val isSoundEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true
)



// =================================================================================================
// 11. VIEWMODEL LOGIC
// =================================================================================================
class CalculatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState = _uiState.asStateFlow()
    private val gson = Gson()
    private var saveJob: Job? = null

    fun loadData(ctx: Context) {
        
        val prefs = ctx.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE)

        val p = UserProfile(
            prefs.getString("name", "") ?: "",
            prefs.getString("username", "") ?: "",
            prefs.getString("password", "") ?: "",
            prefs.getString("supervisor", "") ?: ""
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

        val isSound = prefs.getBoolean("isSoundEnabled", true)
        val isHaptic = prefs.getBoolean("isHapticEnabled", true)

        _uiState.update {
            it.copy(
                userProfile = p,
                debts = debtsList,
                clientData = client,
                capacityCNCA = prefs.getString("capCNCA", "") ?: "",
                
                // --- RECUPERAR DATOS SEGURO ---
                insAge = prefs.getString("insAge", "") ?: "",
                insAmount = prefs.getString("insAmount", "") ?: "",
                insTerm = prefs.getInt("insTerm", 60),
                insProvider = prefs.getString("insProvider", "CONSUBANCO") ?: "CONSUBANCO",
                insPensionType = prefs.getString("insPensionType", "Invalidez") ?: "Invalidez",
                insIsNewClient = prefs.getBoolean("insIsNew", false),
                insAntiquity = prefs.getString("insAntiquity", "") ?: "",
                
                isSoundEnabled = isSound,
                isHapticEnabled = isHaptic,

                
                // Verificar si falta configuración obligatoria
                mandatoryConfigNeeded = p.name.isBlank() || p.username.isBlank() || p.password.isBlank() || p.supervisor.isBlank()
            )
        }
    }

    fun saveData(ctx: Context) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(1000) // Debounce de 1 segundo para no saturar disco
            val s = _uiState.value
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
                
                .putBoolean("isSoundEnabled", s.isSoundEnabled)
                .putBoolean("isHapticEnabled", s.isHapticEnabled)

                .apply()
        }
    }

    fun saveProfile(ctx: Context, p: UserProfile) {
        val prefs = ctx.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("name", p.name).putString("username", p.username)
            .putString("password", p.password).putString("supervisor", p.supervisor).apply()
        _uiState.update {
            it.copy(
                userProfile = p,
                showConfigDialog = false,
                mandatoryConfigNeeded = false
            )
        }
    }

    fun toggleSound(enabled: Boolean) {
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    fun toggleHaptic(enabled: Boolean) {
        _uiState.update { it.copy(isHapticEnabled = enabled) }
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

    fun toggleRetention(enabled: Boolean) {
        _uiState.update { it.copy(useRetention = enabled) }
    }

    fun calculateRefinance(ctx: Context, soundManager: SoundManager) {
        saveToHistory("Refinanciamiento")
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
                val state = _uiState.value
                val cap = state.capacityCNCA.toBD()
                
                if (state.clientData.nss.isNotEmpty()) {
                    val nss = state.clientData.nss
                    if (nss.length != 11) {
                    }
                }

                val debtBalance = state.debts.sumOf { it.balance.toBD() }
                val debts = state.debts.filter { it.balance.toBD() > BD_ZERO }
                val results = mutableListOf<LoanOffer>()
                
                
                val totalPagosDeudas = state.debts.sumOf { it.payment.toBD() }
                val capacidadTotal = totalPagosDeudas.add(cap)

                if (capacidadTotal > BD_ZERO && debts.isNotEmpty()) {
                    // Detectar plazos originales de las deudas
                    val originalTerms = debts.mapNotNull { it.originalTerm.toIntOrNull() }.distinct()
                    
                    // Determinar qué plazos calcular según las reglas de extensión de plazo
                    val termsToCalculate = mutableSetOf<Int>()
                    originalTerms.forEach { origTerm ->
                        when (origTerm) {
                            12         -> termsToCalculate.addAll(listOf(36, 48, 54, 60))
                            24         -> termsToCalculate.addAll(listOf(48, 54, 60))
                            36, 48     -> termsToCalculate.addAll(listOf(54, 60))
                            54         -> termsToCalculate.add(60)
                            else       -> termsToCalculate.add(60)
                        }
                    }
                    if (termsToCalculate.isEmpty()) termsToCalculate.add(60)

                    // CNCA: Solo aplica a products TRADICIONALES
                    // ESPECIAL y VIP son solo para créditos nuevos
                    val cncaProducts = FinancialConfig.PRODUCTS.filter { (k, _) ->
                        !k.endsWith("ESPECIAL") && !k.endsWith("VIP")
                    }

                    cncaProducts.forEach { (prodKey, config) ->
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
                                        if (viable && offerRet.amount >= debtBalance && FinancialEngine.isValidTermAndAmount(prodKey, term, offerRet.amount)) {
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
                                        if (viable && offer.amount >= debtBalance && FinancialEngine.isValidTermAndAmount(prodKey, term, offer.amount)) {
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
                soundManager.play("cash")
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
                val state = _uiState.value
                val inputVal = state.newCreditInput.toBD()
                val results = mutableListOf<LoanOffer>()
                if (inputVal > BD_ZERO) {
                    FinancialConfig.PRODUCTS.forEach { (prodKey, config) ->
                        val isPremiumVariant = prodKey.endsWith("ESPECIAL") || prodKey.endsWith("VIP")

                        if (isPremiumVariant) {
                            // ESPECIAL y VIP: solo 60 meses, sin retención, sin fallback
                            val step = if (state.isByAmount) BigDecimal("100") else BigDecimal("500")
                            val calc = FinancialEngine.calculate(prodKey, inputVal, 60, state.isByAmount, false, step)
                            if (calc != null) {
                                val (res, _) = calc
                                val minRequired = config.minTermMap[60] ?: config.minGlobal
                                if (res.amount >= minRequired && FinancialEngine.isValidTermAndAmount(prodKey, 60, res.amount)) {
                                    results.add(
                                        LoanOffer(
                                            prodKey, res.amount, res.payment, 60,
                                            mainValue = res.amount,
                                            mainLabel = "MONTO PRÉSTAMO",
                                            cat = config.meta.catTrad,
                                            tipoTasa = prodKey.substringAfterLast(" ")
                                        )
                                    )
                                }
                            }
                        } else {
                            // MODO TRADICIONAL: calcular todos los plazos disponibles
                            config.factorsTrad.keys.forEach { term ->
                                val step = if (state.isByAmount) BigDecimal("100") else BigDecimal("500")
                                val calcTrad = FinancialEngine.calculate(prodKey, inputVal, term, state.isByAmount, false, step)
                                if (calcTrad != null) {
                                    val (res, tipo) = calcTrad
                                    val minRequired = config.minTermMap[term] ?: config.minGlobal
                                    if (res.amount >= minRequired && res.amount >= config.minGlobal && FinancialEngine.isValidTermAndAmount(prodKey, term, res.amount)) {
                                        results.add(LoanOffer(prodKey, res.amount, res.payment, term,
                                            mainValue = res.amount, mainLabel = "MONTO PRÉSTAMO",
                                            cat = config.meta.catTrad, tipoTasa = tipo))
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
                soundManager.play("cash")
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
                    val baseProvider = provider.substringBefore(" ")
                    val factors = FinancialConfig.INSURANCE_FACTORS[baseProvider]
                    
                    // Caso especial: OPCIPRES Retención 60m usa factor especial (key 999)
                    val termKey = if (provider.startsWith("OPCIPRES") && term == 60 && (provider.endsWith("ESPECIAL") || provider.endsWith("VIP"))) {
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
    // HISTORY LOGIC
    // =================================================================================================
    fun saveToHistory(type: String) {
        val state = _uiState.value
        // Solo guardar si hay datos relevantes
        if (state.debts.isEmpty() && state.capacityCNCA.isBlank()) return
        
        val draft = CalculationDraft(
            debts = state.debts.map { it.copy() }, // Deep copy
            capacity = state.capacityCNCA,
            type = type
        )
        // Limitar historial a 10 items
        val newHistory = (listOf(draft) + state.history).take(10)
        _uiState.update { it.copy(history = newHistory) }
    }
    


    fun deleteDraft(draft: CalculationDraft) {
        _uiState.update { 
            it.copy(history = it.history - draft) 
        }
    }

    fun clearHistory() {
        _uiState.update { 
            it.copy(history = emptyList()) 
        }
    }
    
    fun restoreHistory(draft: CalculationDraft) {
        _uiState.update { 
            it.copy(
                debts = draft.debts,
                capacityCNCA = draft.capacity,
                showHistory = false,
                offersCNCA = emptyList(),
                selectedTab = if(draft.type == "Refinanciamiento") 0 else 1
            )
        }
    }
    
    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }

    // =================================================================================================
    // TICKET SHARING LOGIC
    // =================================================================================================
    fun togglePremiumOffers() {
        _uiState.update { it.copy(showPremiumOffers = !it.showPremiumOffers) }
    }

    fun prepareTicketSharing(context: Context, offer: LoanOffer) {
        val state = _uiState.value
        val debts = state.debts
        val totalCurrentPayment = debts.sumOf { it.payment.toBD() }
        
        // Formatear datos
        val savings = if (totalCurrentPayment > BigDecimal.ZERO && state.selectedTab == 0) {
            val s = totalCurrentPayment.subtract(offer.monthlyPayment).max(BigDecimal.ZERO)
            "$${s.toCurrency()}"
        } else {
             "N/A"
        }
        
        val prevPaymentStr = if (totalCurrentPayment > BigDecimal.ZERO && state.selectedTab == 0) {
            "${debts.size} Deudas"
        } else {
            "Crédito Nuevo"
        }

        val data = TicketData(
            clientName = state.clientData.name.ifBlank { "Cliente Preferente" },
            date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
            product = offer.product,
            amount = "$${offer.mainValue.toCurrency()}",
            payment = "$${offer.monthlyPayment.toCurrency()}",
            term = offer.term,
            previousPayment = prevPaymentStr,
            savings = savings
        )
        
        _uiState.update { it.copy(showTicketPreview = true, activeTicketData = data) }
    }
    
    fun dismissTicketPreview() {
        _uiState.update { it.copy(showTicketPreview = false, activeTicketData = null) }
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
     */
    fun showAmortizationFor(offer: LoanOffer) {
        val table = calculateAmortizationTable(
            amount = offer.amount,
            term = offer.term,
            fixedPayment = offer.monthlyPayment
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
    val ctx = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager(ctx) }
    LaunchedEffect(state.isSoundEnabled) {
        soundManager.isSoundEnabled = state.isSoundEnabled
    }

    var showMenu by remember { mutableStateOf(false) }

    val navController = rememberNavController()

    // Sincronización Pager eliminada en favor de NavController
    // Efecto decorativo para animaciones según cambios en la ruta actual
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "refinance"

    // Helper para mapear ruta a índice de Tab (solo para animar el indicador inferior)
    val currentTabIndex = when (currentRoute) {
        "refinance" -> 0
        "new_credit" -> 1
        "insurance" -> 2
        "academy" -> 3
        else -> 0
    }
    
    // Si cambia el index guiado por Navigation, informamos al viewModel (Para los layouts heredados que dependen de ello)
    LaunchedEffect(currentTabIndex) {
        if (state.selectedTab != currentTabIndex) {
            viewModel.setTab(currentTabIndex)
        }
    }

    DisposableEffect(Unit) { onDispose { soundManager.release() } }
    LaunchedEffect(Unit) { viewModel.loadData(ctx); delay(50) }
    LaunchedEffect(state) { viewModel.saveData(ctx) }

    // ==== FONDO MONOCROMÁTICO PREMIUM ====
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
            Scaffold(
                containerColor = Color.Transparent, // Deja ver el gradiente
                contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0), // Sin espacio reservado → la isla flota genuinamente
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "ConsuCalc",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        actions = {
                            // Avatar button - Inicial del usuario en círculo premium
                            val firstName = state.userProfile.name.trim().split(" ").firstOrNull { it.isNotEmpty() } ?: "U"
                            val initial = firstName.take(1).uppercase()
                            
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDark) GoldAccent.copy(alpha = 0.15f)
                                            else Color(0xFF1C1C1E).copy(alpha = 0.08f)
                                        )
                                        .border(
                                            1.5.dp,
                                            if (isDark) GoldAccent.copy(alpha = 0.4f)
                                            else Color(0xFF1C1C1E).copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        initial,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) GoldAccent else Color(0xFF1C1C1E)
                                    )
                                }
                            }
                            Spacer(Modifier.width(4.dp))
                        }
                    )

                    // ========== PREMIUM PROFILE BOTTOM SHEET ==========
                    if (showMenu) {
                        ModalBottomSheet(
                            onDismissRequest = { showMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            dragHandle = {
                                // Drag handle sutil
                                Box(
                                    modifier = Modifier
                                        .padding(top = 12.dp, bottom = 4.dp)
                                        .width(36.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                                )
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 32.dp)
                            ) {
                                // ===== USER HEADER =====
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar grande
                                    val userFirstName = state.userProfile.name.trim().split(" ").firstOrNull { it.isNotEmpty() } ?: "U"
                                    val userInitial = userFirstName.take(1).uppercase()
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDark) GoldAccent.copy(alpha = 0.12f)
                                                else Color(0xFF1C1C1E).copy(alpha = 0.06f)
                                            )
                                            .border(
                                                2.dp,
                                                if (isDark) GoldAccent.copy(alpha = 0.3f)
                                                else Color(0xFF1C1C1E).copy(alpha = 0.1f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            userInitial,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) GoldAccent else Color(0xFF1C1C1E)
                                        )
                                    }
                                    
                                    Spacer(Modifier.width(14.dp))
                                    
                                    Column {
                                        Text(
                                            userFirstName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "Asesor Financiero",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                                Spacer(Modifier.height(8.dp))
                                
                                // ===== MENU ITEMS =====
                                // Historial
                                Surface(
                                    onClick = {
                                        showMenu = false
                                        soundManager.play("pop")
                                        viewModel.toggleHistory()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.History,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                "Historial",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                "Cotizaciones guardadas",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                            )
                                        }
                                        Icon(
                                            Icons.Rounded.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                // Configuración
                                Surface(
                                    onClick = {
                                        showMenu = false
                                        viewModel.toggleConfigDialog(true)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.Settings,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                "Configuración",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                "Datos de usuario y convenio",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                            )
                                        }
                                        Icon(
                                            Icons.Rounded.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                                Spacer(Modifier.height(8.dp))
                                
                                // ===== THEME TOGGLE =====
                                Surface(
                                    onClick = {
                                        onToggleTheme()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isDark) Color(0xFF2C2C2E)
                                                    else Color(0xFFE5E5EA)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (isDark) GoldAccent else Color(0xFFFF9500)
                                            )
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Text(
                                            if (isDark) "Modo Oscuro" else "Modo Claro",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        // Toggle visual
                                        Switch(
                                            checked = isDark,
                                            onCheckedChange = { onToggleTheme() },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = GoldAccent,
                                                checkedTrackColor = GoldAccent.copy(alpha = 0.25f),
                                                uncheckedThumbColor = Color(0xFF8E8E93),
                                                uncheckedTrackColor = Color(0xFF8E8E93).copy(alpha = 0.2f),
                                                uncheckedBorderColor = Color.Transparent,
                                                checkedBorderColor = Color.Transparent
                                            )
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                
                                // ===== SONIDOS TOGGLE =====
                                Surface(
                                    onClick = {
                                        viewModel.toggleSound(!state.isSoundEnabled)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isDark) Color(0xFF2C2C2E)
                                                    else Color(0xFFE5E5EA)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (state.isSoundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (state.isSoundEnabled) (if (isDark) GoldAccent else Color(0xFF34C759)) else Color(0xFF8E8E93)
                                            )
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Text(
                                            "Efectos de Sonido",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        Switch(
                                            checked = state.isSoundEnabled,
                                            onCheckedChange = { viewModel.toggleSound(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = GoldAccent,
                                                checkedTrackColor = GoldAccent.copy(alpha = 0.25f),
                                                uncheckedThumbColor = Color(0xFF8E8E93),
                                                uncheckedTrackColor = Color(0xFF8E8E93).copy(alpha = 0.2f),
                                                uncheckedBorderColor = Color.Transparent,
                                                checkedBorderColor = Color.Transparent
                                            )
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                
                                // ===== VIBRACION TOGGLE =====
                                Surface(
                                    onClick = {
                                        viewModel.toggleHaptic(!state.isHapticEnabled)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isDark) Color(0xFF2C2C2E)
                                                    else Color(0xFFE5E5EA)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.Vibration,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (state.isHapticEnabled) (if (isDark) GoldAccent else Color(0xFF5856D6)) else Color(0xFF8E8E93)
                                            )
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Text(
                                            "Vibración Háptica",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        Switch(
                                            checked = state.isHapticEnabled,
                                            onCheckedChange = { viewModel.toggleHaptic(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = GoldAccent,
                                                checkedTrackColor = GoldAccent.copy(alpha = 0.25f),
                                                uncheckedThumbColor = Color(0xFF8E8E93),
                                                uncheckedTrackColor = Color(0xFF8E8E93).copy(alpha = 0.2f),
                                                uncheckedBorderColor = Color.Transparent,
                                                checkedBorderColor = Color.Transparent
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // ============================================
                },
                bottomBar = {
                    // ========== CUSTOM NAVIGATION BAR TIPO SEGMENTED CONTROL ==========
                    val isDarkTheme = isDark
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 32.dp, vertical = 20.dp)
                    ) {
                        Surface(
                            // En modo claro usamos un gris claro (background del segmented bar), en oscuro casi negro
                            color = if (isDarkTheme) Color(0xFF151515) else Color(0xFFF2F2F7),
                            shadowElevation = 0.dp,
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (isDarkTheme) Color(0xFF27272A) else Color(0xFFE4E4E7),
                                    shape = RoundedCornerShape(32.dp)
                                )
                        ) {
                            androidx.compose.foundation.layout.BoxWithConstraints(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val islandWidth = maxWidth

                                // Indicador deslizante animado elástico (spring)
                                val indicatorOffset by animateFloatAsState(
                                    targetValue = currentTabIndex.toFloat(),
                                    animationSpec = androidx.compose.animation.core.spring(
                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                    ),
                                    label = "indicatorOffset"
                                )

                                val tabWidth = islandWidth / 4

                                // === Pastilla (Pill) indicadora detrás de los íconos ===
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .offset(x = tabWidth * indicatorOffset)
                                        .width(tabWidth)
                                        .height(64.dp)
                                        .padding(6.dp)
                                ) {
                                    val indicatorColor = if (isDarkTheme) Color(0xFF27272A) else Color(0xFFFFFFFF)
                                    val indicatorBorder = if (isDarkTheme) Color(0xFF3F3F46) else Color(0xFFE4E4E7)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(30.dp))
                                            .background(indicatorColor)
                                            .border(1.dp, indicatorBorder, RoundedCornerShape(30.dp))
                                    )
                                }

                                val onTabClick: (String) -> Unit = { route ->
                                    soundManager.play("pop")
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }

                                // Items de navegación centrados y expandidos
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    NavBarItem(
                                        icon = if (currentTabIndex == 0) Icons.Rounded.Loop else Icons.Outlined.Loop,
                                        label = "Refinanciar",
                                        selected = currentTabIndex == 0,
                                        color = Color.White, // Mono: ya no importa, se ignora internamente
                                        onClick = { onTabClick("refinance") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    NavBarItem(
                                        icon = if (currentTabIndex == 1) Icons.Rounded.AttachMoney else Icons.Outlined.AttachMoney,
                                        label = "Nuevo",
                                        selected = currentTabIndex == 1,
                                        color = Color.White,
                                        onClick = { onTabClick("new_credit") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    NavBarItem(
                                        icon = Icons.Outlined.Savings,
                                        label = "Seguro",
                                        selected = currentTabIndex == 2,
                                        color = Color.White,
                                        onClick = { onTabClick("insurance") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    NavBarItem(
                                        icon = if (currentTabIndex == 3) Icons.Rounded.School else Icons.Outlined.School,
                                        label = "Academia",
                                        selected = currentTabIndex == 3,
                                        color = Color.White,
                                        onClick = { onTabClick("academy") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                // ========== NAV HOST PARA ENRUTAMIENTO ==========

                
                Box(
                    modifier = Modifier
                        // Solo usar el padding superior (del TopAppBar)
                        // El inferior se maneja manualmente para que no quede bloque gris
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = 0.dp // El contenido se extiende hasta abajo; la isla flota encima
                        )
                        .fillMaxSize()
                ) {
                    // Helper para saber el índice de una ruta
                    fun routeToIndex(route: String?) = when (route) {
                        "refinance"  -> 0
                        "new_credit" -> 1
                        "insurance"  -> 2
                        "academy"    -> 3
                        else         -> 0
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "refinance",
                        enterTransition = {
                            val from  = routeToIndex(initialState.destination.route)
                            val to    = routeToIndex(targetState.destination.route)
                            val dir   = if (to >= from)
                                AnimatedContentTransitionScope.SlideDirection.Left
                            else
                                AnimatedContentTransitionScope.SlideDirection.Right
                            slideIntoContainer(towards = dir, animationSpec = tween(380, easing = FastOutSlowInEasing)) +
                                fadeIn(tween(300))
                        },
                        exitTransition = {
                            val from  = routeToIndex(initialState.destination.route)
                            val to    = routeToIndex(targetState.destination.route)
                            val dir   = if (to >= from)
                                AnimatedContentTransitionScope.SlideDirection.Left
                            else
                                AnimatedContentTransitionScope.SlideDirection.Right
                            slideOutOfContainer(towards = dir, animationSpec = tween(380, easing = FastOutSlowInEasing)) +
                                fadeOut(tween(300))
                        },
                        // Pop usa la misma lógica (al presionar atrás también compara índices)
                        popEnterTransition = {
                            val from  = routeToIndex(initialState.destination.route)
                            val to    = routeToIndex(targetState.destination.route)
                            val dir   = if (to >= from)
                                AnimatedContentTransitionScope.SlideDirection.Left
                            else
                                AnimatedContentTransitionScope.SlideDirection.Right
                            slideIntoContainer(towards = dir, animationSpec = tween(380, easing = FastOutSlowInEasing)) +
                                fadeIn(tween(300))
                        },
                        popExitTransition = {
                            val from  = routeToIndex(initialState.destination.route)
                            val to    = routeToIndex(targetState.destination.route)
                            val dir   = if (to >= from)
                                AnimatedContentTransitionScope.SlideDirection.Left
                            else
                                AnimatedContentTransitionScope.SlideDirection.Right
                            slideOutOfContainer(towards = dir, animationSpec = tween(380, easing = FastOutSlowInEasing)) +
                                fadeOut(tween(300))
                        }
                    ) {
                        composable("refinance") {
                            AnimatedTabContent(tabKey = 0) {
                                RefinanceScreen(viewModel, state, soundManager, ctx)
                            }
                        }
                        composable("new_credit") {
                            AnimatedTabContent(tabKey = 1) {
                                NewCreditScreen(viewModel, state, soundManager, ctx)
                            }
                        }
                        composable("insurance") {
                            AnimatedTabContent(tabKey = 2) {
                                InsuranceScreen(viewModel, state, soundManager, ctx)
                            }
                        }
                        composable("academy") {
                            AnimatedTabContent(tabKey = 3) {
                                AcademyScreen(soundManager = soundManager)
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
            
            // --- NUEVO: TICKET PREVIEW DIALOG ---
            if (state.showTicketPreview && state.activeTicketData != null) {
                Dialog(onDismissRequest = { viewModel.dismissTicketPreview() }) {
                     Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                     ) {
                         DigitalTicket(
                             data = state.activeTicketData!!,
                             modifier = Modifier.padding(16.dp)
                         )
                         
                         Spacer(Modifier.height(16.dp))
                         
                         JuicyButton(
                             text = "CERRAR",
                             color = Color.White,
                             shadowColor = Color.Gray,
                             textColor = Color.Black,
                             height = 48.dp, 
                             soundManager = soundManager,
                             onClick = { viewModel.dismissTicketPreview() }
                         )
                     }
                }
            }

            // HISTORIAL MODAL
            if (state.showHistory) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleHistory() },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    HistorySheet(
                        history = state.history,
                        onRestore = { draft -> 
                             viewModel.restoreHistory(draft)
                             soundManager.play("magic")
                        },
                        onClear = { 
                            viewModel.clearHistory()
                            soundManager.play("delete")
                        },
                        onDelete = { draft ->
                            viewModel.deleteDraft(draft)
                             soundManager.play("delete")
                        }
                    )
                }
            }

            if (state.mandatoryConfigNeeded) {
                MandatoryConfigScreen(viewModel, ctx, soundManager)
            } else {
                if (state.showConfigDialog) {
                    ConfigDialog(viewModel, ctx, soundManager)
                }


                
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

            if (state.showSuccessAnim) SuccessOverlay { viewModel.hideSuccess() }
        }
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
// =================================================================================================
// 13. SCREENS
// =================================================================================================
@Composable
fun SummaryItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color = Color.White,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = alignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (alignment == Alignment.Start) {
                Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            if (alignment == Alignment.End) {
                Spacer(Modifier.width(6.dp))
                Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
            }
        }
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
fun RefinanceScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    soundManager: SoundManager,
    ctx: Context
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val totalDebt = state.debts.sumOf { it.balance.toBD() }
    val totalPayment = state.debts.sumOf { it.payment.toBD() }
    val listState = rememberLazyListState()
    var showLiquidationModal by remember { mutableStateOf(false) }

    // Auto-scroll al agregar deuda
    val debtCount = state.debts.size
    LaunchedEffect(debtCount) {
        if (debtCount > 1) {
            delay(300)
            listState.animateScrollToItem(1) // Scroll a la card de deudas
        }
    }

    // Animated counters para el header
    var animatedDebt by remember { mutableStateOf(0f) }
    var animatedPayment by remember { mutableStateOf(0f) }
    LaunchedEffect(totalDebt) {
        animate(
            initialValue = animatedDebt,
            targetValue = totalDebt.toFloat(),
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ) { value, _ -> animatedDebt = value }
    }
    LaunchedEffect(totalPayment) {
        animate(
            initialValue = animatedPayment,
            targetValue = totalPayment.toFloat(),
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ) { value, _ -> animatedPayment = value }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ========== HEADER TEMÁTICO ==========
        item {
            val bestOffer = state.offersCNCA.find { it.isBest }
            val potentialSaving = if (bestOffer != null) {
                totalPayment.subtract(bestOffer.monthlyPayment).max(BigDecimal.ZERO)
            } else BD_ZERO
            
            val isSavingFound = potentialSaving > BD_ZERO
            
            // Efecto Glow animado si hay ahorro
            val infiniteTransition = rememberInfiniteTransition(label = "glowHeader")
            val glowIntensity by infiniteTransition.animateFloat(
                initialValue = if (isSavingFound) 0.15f else 0f,
                targetValue = if (isSavingFound) 0.5f else 0f,
                animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                label = "glowIntensity"
            )

            JuicyGradientCard(
                brush = if (isDark) {
                    Brush.horizontalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF2C2C2E)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF3A3A3C)))
                },
                shadowColor = if (isSavingFound) GoldAccent.copy(alpha = 0.5f) else Color(0xFF0A0A0A),
                modifier = Modifier.graphicsLayer {
                    if (isSavingFound) {
                        translationY = -2f * glowIntensity
                    }
                }
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Loop,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp).graphicsLayer {
                                    // Rotación lenta si está cargando o hay ahorro
                                    if (state.isLoading) {
                                        rotationZ += 5f
                                    }
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "REFINANCIAMIENTO",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "Consolidación de deudas",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        
                        // Badge de ahorro dinámico
                        AnimatedVisibility(
                            visible = isSavingFound,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Surface(
                                color = GoldAccent,
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Savings, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "AHORRO!",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // Resumen inteligente
                    AnimatedVisibility(
                        visible = (state.debts.count { it.balance.toBD() > BD_ZERO } > 1 && totalDebt > BigDecimal.ZERO) || isSavingFound,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                            Spacer(Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SummaryItem(
                                    label = "DEUDA TOTAL",
                                    value = "$${String.format("%,.0f", animatedDebt)}",
                                    icon = Icons.Rounded.AccountBalance
                                )
                                
                                if (isSavingFound) {
                                    SummaryItem(
                                        label = "AHORRO MENSUAL",
                                        value = "$${potentialSaving.toCurrency()}",
                                        icon = Icons.Rounded.TrendingDown,
                                        color = JuicyGreen,
                                        alignment = Alignment.End
                                    )
                                } else {
                                    SummaryItem(
                                        label = "MENSUALIDAD",
                                        value = "$${String.format("%,.0f", animatedPayment)}",
                                        icon = Icons.Rounded.AttachMoney,
                                        alignment = Alignment.End
                                    )
                                }
                            }
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
                        "DEUDAS A LIQUIDAR",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(16.dp))

                    state.debts.forEachIndexed { index, debt ->
                        val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                soundManager.play("pop")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.removeDebtRow(debt)
                                true
                            } else false
                        })
                        
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300)) + slideInVertically(tween(300), initialOffsetY = { it / 2 }),
                            exit = fadeOut(tween(300)) + slideOutHorizontally(tween(300), targetOffsetX = { -it })
                        ) {
                            AnimatedDebtItem {
                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        Box(
                                            Modifier.fillMaxSize().padding(bottom = 8.dp)
                                                .clip(RoundedCornerShape(16.dp)).background(JuicyRed),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                Icons.Rounded.Delete,
                                                null,
                                                tint = Color.White,
                                                modifier = Modifier.padding(end = 24.dp)
                                            )
                                        }
                                    }
                                ) {
                                    // Rediseño Minimalista y Premium
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                        ),
                                        shadowElevation = 1.dp
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            // ── Header: Minimalista y Elegante ──
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Indicador numérico sutil
                                                    Text(
                                                        text = String.format("%02d", index + 1),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Black,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                    )
                                                    Spacer(Modifier.width(12.dp))
                                                    Text(
                                                        "Detalle de Deuda",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        letterSpacing = (-0.5).sp
                                                    )
                                                }
                                                // Botón X muy discreto
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                        .clickable {
                                                            soundManager.play("pop")
                                                            viewModel.removeDebtRow(debt)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Rounded.Close,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            // ── Selector de producto minimalista ──
                                            DropdownCompact(
                                                label = "COTIZADOR",
                                                options = FinancialConfig.PRODUCTS.keys.toList(),
                                                selectedValue = debt.product,
                                                modifier = Modifier.fillMaxWidth(),
                                                soundManager = soundManager
                                            ) { viewModel.updateDebt(debt.id, debt.copy(product = it)) }

                                            Spacer(Modifier.height(16.dp))

                                            // ── Fila 1: Montos Principales ──
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                CompactInput(
                                                    value = debt.balance,
                                                    label = "ADEUDA",
                                                    modifier = Modifier.weight(1f),
                                                    inputType = KeyboardType.Decimal,
                                                    isCurrency = true
                                                ) { viewModel.updateDebt(debt.id, debt.copy(balance = it)) }
                                                CompactInput(
                                                    value = debt.payment,
                                                    label = "PAGO MENSUAL",
                                                    modifier = Modifier.weight(1f),
                                                    inputType = KeyboardType.Decimal,
                                                    isCurrency = true
                                                ) { viewModel.updateDebt(debt.id, debt.copy(payment = it)) }
                                            }

                                            Spacer(Modifier.height(16.dp))

                                            // ── Fila 2: Condiciones ──
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                CompactInput(
                                                    value = debt.rate,
                                                    label = "TASA %",
                                                    modifier = Modifier.weight(1f),
                                                    inputType = KeyboardType.Decimal
                                                ) { viewModel.updateDebt(debt.id, debt.copy(rate = it)) }
                                                DropdownCompact(
                                                    label = "PLAZO",
                                                    options = FinancialConfig.STANDARD_TERMS,
                                                    selectedValue = debt.originalTerm,
                                                    modifier = Modifier.weight(1f),
                                                    soundManager = soundManager
                                                ) { viewModel.updateDebt(debt.id, debt.copy(originalTerm = it)) }
                                                CompactInput(
                                                    value = debt.paymentsMade,
                                                    label = "PAGADOS",
                                                    modifier = Modifier.weight(1f),
                                                    inputType = KeyboardType.Number
                                                ) { viewModel.updateDebt(debt.id, debt.copy(paymentsMade = it)) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (index < state.debts.size - 1) {
                            Spacer(Modifier.height(16.dp))
                        } else {
                            Spacer(Modifier.height(4.dp)) // padding de la última sombra
                        }
                    }
                    // Botón agregar rediseñado minimalista y elegante
                    Spacer(Modifier.height(16.dp))
                    
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            JuicyButton(
                                text = "AÑADIR DEUDA",
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                icon = Icons.Rounded.Add,
                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                soundManager = soundManager,
                                height = 56.dp,
                                modifier = if (state.debts.isEmpty()) Modifier.scale(pulseScale) else Modifier
                            ) { 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.addDebtRow() 
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            JuicyButton(
                                text = "CALC. LIQ.",
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                icon = Icons.Rounded.AccountBalance,
                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                soundManager = soundManager,
                                height = 56.dp
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showLiquidationModal = true
                            }
                        }
                    }
                }
            }
        }

        // ========== CARD: AJUSTE DE LIQUIDEZ ==========
        item {
            JuicyCard(
                color = MaterialTheme.colorScheme.surface,
                shadowColor = MaterialTheme.colorScheme.outline
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "CAPACIDAD DE PAGO",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(16.dp))

                    val totalPagos = state.debts.sumOf { it.payment.toBD() }
                    val ajusteInput = state.capacityCNCA.toBD()
                    val capacidadFinal = totalPagos.add(ajusteInput)
                    val isNegative = ajusteInput < BigDecimal.ZERO

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
                        Spacer(Modifier.height(12.dp))
                        
                        // Efecto Glow para la tarjeta de ajuste
                        val adjustmentGlow = if (ajusteInput.compareTo(BigDecimal.ZERO) == 0) {
                            Color.Transparent
                        } else if (isNegative) {
                            JuicyRed.copy(alpha = 0.2f)
                        } else {
                            JuicyGreen.copy(alpha = 0.2f)
                        }
                        
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, adjustmentGlow)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Pagos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "$${totalPagos.toCurrency()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    if (isNegative) "−" else "+",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Ajuste",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isNegative) JuicyRed else JuicyGreen
                                    )
                                    Text(
                                        "$${ajusteInput.abs().toCurrency()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNegative) JuicyRed else JuicyGreen
                                    )
                                }
                                Text(
                                    "=",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Capacidad",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "$${capacidadFinal.toCurrency()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ========== BOTONES: CALCULAR + LIMPIAR ==========
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    JuicyButton(
                        text = "CALCULAR",
                        color = if (isDark) Color.White else Color(0xFF1C1C1E),
                        shadowColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFC7C7CC),
                        textColor = if (isDark) Color.Black else Color.White,
                        height = 56.dp,
                        isLoading = state.isLoading,
                        soundManager = soundManager
                    ) { viewModel.calculateRefinance(ctx, soundManager) }
                }
                JuicyIconButton(
                    icon = Icons.Rounded.DeleteSweep,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 56.dp,
                    soundManager = soundManager
                ) { viewModel.clearRefinanceData() }
            }
        }

        // ========== COMPARAR OFERTAS ==========
        item {
            AnimatedVisibility(
                visible = state.offersCNCA.isNotEmpty() && !state.isLoading,
                enter = fadeIn(tween(400)) + expandVertically(tween(400, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                JuicyButton(
                    text = "COMPARAR OFERTAS",
                    color = GoldAccentDark,
                    shadowColor = Color(0xFF8B6914),
                    textColor = Color.White,
                    icon = Icons.AutoMirrored.Rounded.CompareArrows,
                    soundManager = soundManager
                ) {
                    viewModel.toggleOffersComparator(true)
                }
            }
        }

        // ========== RESULTADOS AGRUPADOS ==========
        if (state.isLoading) {
            item { ResultCardSkeletonList(count = 3) }
        } else {
            item {
                GroupedResultsSection(
                    offers = state.offersCNCA,
                    showPremium = state.showPremiumOffers,
                    onTogglePremium = { viewModel.togglePremiumOffers() },
                    onSelectOffer = { offer ->
                        soundManager.play("pop")
                        viewModel.onSelectOffer(offer)
                    },
                    onShowAmortization = { offer ->
                        soundManager.play("magic")
                        viewModel.showAmortizationFor(offer)
                    },
                    emptyContent = {
                        val hasDebts = state.debts.any { it.balance.toBD() > BigDecimal.ZERO }
                        if (hasDebts) {
                            EmptyStateIllustration(
                                title = "Sin ofertas disponibles",
                                message = "Revisa los criterios de rechazo abajo",
                                emoji = "🚦"
                            )
                            Spacer(Modifier.height(16.dp))
                            val cap = state.debts.sumOf { it.payment.toBD() }.add(state.capacityCNCA.toBD())
                            ViabilityTrafficLight(
                                checks = listOf(
                                    "Liquidez y Capacidad" to (cap > BigDecimal.ZERO),
                                    "Validación Plazos (Extensión)" to false,
                                    "Antigüedad (16+ pagos)" to false
                                )
                            )
                        } else {
                            EmptyStateIllustration(
                                title = "Calculadora Inteligente",
                                message = "Agrega tus deudas para comenzar el análisis",
                                emoji = "👋"
                            )
                        }
                    }
                )
            }
        }
        // Spacer para isla flotante
        item { Spacer(Modifier.height(120.dp)) }
    }

    if (showLiquidationModal) {
        LiquidationCalculatorModal(
            onDismiss = { showLiquidationModal = false },
            soundManager = soundManager
        )
    }
}


@Composable
fun NewCreditScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    soundManager: SoundManager,
    ctx: Context
) {
    val isDark = isSystemInDarkTheme()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ========== HEADER TEMÁTICO VERDE ==========
        item {
            JuicyGradientCard(
                brush = if (isDark) {
                    Brush.horizontalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF2C2C2E)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF3A3A3C)))
                },
                shadowColor = Color(0xFF0A0A0A)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.AttachMoney,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "CRÉDITO NUEVO",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "Cotizador multi-producto",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // ========== CARD: DATOS DE COTIZACIÓN ==========
        item {
            JuicyCard(
                color = MaterialTheme.colorScheme.surface,
                shadowColor = MaterialTheme.colorScheme.outline
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "DATOS DE COTIZACIÓN",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(16.dp))

                    // Toggle: Por capacidad / Por monto
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            JuicyChip(
                                selected = !state.isByAmount,
                                text = "Por capacidad",
                                onClick = {
                                    soundManager.play("pop"); viewModel.toggleCalculationMode(false)
                                })
                        }
                        Box(Modifier.weight(1f)) {
                            JuicyChip(
                                selected = state.isByAmount,
                                text = "Por monto",
                                onClick = {
                                    soundManager.play("pop"); viewModel.toggleCalculationMode(true)
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

                    // Tasa Preferencial eliminada de UI, su lógica es inherente a los productos premium
                }
            }
        }

        // ========== BOTONES: COTIZAR + LIMPIAR ==========
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    JuicyButton(
                        text = "COTIZAR",
                        color = if (isDark) Color.White else Color(0xFF1C1C1E),
                        shadowColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFC7C7CC),
                        textColor = if (isDark) Color.Black else Color.White,
                        height = 56.dp,
                        isLoading = state.isLoading,
                        soundManager = soundManager
                    ) { viewModel.calculateNewCredit(ctx, soundManager) }
                }
                JuicyIconButton(
                    icon = Icons.Rounded.DeleteSweep,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 56.dp,
                    soundManager = soundManager
                ) { viewModel.clearNewCreditData() }
            }
        }

        // ========== COMPARAR OFERTAS ==========
        item {
            AnimatedVisibility(
                visible = state.offersNew.isNotEmpty() && !state.isLoading,
                enter = fadeIn(tween(400)) + expandVertically(tween(400, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                JuicyButton(
                    text = "COMPARAR OFERTAS",
                    color = GoldAccentDark,
                    shadowColor = Color(0xFF8B6914),
                    textColor = Color.White,
                    icon = Icons.AutoMirrored.Rounded.CompareArrows,
                    soundManager = soundManager
                ) {
                    viewModel.toggleOffersComparator(true)
                }
            }
        }

        // ========== RESULTADOS AGRUPADOS ==========
        if (state.isLoading) {
            item { ResultCardSkeletonList(count = 3) }
        } else {
            item {
                GroupedResultsSection(
                    offers = state.offersNew,
                    showPremium = state.showPremiumOffers,
                    onTogglePremium = { viewModel.togglePremiumOffers() },
                    onSelectOffer = { offer ->
                        soundManager.play("pop")
                        viewModel.onSelectOffer(offer)
                    },
                    onShowAmortization = { offer ->
                        soundManager.play("magic")
                        viewModel.showAmortizationFor(offer)
                    },
                    emptyContent = {
                        EmptyStateIllustration(
                            title = "Sin ofertas para ti",
                            message = "Ajusta la capacidad de pago o plazo",
                            emoji = "🔍"
                        )
                    }
                )
            }
        }
        // Spacer para isla flotante
        item { Spacer(Modifier.height(120.dp)) }
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
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            JuicyGradientCard(
                brush = if (isDark) {
                    Brush.horizontalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF2C2C2E)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF3A3A3C)))
                },
                shadowColor = Color(0xFF0A0A0A)
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
                                    selectedContainerColor = JuicyBlue,
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
                            colors = CheckboxDefaults.colors(checkedColor = JuicyBlue)
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    JuicyButton(
                        text = "CALCULAR",
                        icon = Icons.Outlined.Savings,
                        color = if (isDark) Color.White else Color(0xFF1C1C1E),
                        shadowColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFC7C7CC),
                        textColor = if (isDark) Color.Black else Color.White,
                        height = 56.dp,
                        isLoading = state.isLoading,
                        soundManager = soundManager,
                        onClick = {
                            soundManager.play("cash")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.calculateInsurance(ctx, soundManager)
                        }
                    )
                }
                JuicyIconButton(
                    icon = Icons.Rounded.DeleteSweep,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 56.dp,
                    soundManager = soundManager
                ) {
                    soundManager.play("pop")
                    viewModel.clearInsuranceData()
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

        
        item { Spacer(Modifier.height(120.dp)) }
    }
}

// =================================================================================================
// 14. REUSABLE COMPONENTS
// =================================================================================================
@Composable
fun FormSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f),
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.15f)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ClientFormContent(
    viewModel: CalculatorViewModel,
    onGenerate: (String) -> Unit,
    onGenerateTicket: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val d = state.clientData

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
            "Completa todos los campos para generar el layout o ticket",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            item {
                FormSectionCard("IDENTIFICACIÓN", Icons.Rounded.Badge) {
                    JuicyInput(
                        "Nombre Completo",
                        d.name,
                        { viewModel.updateClientData(d.copy(name = it)) },
                        KeyboardType.Text,
                        Icons.Rounded.Person
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.weight(1f)) {
                                    JuicyInput(
                                        "NSS", d.nss,
                                        { if (it.length <= 11) viewModel.updateClientData(d.copy(nss = it)) },
                                        KeyboardType.Number, Icons.Rounded.Numbers
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
                                "CURP", d.curp,
                                { if (it.length <= 18) viewModel.updateClientData(d.copy(curp = it)) },
                                KeyboardType.Text, Icons.Rounded.Badge
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "RFC (con homoclave)", d.rfc,
                                { if (it.length <= 13) viewModel.updateClientData(d.copy(rfc = it)) },
                                KeyboardType.Text, Icons.Rounded.Badge
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "Edad", d.age,
                                { if (it.length <= 3) viewModel.updateClientData(d.copy(age = it)) },
                                KeyboardType.Number, Icons.Rounded.CalendarMonth
                            )
                        }
                    }
                }
            }

            item {
                FormSectionCard("CONTACTO Y BANCO", Icons.Rounded.Phone) {
                    JuicyInput(
                        "Correo Electrónico", d.email,
                        { viewModel.updateClientData(d.copy(email = it)) },
                        KeyboardType.Email, Icons.Rounded.Person
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "Teléfono", d.phone,
                                { if (it.length <= 10) viewModel.updateClientData(d.copy(phone = it)) },
                                KeyboardType.Phone, Icons.Rounded.Phone
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            JuicyInput(
                                "Tipo de Pensión", d.pensionType,
                                { viewModel.updateClientData(d.copy(pensionType = it)) },
                                KeyboardType.Text, Icons.Rounded.AccountBalance
                            )
                        }
                    }
                    JuicyInput(
                        "CLABE Interbancaria", d.clabe,
                        { if (it.length <= 18) viewModel.updateClientData(d.copy(clabe = it)) },
                        KeyboardType.Number, Icons.Rounded.CreditCard
                    )
                    JuicyInput(
                        "Banco", d.bank,
                        { viewModel.updateClientData(d.copy(bank = it)) },
                        KeyboardType.Text, Icons.Rounded.AccountBalance
                    )
                }
            }

            item {
                FormSectionCard("DATOS MÉDICOS", Icons.Rounded.Info) {
                    JuicyInput(
                        "Enfermedad", d.disease,
                        { viewModel.updateClientData(d.copy(disease = it)) },
                        KeyboardType.Text, Icons.Rounded.LocalFireDepartment
                    )
                    JuicyInput(
                        "Fecha de Inicio de Padecimiento", d.diseaseDate,
                        { viewModel.updateClientData(d.copy(diseaseDate = it)) },
                        KeyboardType.Text, Icons.Rounded.CalendarMonth
                    )
                }
            }

            item {
                FormSectionCard("TIPO DE TRÁMITE", Icons.Rounded.Description) {
                    TransactionTypeSelector(
                        selected = d.transactionType,
                        onSelect = { viewModel.updateClientData(d.copy(transactionType = it)) }
                    )
                    
                    if (d.transactionType != "NUEVO") {
                        JuicyInput(
                            "ID (Crédito a Liquidar)", d.creditId,
                            { viewModel.updateClientData(d.copy(creditId = it)) },
                            KeyboardType.Text, Icons.Rounded.Numbers
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f)) {
                                JuicyInput(
                                    "Descuento Mensual", d.discountAmount,
                                    { viewModel.updateClientData(d.copy(discountAmount = it)) },
                                    KeyboardType.Text, Icons.Rounded.AttachMoney
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                JuicyInput(
                                    "Núm. Descuentos", d.numDiscounts,
                                    { viewModel.updateClientData(d.copy(numDiscounts = it)) },
                                    KeyboardType.Number, Icons.Rounded.Numbers
                                )
                            }
                        }
                        JuicyInput(
                            "Marca que Liquida", d.liquidatingBrand,
                            { viewModel.updateClientData(d.copy(liquidatingBrand = it)) },
                            KeyboardType.Text, Icons.Rounded.Business
                        )
                    }
                }
            }

            item {
                FormSectionCard("REFERENCIAS", Icons.Rounded.Person) {
                    Text(
                        "1ª Referencia",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    JuicyInput(
                        "Nombre Completo", d.ref1Name,
                        { viewModel.updateClientData(d.copy(ref1Name = it)) },
                        KeyboardType.Text, Icons.Rounded.Person
                    )
                    JuicyInput(
                        "Teléfono", d.ref1Phone,
                        { if (it.length <= 10) viewModel.updateClientData(d.copy(ref1Phone = it)) },
                        KeyboardType.Phone, Icons.Rounded.Phone
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f))

                    Text(
                        "2ª Referencia",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    JuicyInput(
                        "Nombre Completo", d.ref2Name,
                        { viewModel.updateClientData(d.copy(ref2Name = it)) },
                        KeyboardType.Text, Icons.Rounded.Person
                    )
                    JuicyInput(
                        "Teléfono", d.ref2Phone,
                        { if (it.length <= 10) viewModel.updateClientData(d.copy(ref2Phone = it)) },
                        KeyboardType.Phone, Icons.Rounded.Phone,
                        imeAction = ImeAction.Done
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botón principal de Copiar Layout
        JuicyButton(
            text = "COPIAR TEXTO AL PORTAPAPELES",
            color = JuicyGreen,
            shadowColor = JuicyGreenShadow,
            textColor = Color.White,
            height = 60.dp,
            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
            onClick = { onGenerate(viewModel.generateLayout()) }
        )
        
        Spacer(Modifier.height(12.dp))
        
        // Botón Ticket Dorado (Estilo Premium/Secundario)
        JuicyButton(
            text = "COMPARTIR TICKET DORADO",
            color = Color(0xFF1F2937), // Dark Gray/Black
            shadowColor = Color(0xFF111827),
            textColor = Color(0xFFFFD700), // Gold Text
            height = 60.dp,
            icon = Icons.Rounded.Star,
            iconColor = Color(0xFFFFD700),
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .fillMaxWidth()
            .bouncyClick(soundManager = soundManager) {
                if (!isLoading) {
                    onClick()
                }
            }
            .background(color, RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (color == Color.Transparent) shadowColor else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = textColor,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor ?: textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = if (isLoading) "PROCESANDO..." else text,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                letterSpacing = 0.5.sp
            )
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
    Box(
        modifier = Modifier
            .size(size)
            .bouncyClick(soundManager = soundManager, onClick = onClick)
            .background(color, RoundedCornerShape(10.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
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
    
    val borderColor = when {
        isError -> ErrorRed
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                color = if (isError) ErrorRed else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            
            // Character counter
            if (maxLength != null && isFocused) {
                val remaining = maxLength - value.length
                Text(
                    text = "$remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (remaining < 5) WarningOrange else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = if (remaining < 5) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                )
            }
        }
        
        // Clean Minimalist container
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = if (isFocused) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = borderColor,
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
                        tint = if (isError) ErrorRed else if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
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
                        fontWeight = FontWeight.Medium,
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
                        tint = ErrorRed,
                        modifier = Modifier.padding(horizontal = 12.dp).size(20.dp)
                     )
                }
            }
        }
    }
}

/**
 * Muestra una lista de esqueletos (loading) con efecto shimmer sutil
 */
@Composable
fun ResultCardSkeletonList(count: Int = 3) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(count) {
            JuicyCard(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "skeletonAlpha"
                )
                
                Column(Modifier.padding(20.dp).graphicsLayer { this.alpha = alpha }) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(Modifier.width(100.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).background(Color.Gray.copy(alpha = 0.2f)))
                        Box(Modifier.width(60.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).background(Color.Gray.copy(alpha = 0.2f)))
                    }
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray.copy(alpha = 0.2f)))
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray.copy(alpha = 0.2f)))
                        Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray.copy(alpha = 0.2f)))
                    }
                }
            }
        }
    }
}

/**
 * Gráfica de comparación Antes vs Después (Ahorro)
 */
@Composable
fun BeforeAfterChart(
    currentPayment: BigDecimal,
    newPayment: BigDecimal
) {
    val totalMax = currentPayment.max(newPayment).toFloat()
    if (totalMax <= 0f) return

    val ahorro = (currentPayment - newPayment).max(BigDecimal.ZERO)
    val porcentajeAhorro = if (currentPayment > BigDecimal.ZERO) {
        ahorro.multiply(BigDecimal("100")).divide(currentPayment, 2, RoundingMode.HALF_UP)
    } else BigDecimal.ZERO

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "ESTIMACIÓN DE AHORRO MENSUAL",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val haptic = LocalHapticManager.current
            
            ChartBar(
                label = "ACTUAL",
                value = currentPayment,
                maxValue = totalMax,
                color = Color.Gray,
                isNew = false
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.CompareArrows,
                    contentDescription = null,
                    tint = JuicyBlue.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                if (ahorro > BigDecimal.ZERO) {
                    Text(
                        "-${porcentajeAhorro.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = JuicyGreen
                    )
                }
            }

            ChartBar(
                label = "NUEVO",
                value = newPayment,
                maxValue = totalMax,
                color = if (newPayment < currentPayment) JuicyGreen else JuicyOrange,
                isNew = true
            )
        }
        
        if (ahorro > BigDecimal.ZERO) {
            Spacer(Modifier.height(20.dp))
            Surface(
                color = JuicyGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, JuicyGreen.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Savings, 
                        null, 
                        tint = JuicyGreen, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "¡Libera $${ahorro.toCurrency()} al mes!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = JuicyGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartBar(
    label: String,
    value: BigDecimal,
    maxValue: Float,
    color: Color,
    isNew: Boolean
) {
    var barHeight by remember { mutableFloatStateOf(0f) }
    val targetHeight = (value.toFloat() / maxValue).coerceIn(0.1f, 1f)
    
    LaunchedEffect(value) {
        animate(
            0f, targetHeight,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
        ) { v, _ -> barHeight = v }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(
            "$${value.toInt()}", // Simplificado para la gráfica
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isNew) color else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight(barHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(color, color.copy(alpha = 0.7f))
                    )
                )
                .then(
                    if (isNew) Modifier.border(2.dp, color.copy(alpha = 0.3f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    else Modifier
                )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun JuicyCard(
    color: Color, 
    shadowColor: Color, 
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(12.dp))
            .border(1.dp, shadowColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        content()
    }
}

@Composable
fun JuicyGradientCard(
    modifier: Modifier = Modifier,
    brush: Brush,
    shadowColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush, RoundedCornerShape(16.dp))
            .border(1.dp, shadowColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
fun JuicyChip(selected: Boolean, text: String, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (selected) {
        if (isDark) Color.White else Color(0xFF1C1C1E)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val textColor = if (selected) {
        if (isDark) Color.Black else Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderColor = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .bouncyClick(onClick = onClick)
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp),
            fontSize = 14.sp
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
    val state by viewModel.uiState.collectAsState()
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
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
                
                // 🧾 "Premium Receipt" (Ticket Layout) para el Desglose
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Mensualidad
                        DetailRow(
                            icon = Icons.Rounded.AttachMoney,
                            label = "MENSUALIDAD",
                            value = "$${offer.monthlyPayment.toCurrency()}",
                            valueColor = JuicyOrange
                        )
                        
                        // Dashed Divider (Línea Punteada)
                        DashedDivider()
                        
                        // Plazo
                        DetailRow(
                            icon = Icons.Rounded.CalendarMonth,
                            label = "PLAZO",
                            value = "${offer.term} Meses",
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                        DashedDivider()
                        
                        // CAT
                        DetailRow(
                            icon = Icons.AutoMirrored.Rounded.TrendingDown,
                            label = "CAT",
                            value = offer.cat,
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // --- 📊 NUEVO: Gráfica Antes vs Después (Solo Refinanciamiento) ---
                val isRefinance = state.selectedTab == 0
                if (isRefinance) {
                    val currentPayment = remember(state.debts) { 
                        state.debts.sumOf { it.payment.toBD() } 
                    }
                    
                    if (currentPayment > BigDecimal.ZERO) {
                        Spacer(Modifier.height(24.dp))
                        BeforeAfterChart(
                            currentPayment = currentPayment,
                            newPayment = offer.monthlyPayment
                        )
                    }
                }
                // -----------------------------------------------------------------

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
                
                // Botones de acción unificados (Barra Inferior)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón Compartir (Secundario)
                    Box(Modifier.weight(0.3f)) {
                         JuicyButton(
                            text = "",
                            icon = Icons.Rounded.Share,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shadowColor = MaterialTheme.colorScheme.outline.copy(alpha=0.15f),
                            textColor = MaterialTheme.colorScheme.onSurface,
                            height = 60.dp,
                            soundManager = soundManager
                         ) {
                             soundManager.play("pop")
                             viewModel.prepareTicketSharing(context, offer)
                         }
                    }

                    // Botón Continuar (Principal)
                    Box(Modifier.weight(1f)) {
                         JuicyButton(
                            text = "CONTINUAR",
                            color = productColors.primary,
                            shadowColor = productColors.shadow,
                            textColor = Color.White,
                            height = 60.dp,
                            soundManager = soundManager,
                            onClick = onContinue
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
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray.copy(alpha = 0.3f),
    thickness: androidx.compose.ui.unit.Dp = 1.dp,
    dashIntervals: FloatArray = floatArrayOf(15f, 15f)
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .padding(vertical = 16.dp),
        onDraw = {
            drawLine(
                color = color,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = thickness.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(dashIntervals, 0f)
            )
        }
    )
}

// =================================================================================================
// SEMÁFORO DE OFERTAS - Sistema de colores por ranking
// =================================================================================================
enum class SortMode {
    MEJOR_VALOR,
    MAYOR_MONTO,
    MENOR_DESCUENTO,
    MAYOR_PLAZO
}

// La clave de ordenamiento para una oferta dada un SortMode
fun LoanOffer.sortKey(mode: SortMode): Comparable<*> = when (mode) {
    SortMode.MEJOR_VALOR    -> amount.divide(monthlyPayment.max(BigDecimal.ONE), 4, java.math.RoundingMode.HALF_UP)
    SortMode.MAYOR_MONTO    -> amount
    SortMode.MENOR_DESCUENTO -> monthlyPayment.negate()   // negate: menor pago = mejor → mayor negate
    SortMode.MAYOR_PLAZO    -> BigDecimal(term)
}

// =================================================================================================
// GROUPED RESULTS SECTION
// =================================================================================================
@Composable
fun GroupedResultsSection(
    offers: List<LoanOffer>,
    showPremium: Boolean,
    onTogglePremium: () -> Unit,
    onSelectOffer: (LoanOffer) -> Unit,
    onShowAmortization: (LoanOffer) -> Unit,
    emptyContent: @Composable () -> Unit
) {
    val allBrands = listOf("CONSUBANCO", "CONSUPAGO", "OPCIPRES", "MASNOMINA")
    var sortMode by remember { mutableStateOf(SortMode.MEJOR_VALOR) }

    fun brandColor(brand: String): Color = when (brand) {
        "CONSUBANCO" -> JuicyBlue
        "CONSUPAGO"  -> JuicyOrange
        "OPCIPRES"   -> JuicyGreen
        "MASNOMINA"  -> JuicyRed
        else         -> Color.Gray
    }

    fun isVariantPremium(name: String) = name.endsWith("ESPECIAL") || name.endsWith("VIP")

    @Suppress("UNCHECKED_CAST")
    fun sortList(list: List<LoanOffer>): List<LoanOffer> =
        list.sortedByDescending { it.sortKey(sortMode) as Comparable<Any> }

    // Filtrar según toggle premium
    val visibleOffers = if (showPremium) offers else offers.filter { !isVariantPremium(it.product) }

    // Agrupar por marca
    val rawGrouped = allBrands.associateWith { brand ->
        visibleOffers.filter { it.product.startsWith(brand) }
    }.filter { it.value.isNotEmpty() }

    // ── CLAVE DEL FIX: ordenar las marcas según su mejor oferta bajo el sortMode actual ──
    @Suppress("UNCHECKED_CAST")
    val sortedBrands = rawGrouped.entries.sortedByDescending { (_, brandOffers) ->
        val best = sortList(brandOffers).firstOrNull()
        best?.sortKey(sortMode) as? Comparable<Any>
    }.map { it.key }

    val hasPremiumOffers = offers.any { isVariantPremium(it.product) }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

        // ── Etiqueta + Toggle Premium ────────────────────────────
        if (offers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RESULTADOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (hasPremiumOffers) {
                    FilterChip(
                        selected = showPremium,
                        onClick = onTogglePremium,
                        label = { Text(if (showPremium) "ESP + VIP visibles" else "Solo Tradicional", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                if (showPremium) Icons.Rounded.AutoAwesome else Icons.Rounded.Business,
                                null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor  = GoldAccent.copy(alpha = 0.12f),
                            selectedLabelColor      = GoldAccentDark,
                            selectedLeadingIconColor = GoldAccentDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = showPremium,
                            selectedBorderColor = GoldAccent.copy(alpha = 0.4f),
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
            }
        }

        if (rawGrouped.isEmpty()) {
            emptyContent()
        } else {
            // ── Chips de Orden (las marcas se reordenan al instante) ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                SortMode.values().forEach { mode ->
                    val isSelected = mode == sortMode
                    val label = when (mode) {
                        SortMode.MEJOR_VALOR     -> "Mejor Valor"
                        SortMode.MAYOR_MONTO     -> "Mayor Monto"
                        SortMode.MENOR_DESCUENTO -> "Menor Desc."
                        SortMode.MAYOR_PLAZO     -> "Mayor Plazo"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick  = { sortMode = mode },
                        label = {
                            Text(
                                text       = label,
                                fontSize   = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            selectedLabelColor     = MaterialTheme.colorScheme.primary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = isSelected,
                            selectedBorderWidth = 1.dp,
                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // ── Grupos de marcas, ya ordenados por la mejor oferta de cada marca ──
            sortedBrands.forEachIndexed { brandIdx, brand ->
                val brandOffers = rawGrouped[brand] ?: return@forEachIndexed
                val color       = brandColor(brand)

                // En lugar de separar y forzar el orden tradicional > premium, ordenamos todo respetando el SortMode actual
                val sortedOffers = sortList(brandOffers)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    // Encabezado de Marca
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = brand,
                            fontWeight = FontWeight.Black,
                            fontSize   = 13.sp,
                            letterSpacing = 1.sp,
                            color      = color
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .height(1.dp)
                                .weight(1f)
                                .background(color.copy(alpha = 0.2f))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "${brandOffers.size} opción${if (brandOffers.size != 1) "es" else ""}",
                            fontSize   = 10.sp,
                            color      = color.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Renderizar Ofertas Unificadas (respeta el SortMode absoluto)
                    sortedOffers.forEachIndexed { i, offer ->
                        val isPremiumElement = isVariantPremium(offer.product)
                        val isGlobalBest = i == 0 && brandIdx == 0
                        
                        AnimatedResultItem(index = i) {
                            ResultCard(
                                offer              = offer.copy(isBest = isGlobalBest),
                                onClick            = { onSelectOffer(offer) },
                                onShowAmortization = { onShowAmortization(offer) },
                                rankIndex          = if (isGlobalBest) 0 else if (isPremiumElement) -1 else i + 1,
                                accentColor        = color,
                                isPremium          = isPremiumElement
                            )
                        }
                        if (i < sortedOffers.lastIndex) Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}




// =================================================================================================
@Composable
fun ResultCard(
    offer: LoanOffer,
    onClick: () -> Unit,
    onShowAmortization: (() -> Unit)? = null,
    rankIndex: Int = 0,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    isPremium: Boolean = false
) {
    val isBest = rankIndex == 0 || offer.isBest

    // Badge de variante
    val variant = when {
        offer.product.endsWith("VIP")      -> "VIP"
        offer.product.endsWith("ESPECIAL") -> "ESPECIAL"
        else                               -> null
    }

    val variantColor = when (variant) {
        "VIP"      -> GoldAccent
        "ESPECIAL" -> accentColor
        else       -> null
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (isBest || isPremium) 2.dp else 1.dp,
            color = when {
                isPremium && variant == "VIP" -> GoldAccent.copy(alpha = 0.7f)
                isPremium -> accentColor.copy(alpha = 0.5f)
                isBest    -> GoldAccent.copy(alpha = 0.8f)
                else      -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            }
        ),
        shadowElevation = if (isBest || isPremium) 8.dp else 2.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // Fondo marca de agua (Logo Sutil)
            Text(
                text = offer.product.take(4).uppercase(), // Ejemplo: "CONS", "MASN"
                fontSize = 110.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 24.dp, y = 30.dp)
            )

            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // --- 1. HEADER ---
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Nombre del producto (solo la parte base, sin variante)
                        val baseName = offer.product
                            .replace(" TRADICIONAL", "")
                            .replace(" ESPECIAL", "")
                            .replace(" VIP", "")
                        Text(
                            text = baseName,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                        if (offer.tipoTasa == "RETENCION") {
                            Text(
                                text = "TASA PREFERENCIAL",
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                fontSize = 10.sp
                            )
                        } else {
                            Text(
                                text = offer.tipoTasa,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Badge de variante
                        if (variant != null && variantColor != null) {
                            Surface(
                                color = variantColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, variantColor.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (variant == "VIP") Icons.Rounded.AutoAwesome else Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = variantColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = variant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = variantColor,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }
                        }
                        // Badge MEJOR OPCIÓN
                        if (isBest && variant == null) {
                            Surface(
                                color = GoldAccent.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "MEJOR OPCIÓN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }

                }

                // Banner rechazo preferencial (si existe)
                if (offer.rejectionReason != null) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = offer.rejectionReason,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // --- 2. HERO SECTION ---
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (offer.amount > offer.mainValue) "REMANENTE" else "MONTO A RECIBIR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )

                    RollingCurrency(
                        value = offer.mainValue,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 46.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(32.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(Modifier.height(20.dp))

                // --- 3. FOOTER (Pago y Plazo) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "DESCUENTO", 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "$${offer.monthlyPayment.toCurrency()}", 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.primary, 
                            fontSize = 20.sp
                        )
                    }
                    
                    Box(Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "PLAZO", 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "${offer.term} Meses", 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onSurface, 
                            fontSize = 20.sp
                        )
                    }
                }

                // --- 4. TABLA DE AMORTIZACIÓN ---
                if (onShowAmortization != null) {
                    Spacer(Modifier.height(24.dp))
                    Surface(
                        onClick = { onShowAmortization() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.TableChart,
                                contentDescription = "Ver Amortización",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Ver Tabla de Amortización",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
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
    val isFinancialProvider = options.any { opt -> listOf("CONSUBANCO", "CONSUPAGO", "OPCIPRES", "MASNOMINA").any { p -> opt.startsWith(p) } }
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

// =================================================================================================
// PREMIUM ONBOARDING SCREEN — Primer Ingreso
// =================================================================================================

/** Datos del asesor recopilados en el onboarding */
private data class OnboardingData(
    val name: String = "",
    val username: String = "",
    val password: String = "",
    val supervisor: String = ""
)

@Composable
fun MandatoryConfigScreen(viewModel: CalculatorViewModel, ctx: Context, soundManager: SoundManager) {
    val isDark = isSystemInDarkTheme()
    val hapticManager = LocalHapticManager.current

    // ── Estado de pasos (0, 1, 2) ──────────────────────────────────────────────
    var step by remember { mutableIntStateOf(0) }
    var data by remember { mutableStateOf(OnboardingData()) }

    // Validaciones por paso
    val step0Valid = data.name.isNotBlank()
    val step1Valid = data.username.isNotBlank() && data.password.isNotBlank()
    val step2Valid = data.supervisor.isNotBlank()
    val currentStepValid = when (step) {
        0 -> step0Valid
        1 -> step1Valid
        2 -> step2Valid
        else -> false
    }

    // ── Mascot escala según el paso ────────────────────────────────────────────
    val mascotScale by animateFloatAsState(
        targetValue = if (step == 0) 1f else 0.72f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "mascotScale"
    )

    val bgBase = if (isDark) BgDark else BgLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBase)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenedor deslizable para la parte superior (evita problemas de tamaño al abrir el teclado)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(56.dp))

                // ── Dots de progreso ───────────────────────────────────────────
                OnboardingStepDots(currentStep = step, totalSteps = 3)

                Spacer(Modifier.height(32.dp))

                // ── Mascot / Hero ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .graphicsLayer { scaleX = mascotScale; scaleY = mascotScale }
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.avatar_mascot),
                        contentDescription = "Buddy",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── Contenido por paso con Crossfade (más robusto y evita bugs de medición de tamaño) ──
                androidx.compose.animation.Crossfade(
                    targetState = step,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    animationSpec = tween(300),
                    label = "stepContent"
                ) { targetStep ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (targetStep) {
                            // ── PASO 1: Bienvenida + Nombre ───────────────────
                            0 -> {
                                OnboardingStepHeader(
                                    title = "¡Bienvenido, Asesor!",
                                    subtitle = "Empecemos conociéndonos.\n¿Cuál es tu nombre completo?",
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(28.dp))
                                OnboardingInputField(
                                    label = "NOMBRE COMPLETO",
                                    value = data.name,
                                    onValueChange = { data = data.copy(name = it) },
                                    icon = Icons.Rounded.Person,
                                    placeholder = "Ej. María García López",
                                    isDark = isDark,
                                    imeAction = ImeAction.Done
                                )
                            }
                            // ── PASO 2: Credenciales ──────────────────────────
                            1 -> {
                                OnboardingStepHeader(
                                    title = "Tus Credenciales",
                                    subtitle = "Se usan para generar el\nlayout de captura automático.",
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(28.dp))
                                OnboardingInputField(
                                    label = "USUARIO (BP)",
                                    value = data.username,
                                    onValueChange = { data = data.copy(username = it) },
                                    icon = Icons.Rounded.Badge,
                                    placeholder = "Ej. MGARCIA01",
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(16.dp))
                                OnboardingInputField(
                                    label = "CONTRASEÑA DEL SISTEMA",
                                    value = data.password,
                                    onValueChange = { data = data.copy(password = it) },
                                    icon = Icons.Rounded.Lock,
                                    placeholder = "Tu contraseña de acceso",
                                    isDark = isDark,
                                    imeAction = ImeAction.Done
                                )
                            }
                            // ── PASO 3: Supervisor + Confirmar ────────────────
                            2 -> {
                                OnboardingStepHeader(
                                    title = "Último Detalle",
                                    subtitle = "¿Quién es tu supervisor?\nAparecerá en tus reportes.",
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(28.dp))
                                OnboardingInputField(
                                    label = "NOMBRE DEL SUPERVISOR",
                                    value = data.supervisor,
                                    onValueChange = { data = data.copy(supervisor = it) },
                                    icon = Icons.Rounded.Person,
                                    placeholder = "Ej. Carlos Ramírez",
                                    isDark = isDark,
                                    imeAction = ImeAction.Done
                                )
                                Spacer(Modifier.height(16.dp))
                                OnboardingProfileSummary(data = data, isDark = isDark)
                            }
                        }
                    }
                }
            }

            // ── Botones de navegación ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón ATRÁS
                if (step > 0) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.08f)
                                else Color.Black.copy(alpha = 0.06f)
                            )
                            .bouncyClick {
                                hapticManager?.light()
                                soundManager.play("pop")
                                if (step > 0) {
                                    step--
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.KeyboardArrowRight,
                            contentDescription = "Atrás",
                            tint = if (isDark) Color.White.copy(0.7f) else Color.Black.copy(0.6f),
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { scaleX = -1f }
                        )
                    }
                }

                // Botón SIGUIENTE / COMENZAR
                val isLastStep = step == 2
                val btnColor = if (currentStepValid) {
                    if (isLastStep) GoldAccent else JuicyBlue
                } else {
                    if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
                }
                val btnShadow = if (currentStepValid) {
                    if (isLastStep) GoldAccentDark else JuicyBlueShadow
                } else {
                    if (isDark) Color(0xFF1C1C1E) else Color(0xFFD1D1D6)
                }
                val btnTextColor = if (currentStepValid) Color.White
                else if (isDark) Color.White.copy(0.25f) else Color.Black.copy(0.25f)

                JuicyButton(
                    text = when {
                        !currentStepValid -> if (isLastStep) "COMPLETA EL CAMPO" else "INGRESA TU DATO"
                        isLastStep -> "\uD83C\uDF1F  ¡COMENZAR AHORA!"
                        else -> "SIGUIENTE  →"
                    },
                    color = btnColor,
                    shadowColor = btnShadow,
                    textColor = btnTextColor,
                    soundManager = soundManager,
                    modifier = Modifier.weight(1f)
                ) {
                    // Clic lambda: usar valores dinámicos para evitar condiciones de carrera por clicks rápidos (ej. double-tap)
                    val isValidDynamic = when (step) {
                        0 -> data.name.isNotBlank()
                        1 -> data.username.isNotBlank() && data.password.isNotBlank()
                        2 -> data.supervisor.isNotBlank()
                        else -> false
                    }
                    if (isValidDynamic) {
                        if (step == 2) {
                            soundManager.play("magic")
                            hapticManager?.success()
                            viewModel.saveProfile(
                                ctx,
                                UserProfile(data.name, data.username, data.password, data.supervisor)
                            )
                        } else if (step < 2) {
                            hapticManager?.medium()
                            soundManager.play("pop")
                            step++
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Dots de progreso ─────────────────────────────────────────────────────────
@Composable
private fun OnboardingStepDots(currentStep: Int, totalSteps: Int) {
    val isDark = isSystemInDarkTheme()
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalSteps) { i ->
            val isActive = i == currentStep
            val isPast = i < currentStep
            val dotWidth by animateFloatAsState(
                targetValue = if (isActive) 28f else 8f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                label = "dotWidth$i"
            )
            val dotAlpha by animateFloatAsState(
                targetValue = if (isActive || isPast) 1f else 0.3f,
                label = "dotAlpha$i"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(dotWidth.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isActive -> GoldAccent.copy(alpha = dotAlpha)
                            isPast -> GoldAccent.copy(alpha = 0.6f)
                            else -> if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.12f)
                        }
                    )
            )
        }
    }
}

// ── Header de paso ────────────────────────────────────────────────────────────
@Composable
private fun OnboardingStepHeader(title: String, subtitle: String, isDark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = if (isDark) Color.White else Color(0xFF1C1C1E),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Color.White.copy(alpha = 0.55f) else Color(0xFF1C1C1E).copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

// ── Campo de entrada premium para onboarding ──────────────────────────────────
@Composable
private fun OnboardingInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    isDark: Boolean,
    imeAction: ImeAction = ImeAction.Next
) {
    val hapticManager = LocalHapticManager.current
    var isFocused by remember { mutableStateOf(false) }
    val isValid = value.isNotBlank()

    val borderColor by animateColorAsState(
        targetValue = when {
            isValid -> SuccessGreen
            isFocused -> GoldAccent
            else -> if (isDark) Color.White.copy(0.12f) else Color.Black.copy(0.10f)
        },
        animationSpec = tween(250),
        label = "borderColor"
    )
    val bgColor = if (isDark) Color(0xFF18181B) else Color.White

    Column(modifier = Modifier.fillMaxWidth()) {
        // Label con checkmark
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White.copy(0.5f) else Color.Black.copy(0.4f),
                letterSpacing = 1.2.sp
            )
            if (isValid) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        // Superficie del input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .onFocusChanged {
                    if (it.isFocused != isFocused) {
                        isFocused = it.isFocused
                        if (it.isFocused) hapticManager?.light()
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(16.dp))
            Icon(
                icon,
                contentDescription = null,
                tint = if (isValid) SuccessGreen
                       else if (isFocused) GoldAccent
                       else if (isDark) Color.White.copy(0.35f) else Color.Black.copy(0.3f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = value,
                onValueChange = {
                    if (it != value) hapticManager?.tick()
                    onValueChange(it)
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    color = if (isDark) Color.White else Color(0xFF1C1C1E)
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = imeAction
                ),
                singleLine = true,
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                color = if (isDark) Color.White.copy(0.22f) else Color.Black.copy(0.22f),
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
                            )
                        }
                        inner()
                    }
                }
            )
            // Trailing check animado
            AnimatedVisibility(visible = isValid) {
                Box(
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            if (!isValid) Spacer(Modifier.width(14.dp))
        }
    }
}

// ── Resumen del perfil (Paso 3) ───────────────────────────────────────────────
@Composable
private fun OnboardingProfileSummary(data: OnboardingData, isDark: Boolean) {
    val cardBg = if (isDark) Color(0xFF18181B) else Color.White
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "RESUMEN DE TU PERFIL",
                style = MaterialTheme.typography.labelMedium,
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ProfileSummaryRow(Icons.Rounded.Person, "Nombre", data.name, isDark)
            ProfileSummaryRow(Icons.Rounded.Badge, "Usuario", data.username, isDark)
            ProfileSummaryRow(Icons.Rounded.Lock, "Contraseña", "••••••", isDark)
            ProfileSummaryRow(Icons.Rounded.Person, "Supervisor", data.supervisor.ifBlank { "—" }, isDark)
        }
    }
}

@Composable
private fun ProfileSummaryRow(icon: ImageVector, label: String, value: String, isDark: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, contentDescription = null,
            tint = GoldAccent.copy(alpha = 0.7f),
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = if (isDark) Color.White.copy(0.45f) else Color.Black.copy(0.45f),
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color.White.copy(0.85f) else Color(0xFF1C1C1E),
            maxLines = 1
        )
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




@Composable
fun JuicyLottieLoader(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_empty_search))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}


@Composable
fun JuicyLottieSuccess(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_levelup))
    LottieAnimation(
        composition = composition,
        iterations = 1,
        modifier = modifier
    )
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
    // Marcas base y sus variantes disponibles
    val brands = listOf("CONSUBANCO", "CONSUPAGO", "OPCIPRES", "MASNOMINA")
    val variantMap = mapOf(
        "CONSUBANCO" to listOf("TRADICIONAL", "ESPECIAL", "VIP"),
        "CONSUPAGO"  to listOf("TRADICIONAL", "ESPECIAL"),
        "OPCIPRES"   to listOf("TRADICIONAL", "ESPECIAL"),
        "MASNOMINA"  to emptyList()
    )

    // Parsear selección actual → marca + variante
    val selectedBrand   = brands.firstOrNull { selectedValue.startsWith(it) } ?: "CONSUBANCO"
    val selectedVariant = if (selectedValue.contains(" ")) selectedValue.removePrefix("$selectedBrand ") else ""

    var expanded by remember { mutableStateOf(false) }

    fun getBrandColor(name: String): Color = when (name) {
        "CONSUBANCO" -> JuicyBlue
        "CONSUPAGO"  -> JuicyOrange
        "OPCIPRES"   -> JuicyGreen
        "MASNOMINA"  -> JuicyRed
        else         -> Color.Gray
    }

    val brandColor = getBrandColor(selectedBrand)
    val variants   = variantMap[selectedBrand] ?: emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "FINANCIERA",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        // ── Dropdown de MARCA ─────────────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedBrand,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth()
                    .height(56.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    color = brandColor
                ),
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(brandColor, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = brandColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                brands.forEach { brand ->
                    val bc = getBrandColor(brand)
                    DropdownMenuItem(
                        text = {
                            Text(
                                brand,
                                fontWeight = FontWeight.Bold,
                                color = if (brand == selectedBrand) bc else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = { Icon(Icons.Rounded.Business, null, tint = bc) },
                        onClick = {
                            // Al cambiar marca → defaultear a TRADICIONAL (o solo la marca si no tiene variantes)
                            val vars = variantMap[brand] ?: emptyList()
                            onSelectionChange(if (vars.isEmpty()) brand else "$brand TRADICIONAL")
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                    if (brand != brands.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }

        // ── Chips de VARIANTE (solo si la marca tiene variantes) ──────────
        if (variants.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                variants.forEach { variant ->
                    val isSelected = selectedVariant == variant
                    val chipIcon = when (variant) {
                        "VIP"      -> Icons.Rounded.AutoAwesome
                        "ESPECIAL" -> Icons.Rounded.Star
                        else       -> Icons.Rounded.Business
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick  = { onSelectionChange("$selectedBrand $variant") },
                        label    = {
                            Text(
                                variant,
                                fontSize   = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(chipIcon, null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor  = brandColor.copy(alpha = 0.15f),
                            selectedLabelColor      = brandColor,
                            selectedLeadingIconColor = brandColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = isSelected,
                            selectedBorderColor = brandColor,
                            selectedBorderWidth = 1.5.dp
                        )
                    )
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
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif
                ),
                leadingIcon = {
                    Icon(Icons.Rounded.CalendarMonth, null, tint = JuicyPurple)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
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
                        Text("Mes", Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text("Interés", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.End)
                        Text("IVA", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.End)
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
                        Text("${row.month}", Modifier.weight(0.5f), fontSize = 11.sp)
                        Text("$${row.interest.toCurrency()}", Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.End, color = JuicyRed.copy(alpha = 0.7f))
                        Text("$${row.iva.toCurrency()}", Modifier.weight(0.7f), fontSize = 11.sp, textAlign = TextAlign.End, color = JuicyOrange.copy(alpha = 0.7f))
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

// =================================================================================================
// 15. HISTORY SHEET UI
// =================================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: List<CalculationDraft>,
    onRestore: (CalculationDraft) -> Unit,
    onClear: () -> Unit,
    onDelete: (CalculationDraft) -> Unit
) {
    Column(
        Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Row(
           Modifier.fillMaxWidth(), 
           horizontalArrangement = Arrangement.SpaceBetween,
           verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Historial Reciente", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Black
            )
            if (history.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text("Limpiar", color = MaterialTheme.colorScheme.error)
                }
            } else {
                Icon(Icons.Rounded.History, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        if (history.isEmpty()) {
            Column(
                Modifier.fillMaxWidth(), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "📭", 
                    fontSize = 48.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No hay borradores guardados",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(history, key = { it.timestamp }) { draft ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                onDelete(draft)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) 
                                MaterialTheme.colorScheme.errorContainer 
                            else Color.Transparent

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    ) {
                        JuicyCard(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
                            shadowColor = MaterialTheme.colorScheme.outline.copy(alpha=0.3f),
                            modifier = Modifier.clickable { onRestore(draft) }
                        ) {
                            Row(
                                Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        draft.type, 
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${draft.debts.size} deudas • Cap: $${draft.capacity}", 
                                        style = MaterialTheme.typography.bodySmall, 
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(draft.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(draft.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.4f)
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

// ========== CALCULADORA DE LIQUIDACIÓN ==========

fun calculateMonthlyRate(capital: Double, payment: Double, months: Int): Double {
    if (payment * months <= capital) return 0.0
    var lower = 0.000001
    var upper = 1.0
    var rate = 0.0
    for (i in 0 until 100) {
        rate = (lower + upper) / 2.0
        val presentValue = payment * (1 - Math.pow(1 + rate, -months.toDouble())) / rate
        if (presentValue.compareTo(capital) > 0) {
            lower = rate
        } else {
            upper = rate
        }
        if (kotlin.math.abs(upper - lower) < 1e-8) break
    }
    return rate
}

data class LiquidationResultData(
    val isValid: Boolean = false,
    val iMensual: Double = 0.0,
    val iAnual: Double = 0.0,
    val cat: Double = 0.0,
    val saldoCorte: Double = 0.0,
    val saldoHoy: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidationCalculatorModal(
    onDismiss: () -> Unit,
    soundManager: SoundManager? = null
) {
    var capitalInput by remember { mutableStateOf("") }
    var paymentInput by remember { mutableStateOf("") }
    var monthsInput by remember { mutableStateOf("") }
    var paymentsMadeInput by remember { mutableStateOf("") }
    var daysStr by remember { mutableStateOf("0") }
    var isQuincenal by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    val formatCurrency = { amount: Double ->
        java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "MX")).format(amount)
    }

    val calculationResult = remember(capitalInput, paymentInput, monthsInput, paymentsMadeInput, daysStr) {
        try {
            val p = capitalInput.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
            val r = paymentInput.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
            val n = monthsInput.toIntOrNull() ?: 0
            val hechos = paymentsMadeInput.toIntOrNull() ?: 0
            val dias = daysStr.toIntOrNull() ?: 0

            if (p > 0 && r > 0 && n > 0 && hechos >= 0 && n > hechos) {
                // Tasa por periodo (quincenal o mensual)
                val iPeriodo = calculateMonthlyRate(p, r, n)
                val periodsPerYear = if (isQuincenal) 24.0 else 12.0
                
                val iMensual = if (isQuincenal) iPeriodo * 2 else iPeriodo
                val iAnual = iMensual * 12
                val cat = (Math.pow(1 + iPeriodo, periodsPerYear) - 1) * 100

                val remaining = n - hechos
                val saldoCorte = if (iPeriodo > 0) r * (1 - Math.pow(1 + iPeriodo, -remaining.toDouble())) / iPeriodo else r * remaining.toDouble()

                // Interés diario aproximado para liquidación
                val daysInPeriod = if (isQuincenal) 15.2 else 30.4
                val iDiaria = iPeriodo / daysInPeriod
                val interesGenerado = saldoCorte * iDiaria * dias
                val saldoHoy = saldoCorte + interesGenerado

                LiquidationResultData(
                    isValid = true,
                    iMensual = iMensual,
                    iAnual = iAnual,
                    cat = cat,
                    saldoCorte = saldoCorte,
                    saldoHoy = saldoHoy
                )
            } else {
                LiquidationResultData(isValid = false)
            }
        } catch (e: Exception) {
            LiquidationResultData(isValid = false)
        }
    }

    // Efecto háptico cuando se encuentra un estado válido
    LaunchedEffect(calculationResult.isValid, calculationResult.saldoHoy) {
        if (calculationResult.isValid) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Icon header
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Calculadora de Liquidación",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Calcula instantáneamente el monto de liquidación reconstruyendo las condiciones exactas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Results Card (Dynamic sizing)
            AnimatedVisibility(
                visible = calculationResult.isValid,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                JuicyGradientCard(
                    brush = Brush.linearGradient(listOf(Color(0xFF141E30), Color(0xFF243B55))),
                    shadowColor = Color(0xFF141E30).copy(alpha=0.3f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(
                        Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MONTO TOTAL A LIQUIDAR HOY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = formatCurrency(calculationResult.saldoHoy),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1.5).sp
                        )
                        Spacer(Modifier.height(24.dp))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Al corte mensual", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha=0.6f))
                                Text(formatCurrency(calculationResult.saldoCorte), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Tasa (Mensual)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha=0.6f))
                                Text(String.format(java.util.Locale.US, "%.2f %%", calculationResult.iMensual * 100), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Inputs Group 1
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DATOS DEL PRÉSTAMO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { isQuincenal = false },
                            color = if (!isQuincenal) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ) {
                            Text("MEN", fontSize = 10.sp, modifier = Modifier.padding(horizontal=8.dp, vertical=4.dp), color = if(!isQuincenal) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            onClick = { isQuincenal = true },
                            color = if (isQuincenal) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ) {
                            Text("QUI", fontSize = 10.sp, modifier = Modifier.padding(horizontal=8.dp, vertical=4.dp), color = if(isQuincenal) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CompactInput(
                        value = capitalInput,
                        label = "Capital Originado",
                        isCurrency = true,
                        inputType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    ) { capitalInput = it }
                    CompactInput(
                        value = paymentInput,
                        label = if(isQuincenal) "Descuento Qui." else "Descuento Men.",
                        isCurrency = true,
                        inputType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    ) { paymentInput = it }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CompactInput(
                        value = monthsInput,
                        label = if(isQuincenal) "Total Quincenas" else "Total Meses",
                        inputType = androidx.compose.ui.text.input.KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    ) { monthsInput = it }
                    CompactInput(
                        value = paymentsMadeInput,
                        label = "Pagos Pagados",
                        inputType = androidx.compose.ui.text.input.KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    ) { paymentsMadeInput = it }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Inputs Group 2
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "INTERESES A HOY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                
                CompactInput(
                    value = daysStr,
                    label = "Días desde el último pago (corte)",
                    inputType = androidx.compose.ui.text.input.KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                ) { daysStr = it }

                if (capitalInput.contains("35,000") && paymentInput.contains("3,561")) {
                    Text(
                        text = "💡 Tip: En cartas de Consupago, usa '17' días para igualar el ejemplo exacto.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
