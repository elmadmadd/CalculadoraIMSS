package com.example.consucalcu.ui.components

import com.example.consucalcu.R
import com.example.consucalcu.ConsuNavy
import com.example.consucalcu.ConsuOrange
import com.example.consucalcu.ConsuChipBg
import android.content.Context
import androidx.compose.foundation.shape.CircleShape
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.consucalcu.*
import com.example.consucalcu.model.*
import com.example.consucalcu.manager.HapticManager
import com.example.consucalcu.manager.SoundManager
import com.example.consucalcu.data.PreferenceRepository
import com.example.consucalcu.utils.*
import java.math.BigDecimal
import java.math.RoundingMode

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

        val formattedInt = integerPart.toLongOrNull()?.let {
            java.text.DecimalFormat("#,###").format(it)
        } ?: integerPart

        val formatted = buildString {
            append("$")
            append(formattedInt)
            if (decimalPart != null) {
                append(".")
                append(decimalPart)
            }
        }

        val newText = AnnotatedString(formatted)
        val newTextString = newText.text

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val dataChars = originalText.take(offset).count { it.isDigit() || it == '.' }
                var transIdx = 0
                var dataCount = 0
                while (transIdx < newTextString.length && dataCount < dataChars) {
                    val c = newTextString[transIdx]
                    if (c.isDigit() || c == '.') {
                        dataCount++
                    }
                    transIdx++
                }
                return transIdx
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
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
    val haptic = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f, // Un poco más profundo el presionado
        animationSpec = spring(
            dampingRatio = 0.5f, // Más elástico al soltar
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                
                // Feedback táctil inmediato al bajar
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                
                val up = waitForUpOrCancellation()
                isPressed = false
                
                if (up != null) {
                    soundManager?.play("pop")
                    // Feedback táctil al confirmar
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            }
        }
}

// =================================================================================================
// 4. HAPTICS SYSTEM
// =================================================================================================
fun triggerHapticTexture(context: Context, pattern: LongArray, useComposition: Boolean = true) {
    // Check global preference before anything else
    val repository = PreferenceRepository(context)
    if (!repository.getIsHapticEnabled()) return

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
                // Usar diferentes primitivas según el patrón para que se "sienta" la diferencia
                if (pattern.contentEquals(HapticPatterns.SUCCESS)) {
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f, 0)
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.8f, 100)
                } else if (pattern.contentEquals(HapticPatterns.ERROR)) {
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 0.8f, 0)
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 1.0f, 150)
                } else {
                    // Click genérico
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f, 0)
                }
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

