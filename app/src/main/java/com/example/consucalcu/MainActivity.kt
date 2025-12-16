package com.example.consucalcu

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
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Activar Edge-to-Edge para inmersión total (la barra de estado se adapta)
        enableEdgeToEdge()

        setContent {
            // 2. Control de Tema (Persistente en sesión o Auto)
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            // Definimos el esquema de color basado en tu código "Neo-Juicy"
            val colors = if (isDarkTheme) DarkColorScheme else LightColorScheme

            // 3. Configuración de la Barra de Estado (Status Bar)
            // Necesitas añadir la dependencia 'com.google.accompanist:accompanist-systemuicontroller:0.32.0'
            // O usar la API nativa de EdgeToEdge. Aquí un ejemplo visual con Accompanist para control total:
            /* val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = !isDarkTheme
                )
            }
            */

            MaterialTheme(
                colorScheme = colors,
                typography = JuicyTypography // Tu tipografía personalizada
            ) {
                // 4. Inyección del ViewModel (Hilt es mejor, pero viewModel() estándar funciona para esto)
                // Tu Composable Principal
                ConsuCalcuApp(
                    isDark = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

// ==========================================
// 0. HELPER EXTENSIONS & CONSTANTS
// ==========================================
val BD_ZERO = BigDecimal.ZERO; val BD_500 = BigDecimal("500"); val BD_1000 = BigDecimal("1000")
fun String.toBD(): BigDecimal = this.replace(",", "").replace("$", "").trim().toBigDecimalOrNull() ?: BD_ZERO
fun Double.toBD(): BigDecimal = BigDecimal.valueOf(this)
fun BigDecimal.toCurrency(): String = DecimalFormat("#,###.00").format(this)

// ==========================================
// 1. TEMA & ESTILO "NEO-JUICY" (Refinado Fintech)
// ==========================================

// COLORES PRIMARIOS (Más profundos y profesionales)
val JuicyBlue = Color(0xFF007AFF) // Azul "System Blue" moderno (antes era cian claro)
val JuicyBlueShadow = Color(0xFF0056B3) // Sombra con contraste fuerte

val JuicyGreen = Color(0xFF2E7D32) // Verde Financiero/Billetes (antes era verde lima)
val JuicyGreenShadow = Color(0xFF1B5E20)

val JuicyOrange = Color(0xFFFF9800) // Naranja Advertencia (se mantiene vibrante)
val JuicyOrangeShadow = Color(0xFFF57C00)

val JuicyRed = Color(0xFFD32F2F) // Rojo Alerta estándar Material
val JuicyRedShadow = Color(0xFFB71C1C)

val JuicyPurple = Color(0xFF6200EE) // Morado Tech
val JuicyPurpleShadow = Color(0xFF3700B3)

// FONDOS Y SUPERFICIES (Menos fatiga visual)
val BgLight = Color(0xFFF2F5F8) // Un gris-azulado muy pálido (Sensación Premium)
val BgDark = Color(0xFF121212)  // Negro OLED casi puro

val SurfaceLight = Color(0xFFFFFFFF) // Tarjetas blancas sobre fondo grisáceo
val SurfaceDark = Color(0xFF1E1E1E)  // Tarjetas gris oscuro

// TEXTOS (La clave de la elegancia: No usar negro puro)
val TextDark = Color(0xFF102027) // Azul marino casi negro (Elegante)
val TextLight = Color(0xFFEEEEEE)

val BorderLight = Color(0xFFD1D9E6) // Borde sutil gris-azulado
val BorderDark = Color(0xFF37464F)

private val LightColorScheme = lightColorScheme(
    primary = JuicyBlue,
    onPrimary = Color.White,
    secondary = JuicyGreen,
    onSecondary = Color.White,
    background = BgLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    outline = BorderLight,
    surfaceVariant = Color(0xFFE1E6ED)
)

private val DarkColorScheme = darkColorScheme(
    primary = JuicyBlue,
    onPrimary = Color.White,
    secondary = JuicyGreen,
    onSecondary = Color.White,
    background = BgDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    outline = BorderDark,
    surfaceVariant = Color(0xFF2C2C2C)
)

// Tipografía: Añadimos un poco más de espaciado para legibilidad financiera
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
    headlineMedium = TextStyle( // Para los montos de dinero grandes
        fontFamily = FontFamily.Monospace, // Monospace ayuda a alinear números
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

// ==========================================
// 1. UTILS UX AVANZADA (MODIFIERS & TRANSFORMATIONS)
// ==========================================

// Transformation para que el input se vea "$10,000" mientras escribes "10000"
class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        // Separamos la parte entera de la decimal si existe un punto
        val parts = originalText.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else null

        // Formateamos solo la parte entera con comas
        val formattedInteger = try {
            if (integerPart.isNotEmpty()) {
                val num = integerPart.toLong()
                DecimalFormat("#,###").format(num)
            } else ""
        } catch (e: Exception) {
            integerPart
        }

        // Reconstruimos el texto visual
        val newTextString = if (decimalPart != null) {
            "$$formattedInteger.$decimalPart"
        } else {
            "$$formattedInteger"
        }

        val newText = AnnotatedString(newTextString)

        // Mapeo de cursor inteligente
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Simplificación robusta: ponemos el cursor al final si está editando al final,
                // o calculamos la diferencia de comas agregadas.
                // Para evitar bugs de salto de cursor, en inputs financieros simples
                // suele ser más seguro forzar el cursor al final o usar la longitud total.
                return newText.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return originalText.length
            }
        }

        return TransformedText(newText, offsetMapping)
    }
}


// Efecto "Bounce" (Rebote) al hacer click
fun Modifier.bouncyClick(soundManager: SoundManager? = null, onClick: () -> Unit) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale",
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val haptic = LocalHapticFeedback.current

    this
        .scale(scale)
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

// Efecto Skeleton/Shimmer de carga
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