// =================================================================================================
// 14. REUSABLE COMPONENTS
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
fun FormSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
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
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        JuicyCard(
            color = MaterialTheme.colorScheme.surface,
            shadowColor = MaterialTheme.colorScheme.outline.copy(alpha=0.04f)
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
fun JuicyInput(
    label: String,
    value: String,
    onValChange: (String) -> Unit,
    type: KeyboardType,
    leadingIcon: ImageVector,
    isCurrency: Boolean = false,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    placeholder: String? = null,
    maxLength: Int? = null
) {
    val smartPlaceholder = placeholder ?: when {
        isCurrency -> "10,000"
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
    
    val focusManager = LocalFocusManager.current
    val hapticManager = LocalHapticManager.current
    var isFocused by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) 1.01f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "inputScale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> ErrorRed
            isFocused -> ConsuOrange
            else -> if (isSystemInDarkTheme()) BorderDark else ConsuNavy.copy(alpha = 0.8f)
        },
        animationSpec = tween(300),
        label = "borderColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 1.5.dp else 1.dp,
        label = "borderWidth"
    )
    
    Column(modifier = Modifier.graphicsLayer { 
        scaleX = animatedScale
        scaleY = animatedScale
    }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) ErrorRed else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            
            if (maxLength != null && isFocused) {
                val remaining = maxLength - value.length
                Text(
                    text = "$remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (remaining < 5) WarningOrange else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = if (remaining < 5) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                )
            }
        }
        
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
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
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSystemInDarkTheme()) Color(0xFF1E293B) else ConsuChipBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        leadingIcon,
                        null,
                        tint = if (isError) ErrorRed else if (isFocused) ConsuOrange else ConsuNavy,
                        modifier = Modifier.size(18.dp)
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = { input ->
                        if (input != value) {
                            hapticManager?.tick()
                        }
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
                
                if (isCurrency) {
                    Text(
                        text = "MXN",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                } else if (isError) {
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
                if (snappedValue.toFloat() != lastSnappedValue) {
                    hapticManager?.tick()
                    lastSnappedValue = snappedValue.toFloat()
                }
                onValueChange(snappedValue.toString())
            },
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1C1C1E),
                activeTrackColor = Color(0xFF1C1C1E),
                inactiveTrackColor = Color(0xFF1C1C1E).copy(alpha = 0.2f)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuicyProviderDropdown(
    selectedValue: String,
    onSelectionChange: (String) -> Unit
) {
    val brands = listOf("CONSUBANCO", "CONSUPAGO", "OPCIPRES", "MASNOMINA")
    val variantMap = mapOf(
        "CONSUBANCO" to listOf("TRADICIONAL", "ESPECIAL", "VIP"),
        "CONSUPAGO"  to listOf("TRADICIONAL", "ESPECIAL"),
        "OPCIPRES"   to listOf("TRADICIONAL", "ESPECIAL"),
        "MASNOMINA"  to emptyList()
    )

    val selectedBrand   = brands.firstOrNull { selectedValue.startsWith(it) } ?: "CONSUBANCO"
    val selectedVariant = if (selectedValue.contains(" ")) selectedValue.removePrefix("$selectedBrand ") else ""

    var expanded by remember { mutableStateOf(false) }

    fun getBrandColor(name: String): Color = when (name) {
        "CONSUBANCO" -> Color(0xFF1E3A8A) // Blue
        "CONSUPAGO"  -> Color(0xFFD97706) // Orange/Amber
        "OPCIPRES"   -> Color(0xFF059669) // Green/Emerald
        "MASNOMINA"  -> Color(0xFFDC2626) // Red
        else         -> Color.Gray
    }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Surface(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(getBrandColor(selectedBrand))
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (selectedVariant.isNotEmpty()) "$selectedBrand ($selectedVariant)" else selectedBrand,
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                brands.forEach { brand ->
                    val variants = variantMap[brand] ?: emptyList()
                    if (variants.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(getBrandColor(brand)))
                                    Spacer(Modifier.width(10.dp))
                                    Text(brand, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                onSelectionChange(brand)
                                expanded = false
                            }
                        )
                    } else {
                        variants.forEach { variant ->
                            val fullName = "$brand $variant"
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(getBrandColor(brand)))
                                        Spacer(Modifier.width(10.dp))
                                        Text("$brand ($variant)", fontWeight = FontWeight.Medium)
                                    }
                                },
                                onClick = {
                                    onSelectionChange(fullName)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JuicyCard(
    color: Color, 
    shadowColor: Color, 
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = color,
        border = BorderStroke(1.dp, shadowColor.copy(alpha = 0.12f)),
        shadowElevation = 2.dp
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
    val contentColor = if (selected) {
        if (isDark) Color.Black else Color.White
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.padding(end = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

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
                        color = SuccessGreen
                    )
                }
            }

            ChartBar(
                label = "NUEVO",
                value = newPayment,
                maxValue = totalMax,
                color = if (newPayment < currentPayment) SuccessGreen else WarningOrange,
                isNew = true
            )
        }
        
        if (ahorro > BigDecimal.ZERO) {
            Spacer(Modifier.height(20.dp))
            Surface(
                color = SuccessGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Savings, 
                        null, 
                        tint = SuccessGreen, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "¡Libera $${ahorro.toCurrency()} al mes!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = SuccessGreen
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
            "$${value.toInt()}",
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

@Composable
fun JuicyButton(
    text: String,
    color: Color,
    shadowColor: Color,
    textColor: Color,
    height: androidx.compose.ui.unit.Dp = 56.dp,
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
    size: androidx.compose.ui.unit.Dp = 56.dp,
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
fun NSSValidationIndicator(nss: String) {
    if (nss.length == 11) {
        val isValid = nss.isValidNSS()
        Icon(
            imageVector = if (isValid) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
            contentDescription = if (isValid) "NSS Válido" else "NSS Inválido",
            tint = if (isValid) SuccessGreen else ErrorRed,
            modifier = Modifier.size(24.dp)
        )
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

    // ordenar las marcas según su mejor oferta bajo el sortMode actual
    @Suppress("UNCHECKED_CAST")
    val sortedBrands = rawGrouped.entries.sortedByDescending { (_, brandOffers) ->
        val best = sortList(brandOffers).firstOrNull()
        best?.sortKey(sortMode) as? Comparable<Any>
    }.map { it.key }

    val hasPremiumOffers = offers.any { isVariantPremium(it.product) }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

        // ── Etiqueta + Toggle Premium ──
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
            // ── Chips de Orden ──
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

            // ── Grupos de marcas ──
            sortedBrands.forEachIndexed { brandIdx, brand ->
                val brandOffers = rawGrouped[brand] ?: return@forEachIndexed
                val color       = brandColor(brand)
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

                    // Renderizar Ofertas Unificadas
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
            Text(
                text = offer.product.take(4).uppercase(),
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
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
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
                                val widthFraction = term / 60f
                                Box(
                                    modifier = Modifier
                                        .height(4.dp)
                                        .width(40.dp * widthFraction)
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