// ==========================================
// 2. LOGIC LAYERS (SOUND, DATA, ENGINE)
// ==========================================
class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()

    init {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(10) // Aumenté a 10 para soportar el habla rápida sin cortes
            .setAudioAttributes(attr)
            .build()
        try {
            // Carga de sonidos
            soundMap["pop"] = soundPool.load(context, R.raw.pop, 1)
            soundMap["cash"] = soundPool.load(context, R.raw.cash, 1)
            soundMap["magic"] = soundPool.load(context, R.raw.magic, 1)

            // 👇 NUEVO: Sonido de voz (asegúrate que el archivo esté en res/raw/villager_blip.mp3)
            soundMap["villager"] = soundPool.load(context, R.raw.villager_blip, 1)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play(name: String) {
        // Para la voz, bajamos un poco el volumen (0.6f) para que no aturda al repetirse rápido
        val volume = if (name == "villager") 0.6f else 1f
        // Rate (velocidad): Podemos variarla ligeramente (0.9f a 1.1f) para dar naturalidad,
        // pero lo dejaremos en 1f para el estilo clásico.
        soundMap[name]?.let { soundPool.play(it, volume, volume, 1, 0, 1f) }
    }

    fun release() {
        soundPool.release()
    }
}

object TicketGenerator {
    // Ahora es una función suspendida para correr en IO
    suspend fun generateAndShare(
        context: Context,
        offer: LoanOffer,
        client: ClientData,
        advisor: String
    ) = withContext(Dispatchers.IO) {
        val width = 1080;
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1CB0F6"); style = Paint.Style.FILL
        }
        val cardPaint = Paint().apply {
            color = android.graphics.Color.WHITE; style = Paint.Style.FILL; setShadowLayer(
            20f,
            0f,
            10f,
            android.graphics.Color.DKGRAY
        )
        }
        val textPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#4B4B4B"); textSize = 50f; typeface =
            Typeface.DEFAULT_BOLD; isAntiAlias = true
        }
        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#58CC02"); textSize = 80f; typeface =
            Typeface.DEFAULT_BOLD; isAntiAlias = true; textAlign = Paint.Align.CENTER
        }
        val labelPaint = Paint().apply {
            color = android.graphics.Color.GRAY; textSize = 40f; typeface =
            Typeface.DEFAULT; isAntiAlias = true
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        val cardMargin = 80f
        canvas.drawRoundRect(
            cardMargin,
            300f,
            width - cardMargin,
            height - 300f,
            60f,
            60f,
            cardPaint
        )
        canvas.drawText("PROPUESTA APROBADA", width / 2f, 450f, titlePaint)
        var y = 600f;
        val xLabel = 150f;
        val xValue = width - 150f;
        val rightAlign = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }

        fun drawRow(label: String, value: String, isBig: Boolean = false) {
            canvas.drawText(label, xLabel, y, labelPaint)
            rightAlign.textSize = if (isBig) 90f else 50f
            if (isBig) rightAlign.color = android.graphics.Color.parseColor("#1CB0F6")
            canvas.drawText(value, xValue, y, rightAlign)
            y += 120f
        }
        drawRow("Cliente", client.name.take(15)); drawRow("Producto", offer.product); y += 50f
        drawRow("MONTO", "$${offer.amount.toCurrency()}", true); drawRow(
        "Plazo",
        "${offer.term} Meses"
    )
        drawRow("Mensualidad", "$${offer.monthlyPayment.toCurrency()}"); drawRow(
        "CAT",
        offer.cat
    )
        y = height - 400f; titlePaint.textSize = 40f; titlePaint.color =
        android.graphics.Color.WHITE
        canvas.drawText(
            "Asesor: $advisor",
            width / 2f,
            y,
            titlePaint
        ); canvas.drawText("Generado con ConsuCalcu Pro", width / 2f, y + 60f, titlePaint)

        try {
            val filename = "Ticket_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    filename
                ); put(
                MediaStore.MediaColumns.MIME_TYPE,
                "image/jpeg"
            ); if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/ConsuCalcu"
                )
            }
            }
            val imageUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            imageUri?.let { uri ->
                context.contentResolver.openOutputStream(uri)
                    ?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                // Compartir en Main Thread
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

// --- DATA & ENGINE (Sin cambios lógicos mayores, solo estructura) ---
data class Debt(
    val id: Long = System.currentTimeMillis(),
    val product: String = "",
    val balance: String = "",
    val payment: String = "",
    val rate: String = "",
    val originalTerm: String = "",
    val paymentsMade: String = ""
)

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
    val tipoTasa: String = "TRADICIONAL"
)

data class ClientData(
    val name: String = "",
    val nss: String = "",
    val curp: String = "",
    val rfc: String = "",
    val phone: String = "",
    val clabe: String = "",
    val bank: String = "",
    val age: String = "",
    val pensionType: String = "",
    val agreement: String = "IMSS",
    val disease: String = "NINGUNA",
    val diseaseDate: String = "",
    val ref1: String = "",
    val ref2: String = ""
)

data class UserProfile(
    val name: String = "",
    val username: String = "",
    val password: String = "",
    val supervisor: String = "",
    val xp: Int = 0,
    val level: Int = 1
)

object FinancialConfig {
    data class ProductMeta(val tasaRef: Double, val catTrad: String, val catRet: String)
    data class RetentionRule(val factor: BigDecimal, val minAmount: BigDecimal)
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
                60 to BigDecimal("34.477850"),
                54 to BigDecimal("36.500000"),
                48 to BigDecimal("38.650000"),
                36 to BigDecimal("45.480000")
            ),
            RetentionRule(BigDecimal("30.954160"), BigDecimal("100000.00")),
            ProductMeta(2.50, "39.03%", "31.50%"),
            BigDecimal("5000"),
            BigDecimal("1000"),
            mapOf(
                60 to BigDecimal("40000"),
                54 to BigDecimal("95000"),
                48 to BigDecimal("100000"),
                36 to BigDecimal("145000")
            )
        ),
        "CONSUPAGO" to ProductData(
            mapOf(
                60 to BigDecimal("36.977550"),
                54 to BigDecimal("39.000000"),
                48 to BigDecimal("41.200000"),
                36 to BigDecimal("47.500000"),
                24 to BigDecimal("59.000000"),
                18 to BigDecimal("70.000000"),
                12 to BigDecimal("98.000000")
            ),
            RetentionRule(BigDecimal("33.257160"), BigDecimal("50000.00")),
            ProductMeta(2.73, "44.39%", "36.42%"),
            BigDecimal("5000"),
            BigDecimal("500"),
            mapOf(
                12 to BigDecimal("35000"),
                18 to BigDecimal("35000"),
                24 to BigDecimal("35000"),
                36 to BigDecimal("15000"),
                48 to BigDecimal("15000")
            )
        ),
        "OPCIPRES" to ProductData(
            mapOf(
                60 to BigDecimal("35.985630"),
                54 to BigDecimal("38.000000"),
                48 to BigDecimal("40.100000"),
                36 to BigDecimal("47.500000")
            ),
            RetentionRule(BigDecimal("32.222830"), BigDecimal("75000.00")),
            ProductMeta(2.67, "42.26%", "34.21%"),
            BigDecimal("5000"),
            BigDecimal("750"),
            mapOf(
                60 to BigDecimal("30000"),
                54 to BigDecimal("45000"),
                48 to BigDecimal("50000"),
                36 to BigDecimal("70000"),
                24 to BigDecimal("70000")
            )
        ),
        "MASNOMINA" to ProductData(
            mapOf(
                60 to BigDecimal("37.200780"),
                48 to BigDecimal("40.580000"),
                36 to BigDecimal("46.740000"),
                24 to BigDecimal("59.920000")
            ),
            RetentionRule(BigDecimal("38.000000"), BigDecimal("999999999")),
            ProductMeta(2.73, "44.87%", "N/A"),
            BigDecimal("8000"),
            BigDecimal("0")
        )
    )
    val STANDARD_TERMS = listOf("12", "18", "24", "36", "48", "54", "60")
}

object FinancialEngine {
    data class CalculationResult(val amount: BigDecimal, val payment: BigDecimal)

    private fun roundSipre(value: BigDecimal, step: BigDecimal = BD_500): BigDecimal =
        value.divide(step, 0, RoundingMode.FLOOR).multiply(step)

    fun calculate(
        prodKey: String,
        value: BigDecimal,
        term: Int,
        isByAmount: Boolean,
        useRetention: Boolean,
        roundingStep: BigDecimal = BD_500
    ): Pair<CalculationResult, String>? {
        val config = FinancialConfig.PRODUCTS[prodKey] ?: return null
        var factor = config.factorsTrad[term] ?: return null
        var tipoTasa = "TRADICIONAL"
        if (useRetention && term == 60 && config.retention != null) {
            val estAmount = if (isByAmount) value else value.divide(
                config.retention.factor,
                10,
                RoundingMode.HALF_UP
            ).multiply(BD_1000)
            if (estAmount >= config.retention.minAmount) {
                factor = config.retention.factor; tipoTasa = "RETENCION"
            }
        }
        if (tipoTasa == "RETENCION" && term == 60) {
            var currentVal = value;
            var currentModeByAmount = isByAmount
            repeat(50) {
                val montoBase = if (currentModeByAmount) roundSipre(
                    currentVal,
                    roundingStep
                ) else roundSipre(
                    currentVal.divide(factor, 10, RoundingMode.HALF_UP).multiply(BD_1000),
                    roundingStep
                )
                val pagoCalc =
                    montoBase.divide(BD_1000).multiply(factor).setScale(2, RoundingMode.HALF_UP)
                if (!currentModeByAmount && pagoCalc > value) {
                    val nuevoMonto = montoBase.subtract(roundingStep)
                    if (nuevoMonto < BigDecimal("5000")) return null
                    currentVal = nuevoMonto; currentModeByAmount = true
                } else return Pair(CalculationResult(montoBase, pagoCalc), tipoTasa)
            }
            return null
        }
        val result = if (isByAmount) {
            val m = roundSipre(value, roundingStep); CalculationResult(
                m,
                m.divide(BD_1000).multiply(factor).setScale(2, RoundingMode.HALF_UP)
            )
        } else {
            val raw = value.divide(factor, 10, RoundingMode.HALF_UP).multiply(BD_1000);
            val m = roundSipre(raw, roundingStep); CalculationResult(
                m,
                m.divide(BD_1000).multiply(factor).setScale(2, RoundingMode.HALF_UP)
            )
        }
        return Pair(result, tipoTasa)
    }

    fun checkNormative(
        prodKey: String,
        debts: List<Debt>,
        offer: LoanOffer,
        config: FinancialConfig.ProductData
    ): Pair<Boolean, List<String>> {
        val reasons = mutableListOf<String>();
        val totalDebts = debts.sumOf { it.balance.toBD() };
        val remnant = offer.amount.subtract(totalDebts);
        val increase = remnant
        if (remnant < BD_ZERO) return Pair(
            false,
            emptyList()
        ); if (remnant >= BD_1000) reasons.add("Liquidez")
        val allGold = debts.all {
            (it.paymentsMade.toIntOrNull() ?: 0) >= 24
        }; if (allGold && debts.isNotEmpty()) {
            reasons.add("Antigüedad 24+"); return Pair(true, reasons)
        }
        var validDebtsCount = 0;
        var needsIncrement = false;
        val tasaRef = config.meta.tasaRef
        for (debt in debts) {
            val dRate = debt.rate.toDoubleOrNull() ?: 0.0;
            val dPaid = debt.paymentsMade.toIntOrNull() ?: 0
            if (dRate > tasaRef) reasons.add("Mejora Tasa"); if (dPaid >= 16) reasons.add("Antigüedad")
            if (prodKey == "MASNOMINA") {
                if (remnant > BD_ZERO) validDebtsCount++
            } else if (prodKey == "CONSUPAGO") {
                if (increase >= BigDecimal("500") || dPaid >= 16) validDebtsCount++
            } else {
                val tasaLimite = if (prodKey == "CONSUBANCO") 2.57 else 2.68; if (dPaid >= 16) {
                    if (dRate <= tasaLimite) validDebtsCount++ else needsIncrement = true
                }
            }
        }
        if (prodKey == "MASNOMINA") return Pair(remnant > BD_ZERO, reasons)
        if (prodKey == "CONSUPAGO") return Pair(
            validDebtsCount > 0 && remnant > BD_ZERO,
            reasons
        )
        if (needsIncrement) {
            val minInc =
                if (prodKey == "CONSUBANCO") BigDecimal("1000") else BigDecimal("750"); if (increase >= minInc) {
                reasons.add("Incremento Suficiente"); return Pair(true, reasons)
            } else return Pair(false, emptyList())
        }
        return Pair(validDebtsCount > 0, reasons)
    }
}

// ==========================================
// 3. VIEWMODEL
// ==========================================
data class CalculatorUiState(
    val selectedTab: Int = 0,
    val debts: List<Debt> = listOf(Debt()),
    val capacityCNCA: String = "",
    val offersCNCA: List<LoanOffer> = emptyList(),
    val newCreditInput: String = "",
    val isByAmount: Boolean = false,
    val offersNew: List<LoanOffer> = emptyList(),
    val showClientForm: Boolean = false, // Ahora controlará el BottomSheet
    val showConfigDialog: Boolean = false,
    val showSuccessAnim: Boolean = false,
    val selectedOfferForLayout: LoanOffer? = null,
    val clientData: ClientData = ClientData(),
    val userProfile: UserProfile = UserProfile(),
    val showTutorial: Boolean = false,
    val tutorialStep: Int = 0,
    val showOnboarding: Boolean = false,
    val isLoading: Boolean = false,
    val showConfetti: Boolean = false,
    val streak: Int = 0
)

class CalculatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState = _uiState.asStateFlow()
    private val gson = Gson()

    fun loadData(ctx: Context) {
        val prefs = ctx.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE)

        // --- 1. LÓGICA DE RACHAS (STREAKS) 🔥 ---
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val today = sdf.format(java.util.Date())

        // Calculamos la fecha de ayer
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DATE, -1)
        val yesterday = sdf.format(cal.time)

        val lastDate = prefs.getString("last_streak_date", "") ?: ""
        var currentStreak = prefs.getInt("current_streak", 0)

        // Si la última vez que entró NO es hoy, hay que calcular
        if (lastDate != today) {
            if (lastDate == yesterday) {
                // Entró ayer, así que la racha sube
                currentStreak++
            } else {
                // No entró ayer (o es nuevo), la racha se reinicia a 1
                currentStreak = 1
            }
            // Guardamos el progreso de hoy
            prefs.edit()
                .putString("last_streak_date", today)
                .putInt("current_streak", currentStreak)
                .apply()
        }
        // Si lastDate == today, no hacemos nada, se mantiene la racha que ya tenía hoy.

        // --- 2. TU LÓGICA ORIGINAL (RECUPERAR DATOS) ---
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

        // --- 3. ACTUALIZAR EL ESTADO (INCLUYENDO LA NUEVA RACHA) ---
        _uiState.update {
            it.copy(
                userProfile = p,
                debts = debtsList,
                clientData = client,
                capacityCNCA = prefs.getString("capCNCA", "") ?: "",
                showOnboarding = !onboardingSeen,
                streak = currentStreak // <--- Aquí pasamos la racha a la UI
            )
        }
    }

    fun saveData(ctx: Context) {
        val s = _uiState.value;
        val prefs = ctx.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("debts", gson.toJson(s.debts))
            .putString("client", gson.toJson(s.clientData)).putString("capCNCA", s.capacityCNCA)
            .apply()
    }

    fun saveProfile(ctx: Context, p: UserProfile) {
        val prefs = ctx.getSharedPreferences("ConsuCalcuPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("name", p.name).putString("username", p.username)
            .putString("password", p.password).putString("supervisor", p.supervisor)
            .putInt("xp", p.xp).putInt("level", p.level).apply(); _uiState.update {
            it.copy(
                userProfile = p,
                showConfigDialog = false
            )
        }
    }

    private fun addXp(ctx: Context) {
        val p = _uiState.value.userProfile;
        var nx = p.xp + 10;
        var nl = p.level; if (nx >= nl * 100) {
            nl++; nx = 0; Toast.makeText(ctx, "¡NIVEL $nl ALCANZADO!", Toast.LENGTH_LONG).show()
        }; saveProfile(ctx, p.copy(xp = nx, level = nl))
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
        _uiState.update { it.copy(selectedOfferForLayout = offer, showClientForm = true) }
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
            _uiState.update { it.copy(isLoading = true, offersCNCA = emptyList()) }
            // Simulamos delay de red/cálculo para mostrar el Skeleton
            delay(1500)
            val calculatedOffers = withContext(Dispatchers.Default) {
                val state = _uiState.value;
                val cap = state.capacityCNCA.toBD();
                val debtBalance = state.debts.sumOf { it.balance.toBD() };
                val debts = state.debts.filter { it.balance.toBD() > BD_ZERO };
                val results = mutableListOf<LoanOffer>()
                if (cap > BD_ZERO && debts.isNotEmpty()) {
                    FinancialConfig.PRODUCTS.forEach { (prodKey, config) ->
                        listOf(60, 54, 48, 36, 24).forEach { term ->
                            val calcData = FinancialEngine.calculate(
                                prodKey,
                                cap,
                                term,
                                false,
                                false,
                                BD_500
                            )
                            if (calcData != null) {
                                val (res, tipo) = calcData;
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
                                    ); if (viable && offer.amount > debtBalance) {
                                        val remnant =
                                            offer.amount.subtract(debtBalance); results.add(
                                            offer.copy(
                                                mainValue = remnant,
                                                mainLabel = "REMANENTE",
                                                reasons = reasons
                                            )
                                        )
                                    }
                                }
                            }
                            if (term == 60) {
                                val calcRet = FinancialEngine.calculate(
                                    prodKey,
                                    cap,
                                    term,
                                    false,
                                    true,
                                    BD_500
                                )
                                if (calcRet != null && calcRet.second == "RETENCION") {
                                    val (res, tipo) = calcRet;
                                    val offerRet = LoanOffer(
                                        prodKey,
                                        res.amount,
                                        res.payment,
                                        term,
                                        cat = config.meta.catRet,
                                        tipoTasa = tipo
                                    ); val (viable, reasons) = FinancialEngine.checkNormative(
                                        prodKey,
                                        debts,
                                        offerRet,
                                        config
                                    ); if (viable && offerRet.amount > debtBalance) {
                                        val remnant = offerRet.amount.subtract(debtBalance);
                                        val finalReasons = reasons.toMutableList()
                                            .apply { add("Tasa Preferencial") }; results.add(
                                            offerRet.copy(
                                                mainValue = remnant,
                                                mainLabel = "REMANENTE",
                                                reasons = finalReasons
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                results.sortedByDescending { it.mainValue }
                    .mapIndexed { i, o -> o.copy(isBest = i == 0) }
            }
            if (calculatedOffers.isNotEmpty()) {
                soundManager.play("cash"); addXp(ctx); triggerConfetti()
            }
            _uiState.update { it.copy(isLoading = false, offersCNCA = calculatedOffers) }
        }
    }

    fun calculateNewCredit(ctx: Context, soundManager: SoundManager) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, offersNew = emptyList()) }
            delay(1500) // Delay para efecto visual Skeleton
            val calculatedOffers = withContext(Dispatchers.Default) {
                val state = _uiState.value;
                val inputVal = state.newCreditInput.toBD();
                val results = mutableListOf<LoanOffer>()
                if (inputVal > BD_ZERO) {
                    FinancialConfig.PRODUCTS.forEach { (prodKey, config) ->
                        config.factorsTrad.keys.forEach { term ->
                            val calcData = FinancialEngine.calculate(
                                prodKey,
                                inputVal,
                                term,
                                state.isByAmount,
                                true,
                                BigDecimal("100")
                            )
                            if (calcData != null) {
                                val (res, tipo) = calcData;
                                val cat =
                                    if (tipo == "RETENCION") config.meta.catRet else config.meta.catTrad;
                                val minRequired = config.minTermMap[term]
                                    ?: config.minGlobal; if (res.amount >= minRequired && res.amount >= config.minGlobal) {
                                    results.add(
                                        LoanOffer(
                                            prodKey,
                                            res.amount,
                                            res.payment,
                                            term,
                                            mainValue = res.amount,
                                            mainLabel = "MONTO PRÉSTAMO",
                                            cat = cat,
                                            tipoTasa = tipo
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                results.sortedWith(compareByDescending<LoanOffer> { it.amount }.thenBy { it.monthlyPayment })
                    .mapIndexed { i, o -> o.copy(isBest = i == 0) }
            }
            if (calculatedOffers.isNotEmpty()) {
                soundManager.play("cash"); addXp(ctx); triggerConfetti()
            }
            _uiState.update { it.copy(isLoading = false, offersNew = calculatedOffers) }
        }
    }

    fun generateLayout(): String {
        val s = _uiState.value;
        val d = s.clientData;
        val p = s.userProfile;
        val o = s.selectedOfferForLayout ?: return ""
        return "DATOS ASESOR\n${p.name.uppercase()}\nID: ${p.username}\n\n*SOLICITUD ${o.product} (${o.tipoTasa})\nCLIENTE: ${d.name.uppercase()}\nNSS: ${d.nss}\nCURP: ${d.curp.uppercase()}\nRFC: ${d.rfc.uppercase()}\n\n*PROPUESTA\n${o.mainLabel}: $${o.mainValue.toCurrency()}\nMONTO: $${o.amount.toCurrency()}\nPLAZO: ${o.term} meses\nPAGO: $${o.monthlyPayment.toCurrency()}\nCAT: ${o.cat}\n\n*BANCO*\n${d.bank.uppercase()}\n${d.clabe}\n\nCreado con ConsuCalcu Pro"
    }

    fun generateTicketBackground(ctx: Context) {
        val o = _uiState.value.selectedOfferForLayout ?: return
        val c = _uiState.value.clientData
        val u = _uiState.value.userProfile
        viewModelScope.launch {
            TicketGenerator.generateAndShare(ctx, o, c, u.name)
        }
    }
}

// ==========================================
// 4. UI PRINCIPAL
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsuCalcuApp(
    viewModel: CalculatorViewModel = viewModel(),
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager(ctx) }

    // Estado para el menú desplegable
    var showMenu by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { 2 })
    LaunchedEffect(state.selectedTab) { pagerState.animateScrollToPage(state.selectedTab) }
    LaunchedEffect(pagerState.currentPage) {
        if (state.selectedTab != pagerState.currentPage) viewModel.setTab(
            pagerState.currentPage
        )
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
                    // --- TOP BAR MINIMALISTA ---
                    TopAppBar(
                        title = {
                            // Solo Texto, limpio y profesional
                            Text(
                                "ConsuCalcu",
                                style = MaterialTheme.typography.titleLarge, // Usamos la nueva fuente tight
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.offset(y = (-2).dp) // Pequeño ajuste óptico
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        actions = {
                            // 1. PÍLDORA DE ESTATUS (Todo en uno)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface, // Fondo sutil
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .height(32.dp)
                                    .clickable {
                                        // Easter egg: Al tocar la píldora, suena
                                        soundManager.play("pop")
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    // Fuego
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

                                    // Separador vertical pequeño
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

                                    // Nivel
                                    Text(
                                        "LVL ${state.userProfile.level}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = JuicyBlue
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            // 2. MENÚ DE TRES PUNTOS (Limpia el desorden)
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        Icons.Rounded.MoreVert,
                                        contentDescription = "Opciones",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // El contenido del menú
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
                                        leadingIcon = { Icon(Icons.Rounded.HelpOutline, null) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.resetTutorial()
                                        }
                                    )
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
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp, // Flat look moderno
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        ).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.Loop, null) },
                            label = { Text("Refinanciar", fontWeight = FontWeight.Bold) },
                            selected = state.selectedTab == 0,
                            onClick = {
                                soundManager.play("pop"); haptic.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            ); viewModel.setTab(0)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = JuicyBlue.copy(
                                    alpha = 0.15f
                                ),
                                selectedIconColor = JuicyBlue,
                                selectedTextColor = JuicyBlue,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.6f
                                )
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.AttachMoney, null) },
                            label = { Text("Nuevo", fontWeight = FontWeight.Bold) },
                            selected = state.selectedTab == 1,
                            onClick = {
                                soundManager.play("pop"); haptic.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            ); viewModel.setTab(1)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = JuicyGreen.copy(
                                    alpha = 0.15f
                                ),
                                selectedIconColor = JuicyGreen,
                                selectedTextColor = JuicyGreen,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.6f
                                )
                            )
                        )
                    }
                }
            ) { padding ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.padding(padding).fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { page ->
                    if (page == 0) RefinanceScreen(
                        viewModel,
                        state,
                        soundManager,
                        ctx
                    ) else NewCreditScreen(viewModel, state, soundManager, ctx)
                }
            }

            if (state.showClientForm) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.dismissForm() },
                    containerColor = MaterialTheme.colorScheme.surface
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

            if (state.showTutorial) {
                RPGDialogOverlay(
                    stepIndex = state.tutorialStep,
                    soundManager = soundManager,
                    onNext = { soundManager.play("pop"); viewModel.advanceTutorial() },
                    onFinish = { viewModel.closeTutorial(ctx) }
                )
            }

            if (state.showConfigDialog) ConfigDialog(viewModel, ctx, soundManager)
            if (state.showSuccessAnim) SuccessOverlay { viewModel.hideSuccess() }
            if (state.showConfetti) {
                KonfettiView(
                    parties = listOf(
                        Party(
                            speed = 0f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            spread = 360,
                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def).map { it },
                            position = Position.Relative(0.5, 0.3),
                            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
                        )
                    ), modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ==========================================
// 5. PANTALLAS (Refactorizadas con Shimmer & Pager)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefinanceScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    soundManager: SoundManager,
    ctx: Context
) {
    val haptic = LocalHapticFeedback.current
    val totalDebt = state.debts.sumOf { it.balance.toBD() };
    val totalPayment = state.debts.sumOf { it.payment.toBD() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            JuicyGradientCard(
                brush = Brush.horizontalGradient(
                    listOf(
                        JuicyBlue,
                        Color(0xFF1899D6)
                    )
                ), shadowColor = JuicyBlueShadow
            ) {
                Row(
                    Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "TOTAL DEUDA",
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
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 2.dp
            )
        }
        item {
            JuicyInput(
                label = "CAPACIDAD DISPONIBLE",
                value = state.capacityCNCA,
                onValChange = { viewModel.updateCapacityCNCA(it) },
                type = KeyboardType.Decimal, // <--- CAMBIAR A DECIMAL AQUÍ
                leadingIcon = Icons.Rounded.AccountBalanceWallet,
                isCurrency = true
            )
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

        // --- SKELETON / RESULTADOS ---
        if (state.isLoading) {
            items(3) {
                Box(
                    Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(20.dp))
                        .shimmerEffect()
                )
            }
        } else {
            items(state.offersCNCA) { offer ->
                ResultCard(offer) {
                    soundManager.play("pop"); viewModel.onSelectOffer(
                    offer
                )
                }
            }
            if (state.offersCNCA.isEmpty()) item { EmptyState() }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun NewCreditScreen(
    viewModel: CalculatorViewModel,
    state: CalculatorUiState,
    soundManager: SoundManager,
    ctx: Context
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            JuicyCard(
                color = MaterialTheme.colorScheme.surface,
                shadowColor = MaterialTheme.colorScheme.outline
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "COTIZADOR RÁPIDO",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(15.dp))

                    // Chips de Selección
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            JuicyChip(
                                selected = !state.isByAmount,
                                text = "Tengo Capacidad",
                                onClick = {
                                    soundManager.play("pop"); viewModel.toggleCalculationMode(
                                    false
                                )
                                })
                        }
                        Box(Modifier.weight(1f)) {
                            JuicyChip(
                                selected = state.isByAmount,
                                text = "Quiero Monto",
                                onClick = {
                                    soundManager.play("pop"); viewModel.toggleCalculationMode(
                                    true
                                )
                                })
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // LÓGICA DEL SLIDER vs INPUT NORMAL
                    if (state.isByAmount) {
                        // Modo: Monto del Préstamo (Usa Slider)
                        JuicySliderInput(
                            label = "¿CUÁNTO NECESITAS?",
                            value = state.newCreditInput,
                            range = 5000f..200000f, // Rango de $5k a $200k
                            steps = 38, // Saltos de $5k aprox
                            onValueChange = { viewModel.updateNewCreditInput(it) }
                        )
                    } else {
                        // Modo: Capacidad de Pago (Input Normal para precisión)
                        JuicyInput(
                            label = "¿CUÁNTO PUEDES PAGAR?",
                            value = state.newCreditInput,
                            onValChange = { viewModel.updateNewCreditInput(it) },
                            type = KeyboardType.Number,
                            leadingIcon = Icons.Outlined.Savings, // Icono de alcancía
                            isCurrency = true
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Botones de Acción
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

        // Resultados
        if (state.isLoading) {
            items(3) {
                Box(
                    Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(20.dp))
                        .shimmerEffect()
                )
            }
        } else {
            items(state.offersNew) { offer ->
                ResultCard(offer) {
                    soundManager.play("pop"); viewModel.onSelectOffer(
                    offer
                )
                }
            }
            if (state.offersNew.isEmpty()) item { EmptyState() }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ==========================================
// 6. COMPONENTES & DIALOGS (La parte que faltaba)
// ==========================================

@Composable
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
            .navigationBarsPadding() // Evita que el teclado tape botones
            .fillMaxWidth()
    ) {
        Text(
            "Datos del Cliente",
            style = JuicyTypography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Lista scrolleable para inputs en pantallas pequeñas
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                JuicyInput(
                    "Nombre",
                    d.name,
                    { viewModel.updateClientData(d.copy(name = it)) },
                    KeyboardType.Text,
                    Icons.Rounded.Person
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        JuicyInput(
                            "NSS",
                            d.nss,
                            { if (it.length <= 11) viewModel.updateClientData(d.copy(nss = it)) },
                            KeyboardType.Number,
                            Icons.Rounded.Numbers
                        )
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
                            "RFC",
                            d.rfc,
                            { if (it.length <= 13) viewModel.updateClientData(d.copy(rfc = it)) },
                            KeyboardType.Text,
                            Icons.Rounded.Badge
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        JuicyInput(
                            "Teléfono",
                            d.phone,
                            { if (it.length <= 10) viewModel.updateClientData(d.copy(phone = it)) },
                            KeyboardType.Phone,
                            Icons.Rounded.Phone
                        )
                    }
                }
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
            item {
                JuicyInput(
                    "CLABE",
                    d.clabe,
                    { if (it.length <= 18) viewModel.updateClientData(d.copy(clabe = it)) },
                    KeyboardType.Number,
                    Icons.Rounded.CreditCard
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botones de acción
        if (o != null) {
            JuicyButton(
                text = "🎫 GENERAR TICKET (JPG)",
                color = JuicyPurple,
                shadowColor = JuicyPurpleShadow,
                textColor = Color.White,
                onClick = onGenerateTicket
            )
            Spacer(Modifier.height(8.dp))
        }
        JuicyButton(
            text = "📋 COPIAR TEXTO",
            color = JuicyBlue,
            shadowColor = JuicyBlueShadow,
            textColor = Color.White,
            onClick = { onGenerate(viewModel.generateLayout()) })
    }
}

@Composable
fun JuicyButton(
    text: String,
    color: Color,
    shadowColor: Color,
    textColor: Color,
    height: Dp = 48.dp,
    icon: ImageVector? = null,
    iconColor: Color? = null,
    soundManager: SoundManager? = null,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.height(height).fillMaxWidth().bouncyClick(soundManager, onClick)) {
        Box(
            modifier = Modifier.fillMaxSize().padding(top = 4.dp)
                .background(shadowColor, RoundedCornerShape(16.dp))
        )
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 4.dp)
                .background(color, RoundedCornerShape(16.dp))
                .border(2.dp, shadowColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        icon,
                        null,
                        tint = iconColor ?: textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text,
                    color = textColor,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
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
    size: Dp = 40.dp,
    soundManager: SoundManager? = null,
    onClick: () -> Unit
) {
    val realShadow = shadowColor ?: color.copy(alpha = 0.5f)
    Box(modifier = Modifier.size(size).bouncyClick(soundManager, onClick)) {
        Box(
            modifier = Modifier.fillMaxSize().padding(top = 2.dp)
                .background(realShadow, CircleShape)
        )
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 2.dp)
                .background(color, CircleShape).border(2.dp, realShadow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(size * 0.6f))
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
    isCurrency: Boolean = false
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                // VALIDACIÓN CORREGIDA: Permite dígitos Y un solo punto decimal
                if (type == KeyboardType.Number || type == KeyboardType.Decimal) {
                    if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                        onValChange(input)
                    }
                } else {
                    onValChange(input)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            // Aseguramos que el teclado muestre el punto decimal
            keyboardOptions = KeyboardOptions(keyboardType = if (isCurrency) KeyboardType.Decimal else type),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(leadingIcon, null, tint = MaterialTheme.colorScheme.outline) },
            visualTransformation = if (isCurrency) CurrencyVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JuicyBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
fun JuicyCard(color: Color, shadowColor: Color, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
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
fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(40.dp)
    ) {
        Icon(
            Icons.Rounded.SearchOff,
            null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Sin Resultados",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SuccessOverlay(onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(
            0f,
            1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) { value, _ -> scale = value }
        delay(1500)
        onDismiss()
    }
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.scale(scale).size(200.dp)
                    .background(JuicyGreen, RoundedCornerShape(40.dp))
                    .border(4.dp, Color.White, RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.Check,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        "¡LISTO!",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ResultCard(offer: LoanOffer, onClick: () -> Unit) {
    val color = if (offer.isBest) JuicyOrange else MaterialTheme.colorScheme.surface
    val shadow = if (offer.isBest) JuicyOrangeShadow else MaterialTheme.colorScheme.outline
    val contentColor = if (offer.isBest) Color.White else MaterialTheme.colorScheme.onSurface

    JuicyCard(color = color, shadowColor = shadow) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            // Cabecera: Producto y Estrella
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${offer.product} • ${offer.tipoTasa}",
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                    fontSize = 10.sp
                )
                if (offer.isBest) {
                    Icon(Icons.Rounded.Star, null, tint = Color.Yellow)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Etiqueta Principal (Monto o Remanente)
            Text(
                text = offer.mainLabel,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f)
            )

            // VALOR PRINCIPAL (Aquí ocurre la magia Monospace)
            Text(
                text = "$${offer.mainValue.toCurrency()}",
                style = MaterialTheme.typography.headlineMedium, // Usa la nueva fuente numérica
                fontWeight = FontWeight.Black,
                color = contentColor
            )

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = contentColor.copy(alpha = 0.2f)
            )

            // Detalles Inferiores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MENSUALIDAD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$${offer.monthlyPayment.toCurrency()}",
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor,
                        fontSize = 16.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PLAZO / CAT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${offer.term} Mes / ${offer.cat}",
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor,
                        fontSize = 16.sp
                    )
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
                        Icons.Rounded.ReceiptLong,
                        null,
                        tint = JuicyBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                DropdownCompact(
                    label = "Financiera",
                    options = FinancialConfig.PRODUCTS.keys.toList(),
                    selectedValue = debt.product,
                    modifier = Modifier.weight(1f)
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
                    label = "SALDO",
                    modifier = Modifier.weight(1f),
                    inputType = KeyboardType.Decimal,
                    isCurrency = true
                ) { onUpdate(debt.copy(balance = it)) }
                CompactInput(
                    value = debt.payment,
                    label = "PAGO MES",
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
                    modifier = Modifier.weight(1f)
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
                // MISMA VALIDACIÓN CORREGIDA AQUÍ
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
            // Forzamos teclado decimal si es moneda
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
    onSelectionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth().height(50.dp),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = JuicyBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, fontWeight = FontWeight.Bold) },
                        onClick = { onSelectionChange(item); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigDialog(viewModel: CalculatorViewModel, ctx: Context, soundManager: SoundManager) {
    val state by viewModel.uiState.collectAsState()
    var tempName by remember { mutableStateOf(state.userProfile.name) }
    var tempUser by remember { mutableStateOf(state.userProfile.username) }
    var tempPwd by remember { mutableStateOf(state.userProfile.password) }
    var tempSup by remember { mutableStateOf(state.userProfile.supervisor) }

    Dialog(onDismissRequest = { viewModel.toggleConfigDialog(false) }) {
        JuicyCard(
            color = MaterialTheme.colorScheme.surface,
            shadowColor = MaterialTheme.colorScheme.outline
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "PERFIL ASESOR",
                    style = MaterialTheme.typography.titleLarge,
                    color = JuicyBlue
                )
                Spacer(Modifier.height(20.dp))
                JuicyInput(
                    "Nombre",
                    tempName,
                    { tempName = it },
                    KeyboardType.Text,
                    Icons.Rounded.Person
                )
                Spacer(Modifier.height(10.dp))
                JuicyInput(
                    "Usuario",
                    tempUser,
                    { tempUser = it },
                    KeyboardType.Text,
                    Icons.Rounded.Badge
                )
                Spacer(Modifier.height(20.dp))
                JuicyButton(
                    text = "GUARDAR",
                    color = JuicyBlue,
                    shadowColor = JuicyBlueShadow,
                    textColor = Color.White,
                    soundManager = soundManager
                ) {
                    viewModel.saveProfile(
                        ctx,
                        UserProfile(tempName, tempUser, tempPwd, tempSup)
                    )
                }
            }
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
            Icon(
                steps[currentStep].icon,
                null,
                tint = JuicyBlue,
                modifier = Modifier.size(100.dp)
            )
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
    onValueChange: (String) -> Unit
) {
    // Convertimos String a Float para el Slider
    val floatValue = value.replace(",", "").replace("$", "").toFloatOrNull() ?: range.start

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        JuicyInput(
            label = label,
            value = value,
            onValChange = onValueChange,
            type = KeyboardType.Number,
            leadingIcon = Icons.Rounded.AttachMoney,
            isCurrency = true
        )

        Spacer(Modifier.height(4.dp))

        Slider(
            value = floatValue,
            onValueChange = { newVal ->
                // Convertimos el Float del slider de vuelta a String entero
                onValueChange(newVal.toInt().toString())
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

// ==========================================
// 7. BUDDY TUTORIAL SYSTEM (RPG OVERLAY)
// ==========================================

data class RPGStep(val text: String, val focusArea: String)

@Composable
fun RPGDialogOverlay(
    stepIndex: Int,
    soundManager: SoundManager, // Recibimos el SoundManager
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val script = listOf(
        RPGStep(
            "¡Hola Asesor! Soy Buddy 🐻. ¿Listo para vender más? Te enseñaré cómo funciona esto.",
            "Intro"
        ),
        RPGStep(
            "Abajo tienes 'Refinanciar' para comprar deudas (CNCA) y 'Nuevo' para créditos normales.",
            "Tabs"
        ),
        RPGStep(
            "Yo calculo TODO automáticamente: redondeo SIPRE, mejora de tasa y liquidez. ¡Sin errores!",
            "Intelligence"
        ),
        RPGStep(
            "Elige una oferta y generaré un texto mágico listo para WhatsApp. ¡A ganar comisiones!",
            "End"
        )
    )

    if (stepIndex >= script.size) {
        LaunchedEffect(Unit) { onFinish() }
        return
    }

    val currentData = script[stepIndex]
    var displayedText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(true) }

    // --- Lógica de Mecanografía Animal Crossing ---
    LaunchedEffect(currentData.text, stepIndex) {
        isTyping = true
        displayedText = ""
        delay(300) // Pequeña pausa antes de empezar

        currentData.text.forEachIndexed { _, char ->
            displayedText += char

            // Si NO es un espacio, reproduce el "blip"
            if (char != ' ') {
                soundManager.play("villager")
            }

            // Velocidad de escritura (40ms es un buen ritmo)
            delay(40)
        }
        isTyping = false
    }
    // ----------------------------------------------

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

                    // CÍRCULO DE LA MASCOTA
                    Box(
                        modifier = Modifier
                            .offset(y = bounce.dp)
                            .size(80.dp)
                            .background(JuicyBlue, CircleShape)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // 👇 AQUÍ VA TU IMAGEN REAL
                        // Asegúrate de que tu archivo se llame tal cual, ej: R.drawable.buddy_face
                        // Si no tienes la imagen cargada aun, usa Icon(Icons.Rounded.EmojiEmotions...)
                        Image(
                            painter = painterResource(id = R.drawable.avatar_mascot), // <--- CAMBIA ESTO SI TU ARCHIVO SE LLAMA DIFERENTE
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
