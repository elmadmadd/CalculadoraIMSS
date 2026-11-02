package com.example.consucalcu.ui.components
import com.example.consucalcu.ui.theme.ConsuRadius
import com.example.consucalcu.ui.theme.ConsuSpacing

import com.example.consucalcu.R
import androidx.compose.foundation.shape.CircleShape
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.geometry.Offset
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
import com.example.consucalcu.data.FinancialConfig
import com.example.consucalcu.data.PreferenceRepository
import com.example.consucalcu.utils.*
import java.math.BigDecimal
import java.math.RoundingMode
import android.content.Context

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

fun Modifier.bouncyClick(onClick: () -> Unit) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticManager = LocalHapticManager.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
        ) {
            hapticManager?.medium()
            onClick()
        }
}

/**
 * Shimmer Effect for world-class skeleton loading
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerAnim"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    this.background(brush)
}

// =================================================================================================
// 4. HAPTICS SYSTEM
// =================================================================================================
fun triggerHapticTexture(context: Context, pattern: LongArray, useComposition: Boolean = true) {
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
                if (pattern.contentEquals(HapticPatterns.SUCCESS)) {
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f, 0)
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.8f, 100)
                } else if (pattern.contentEquals(HapticPatterns.ERROR)) {
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 0.8f, 0)
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 1.0f, 150)
                } else {
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f, 0)
                }
                it.vibrate(composition.compose())
            } catch (e: Exception) {
                val effect = VibrationEffect.createWaveform(pattern, -1)
                it.vibrate(effect)
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
                modifier = Modifier.padding(ConsuSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ConsuSpacing.lg)
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
    maxLength: Int? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onImeDone: (() -> Unit)? = null,
) {
    val smartPlaceholder = placeholder ?: when {
        isCurrency -> "Ej. 10,000"
        label.contains("NSS", ignoreCase = true) -> "12345678901"
        label.contains("Teléfono", ignoreCase = true) || label.contains("Celular", ignoreCase = true) -> "5512345678"
        label.contains("Nombre", ignoreCase = true) -> "Juan Pérez"
        label.contains("Monto", ignoreCase = true) -> "50000"
        label.contains("Plazo", ignoreCase = true) || label.contains("Meses", ignoreCase = true) -> "60"
        label.contains("Edad", ignoreCase = true) -> "Ej. 35"
        label.contains("Antigüedad", ignoreCase = true) -> "10"
        type == KeyboardType.Number || type == KeyboardType.Decimal -> "0"
        else -> "Ingresa $label"
    }

    val hapticManager = LocalHapticManager.current
    var isFocused by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "inputScale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
        },
        animationSpec = tween(300),
        label = "borderColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = 1.dp,
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
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (maxLength != null && isFocused) {
                val remaining = maxLength - value.length
                Text(
                    text = "$remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (remaining < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
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
                    .heightIn(min = 56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(ConsuRadius.lg)
                    )
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = RoundedCornerShape(ConsuRadius.lg)
                    )
                    .clip(RoundedCornerShape(ConsuRadius.lg))
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
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        leadingIcon,
                        null,
                        tint = if (isError) MaterialTheme.colorScheme.error else if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (onImeDone != null) onImeDone() else defaultKeyboardAction(ImeAction.Done)
                        }
                    ),
                    singleLine = true,
                    visualTransformation = if (isCurrency) CurrencyVisualTransformation() else visualTransformation,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    smartPlaceholder,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                val isValidField = remember(value, label) {
                    when {
                        label.contains("CURP", true) -> GeneralUtils.isValidCURP(value)
                        label.contains("RFC", true) -> GeneralUtils.isValidRFC(value)
                        label.contains("CLABE", true) -> GeneralUtils.isValidCLABE(value)
                        label.contains("Correo", true) -> GeneralUtils.isValidEmail(value)
                        label.contains("NSS", true) -> value.isValidNSS()
                        else -> value.isNotBlank()
                    }
                }

                if (isValidField && value.isNotEmpty() && !isCurrency) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(ConsuSuccess.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            null,
                            tint = ConsuSuccess,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else if (isError) {
                     Icon(
                        Icons.Rounded.Error,
                        null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 12.dp).size(20.dp)
                     )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuicyProviderDropdown(
    selectedValue: String,
    onSelectionChange: (String) -> Unit
) {
    val products = FinancialConfig.IMSS_PRODUCT_KEYS.toList()
    val selectedProduct = selectedValue.takeIf { it in products } ?: "CSB"

    var expanded by remember { mutableStateOf(false) }

    val csbBrandColor = MaterialTheme.colorScheme.onSurface
    fun getBrandColor(name: String): Color = when (name) {
        "CSB" -> csbBrandColor
        "OPC" -> ConsuSuccess
        else         -> Color.Gray
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "PRODUCTO",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Surface(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                shape = RoundedCornerShape(ConsuRadius.lg),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f))
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
                                .background(getBrandColor(selectedProduct.substringBefore(" ")))
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = selectedProduct,
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
                products.forEach { product ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(getBrandColor(product.substringBefore(" "))))
                                Spacer(Modifier.width(10.dp))
                                Text(product, fontWeight = FontWeight.Bold)
                            }
                        },
                        onClick = {
                            onSelectionChange(product)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun JuicyCard(
    color: Color,
    shadowColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = color,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)),
        shadowElevation = 0.dp
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
            .background(brush, RoundedCornerShape(ConsuRadius.lg))
            .border(1.dp, shadowColor.copy(alpha = 0.2f), RoundedCornerShape(ConsuRadius.lg))
    ) {
        content()
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
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
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
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                if (ahorro > BigDecimal.ZERO) {
                    Text(
                        "-${porcentajeAhorro.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = ConsuSuccess
                    )
                }
            }

            ChartBar(
                label = "NUEVO",
                value = newPayment,
                maxValue = totalMax,
                color = if (newPayment < currentPayment) ConsuSuccess else ConsuWarning,
                isNew = true
            )
        }

        if (ahorro > BigDecimal.ZERO) {
            Spacer(Modifier.height(20.dp))
            Surface(
                color = ConsuSuccess.copy(alpha = 0.12f),
                shape = RoundedCornerShape(ConsuRadius.md),
                border = BorderStroke(1.dp, ConsuSuccess.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(ConsuSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Savings,
                        null,
                        tint = ConsuSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "¡Libera $${ahorro.toCurrency()} al mes!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = ConsuSuccess
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
                .clip(RoundedCornerShape(topStart = ConsuRadius.badge, topEnd = ConsuRadius.badge))
                .background(color)
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
    Column(verticalArrangement = Arrangement.spacedBy(ConsuSpacing.lg)) {
        repeat(count) {
            JuicyCard(
                color = MaterialTheme.colorScheme.surface,
                shadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
            ) {
                Column(Modifier.padding(ConsuSpacing.xl)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(Modifier.width(100.dp).height(20.dp).clip(RoundedCornerShape(ConsuRadius.xs)).shimmerEffect())
                        Box(Modifier.width(60.dp).height(20.dp).clip(RoundedCornerShape(ConsuRadius.xs)).shimmerEffect())
                    }
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(ConsuRadius.sm)).shimmerEffect())
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.md)) {
                        Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(ConsuRadius.sm)).shimmerEffect())
                        Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(ConsuRadius.sm)).shimmerEffect())
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
    shadowColor: Color = Color.Transparent,
    textColor: Color,
    height: androidx.compose.ui.unit.Dp = 56.dp,
    icon: ImageVector? = null,
    iconColor: Color? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = height)
            .fillMaxWidth()
            .bouncyClick {
                if (!isLoading) {
                    onClick()
                }
            }
            .background(color, RoundedCornerShape(ConsuRadius.badge))
            .border(
                width = 1.dp,
                color = if (color == Color.Transparent) MaterialTheme.colorScheme.outline.copy(alpha=0.3f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(ConsuRadius.badge)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
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
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun JuicyIconButton(
    icon: ImageVector,
    contentDescription: String?,
    color: Color,
    shadowColor: Color? = null,
    iconColor: Color,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .bouncyClick(onClick = onClick)
            .background(color, RoundedCornerShape(ConsuRadius.badge))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(ConsuRadius.badge)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
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
            tint = if (isValid) ConsuSuccess else MaterialTheme.colorScheme.error,
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
            fontSize = 12.sp,
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
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
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
            prefix = null,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(ConsuRadius.md)
        )
    }
}

// =================================================================================================
// SEMÁFORO DE OFERTAS - Sistema de colores por ranking
// =================================================================================================
enum class SortMode(val label: String) {
    MEJOR_VALOR("Mejor valor"),
    MAYOR_MONTO("Mayor monto"),
    MENOR_DESCUENTO("Menor descuento"),
    MAYOR_PLAZO("Mayor plazo")
}

fun LoanOffer.sortKey(mode: SortMode): Comparable<*> = when (mode) {
    SortMode.MEJOR_VALOR    -> amount.divide(monthlyPayment.max(BigDecimal.ONE), 4, java.math.RoundingMode.HALF_UP)
    SortMode.MAYOR_MONTO    -> amount
    SortMode.MENOR_DESCUENTO -> monthlyPayment.negate()
    SortMode.MAYOR_PLAZO    -> BigDecimal(term)
}

@Composable
fun CurrencyText(
    value: BigDecimal,
    style: TextStyle,
    color: Color = Color.Unspecified
) {
    // Sin animación: una captura nunca debe salir con una cifra a medio camino.
    Text(
        text = "$${value.setScale(2, RoundingMode.HALF_UP).toCurrency()}",
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
    emptyContent: @Composable () -> Unit,
    onCompare: (() -> Unit)? = null,
    showRates: Boolean = true,
    onToggleRates: (() -> Unit)? = null
) {
    val allBrands = buildList {
        addAll(FinancialConfig.IMSS_PRODUCTS_BY_BRAND.keys)
        if (offers.any { it.agreementId == FinancialConfig.AGREEMENT_IMSS_BIENESTAR }) add("CONSUBANCO")
    }
    var sortMode by remember { mutableStateOf(SortMode.MEJOR_VALOR) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    @Composable
    fun brandColor(brand: String): Color = when (brand) {
        "CSB" -> MaterialTheme.colorScheme.onSurface
        "OPC" -> ConsuSuccess
        "CONSUBANCO" -> MaterialTheme.colorScheme.onSurface
        else         -> MaterialTheme.colorScheme.primary
    }

    fun isVariantPremium(name: String) = FinancialConfig.isPremiumProduct(name)

    @Suppress("UNCHECKED_CAST")
    fun sortList(list: List<LoanOffer>): List<LoanOffer> =
        list.sortedByDescending { it.sortKey(sortMode) as Comparable<Any> }

    val visibleOffers = if (showPremium) offers else offers.filter { !isVariantPremium(it.product) }

    val rawGrouped = allBrands.associateWith { brand ->
        visibleOffers.filter { it.product.startsWith(brand) }
    }.filter { it.value.isNotEmpty() }

    @Suppress("UNCHECKED_CAST")
    val sortedBrands = rawGrouped.entries.sortedByDescending { (_, brandOffers) ->
        val best = sortList(brandOffers).firstOrNull()
        best?.sortKey(sortMode) as? Comparable<Any>
    }.map { it.key }

    val hasPremiumOffers = offers.any { isVariantPremium(it.product) }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

        if (offers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = ConsuSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RESULTADOS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                    )
                    Text(
                        text = "${visibleOffers.size} oferta${if (visibleOffers.size != 1) "s" else ""}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                if (onToggleRates != null) {
                    // Atajo antes de tomar captura: muestra u oculta tasa y CAT.
                    IconButton(onClick = onToggleRates) {
                        Icon(
                            if (showRates) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = if (showRates) "Ocultar tasa y CAT" else "Mostrar tasa y CAT",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (onCompare != null && visibleOffers.size > 1) {
                    // Acción secundaria: no compite con COTIZAR.
                    TextButton(
                        onClick = onCompare,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Comparar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                }
            }
        }

        if (rawGrouped.isEmpty()) {
            emptyContent()
        } else {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = ConsuSpacing.lg)
            ) {
                if (hasPremiumOffers) {
                    FilterChip(
                        selected = showPremium,
                        onClick = onTogglePremium,
                        label = {
                            Text(
                                "MAX · ESP · VIP",
                                fontSize = 11.sp,
                                fontWeight = if (showPremium) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (showPremium) Icons.Rounded.Check else Icons.Rounded.Add,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        // Filtro neutro: el color se reserva para las tarjetas y la navegación.
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                            selectedLabelColor      = MaterialTheme.colorScheme.onSurface,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                            containerColor          = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor              = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            iconColor               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = showPremium,
                            borderColor         = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            selectedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            selectedBorderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(50)
                    )
                }
                Spacer(Modifier.weight(1f))
                Box {
                    TextButton(
                        onClick = { sortMenuOpen = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        contentPadding = PaddingValues(start = 8.dp, end = 4.dp)
                    ) {
                        Text("Ordenar por: ${sortMode.label}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Icon(
                            Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuOpen,
                        onDismissRequest = { sortMenuOpen = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        SortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        mode.label,
                                        fontWeight = if (mode == sortMode) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    sortMode = mode
                                    sortMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            sortedBrands.forEachIndexed { brandIdx, brand ->
                val brandOffers = rawGrouped[brand] ?: return@forEachIndexed
                val color       = brandColor(brand)
                val sortedOffers = sortList(brandOffers)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = ConsuSpacing.xxl)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = ConsuSpacing.md)
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
                            text       = "${brandOffers.size} ${if (brandOffers.size == 1) "opción" else "opciones"}",
                            fontSize   = 12.sp,
                            color      = color.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

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
                                isPremium          = isPremiumElement,
                                showRates          = showRates
                            )
                        }
                        if (i < sortedOffers.lastIndex) Spacer(Modifier.height(ConsuSpacing.md))
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
    isPremium: Boolean = false,
    showRates: Boolean = true
) {
    // IMSS Bienestar uses a compact information hierarchy because its offers
    // contain both months and quincenas. Keep the existing card unchanged for
    // every other agreement to avoid changing established workflows.
    if (offer.agreementId == FinancialConfig.AGREEMENT_IMSS_BIENESTAR) {
        ImssBienestarResultCard(
            offer = offer,
            onClick = onClick,
            onShowAmortization = onShowAmortization,
            rankIndex = rankIndex,
            accentColor = accentColor,
            isPremium = isPremium
        )
        return
    }

    val isBest = rankIndex == 0 || offer.isBest

    val variant = when {
        offer.product.endsWith("VIP")      -> "VIP"
        offer.product.endsWith("MAX")      -> "MAX"
        offer.product.endsWith("ESP")      -> "ESP"
        else                               -> null
    }

    // Especiales y VIP resaltan con el color de su marca: CSB naranja, OPC verde.
    val isCsbPremium = offer.product.startsWith("CSB")
    val premiumColor = when {
        !isPremium   -> null
        isCsbPremium -> ConsuOrange
        else         -> ConsuSuccess
    }
    val variantColor = when {
        variant == null -> null
        isCsbPremium    -> ConsuOrangeDark
        else            -> ConsuSuccessDark
    }

    // Sobre fondo oscuro el tono oscuro de la marca pierde contraste.
    val isDarkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // El monto es la cifra principal; la mensualidad queda como dato secundario.
    val isRemnant = offer.amount > offer.mainValue
    val amountLabel = if (isRemnant) "REMANENTE" else "MONTO A RECIBIR"
    val mutedText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = premiumColor?.copy(alpha = 0.10f)?.compositeOver(MaterialTheme.colorScheme.surface)
            ?: MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(ConsuRadius.xl),
        border = BorderStroke(
            width = when {
                isPremium -> 1.5.dp
                isBest    -> 1.dp
                else      -> 0.5.dp
            },
            color = when {
                premiumColor != null -> premiumColor.copy(alpha = 0.65f)
                isBest               -> ConsuOrange.copy(alpha = 0.5f)
                else                 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            }
        ),
        shadowElevation = if (isBest || isPremium) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = ConsuSpacing.lg, vertical = ConsuSpacing.md + 2.dp)
        ) {
            // ── Encabezado: producto, condiciones y distintivo ──
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val baseName = offer.product
                        .replace(" TRADICIONAL", "")
                        .replace(" ESPECIAL", "")
                        .replace(" MAX", "")
                        .replace(" ESP", "")
                        .replace(" VIP", "")
                    Text(
                        text = baseName,
                        fontWeight = FontWeight.Black,
                        color = (if (isDarkSurface) premiumColor else variantColor) ?: accentColor,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                    if (showRates) {
                        Text(
                            text = if (offer.tipoTasa == "RETENCION") {
                                "Tasa preferencial · CAT ${offer.cat}"
                            } else {
                                "Tasa mensual ${offer.tipoTasa} · CAT ${offer.cat}"
                            },
                            fontWeight = FontWeight.Medium,
                            color = mutedText,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // La variante va en color sólido; la mejor oferta, siempre en naranja suave.
                val badges = buildList {
                    if (variant != null && variantColor != null) add(Triple(variant, variantColor, if (variant == "VIP") Icons.Rounded.AutoAwesome else Icons.Rounded.Star))
                    if (isBest) add(Triple("MEJOR OPCIÓN", ConsuOrange, Icons.Rounded.Star))
                }
                badges.forEach { (badgeText, badgeColor, badgeIcon) ->
                    val solid = badgeText == variant
                    val badgeContent = if (solid) Color.White else badgeColor
                    Spacer(Modifier.width(ConsuSpacing.sm))
                    Surface(
                        color = if (solid) badgeColor else badgeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(ConsuRadius.sm),
                        border = if (solid) null else BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                badgeIcon,
                                contentDescription = null,
                                tint = badgeContent,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeContent,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }

            if (offer.rejectionReason != null) {
                Spacer(Modifier.height(ConsuSpacing.md))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ConsuRadius.sm),
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(ConsuSpacing.md))

            // ── Cifra principal y plazo ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = amountLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = mutedText,
                        letterSpacing = 0.8.sp
                    )
                    CurrencyText(
                        value = offer.mainValue,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "PLAZO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = mutedText,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        offer.termDisplay,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp
                    )
                }
            }

            Spacer(Modifier.height(ConsuSpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // ── Dato secundario y acción discreta ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = mutedText)) {
                            append("${offer.paymentNoun} ")
                        }
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)) {
                            append("$${offer.monthlyPayment.toCurrency()}")
                        }
                    },
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                if (onShowAmortization != null) {
                    TextButton(
                        onClick = onShowAmortization,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            Icons.Rounded.TableChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Amortización",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shorter result card for the IMSS Bienestar agreement.
 *
 * The agreement is biweekly, so displaying the two time units in one line
 * made the generic card wrap and compete with the payment amount. This layout
 * keeps one concept per metric and makes the card readable on narrow phones.
 */
@Composable
private fun ImssBienestarResultCard(
    offer: LoanOffer,
    onClick: () -> Unit,
    onShowAmortization: (() -> Unit)?,
    rankIndex: Int,
    accentColor: Color,
    isPremium: Boolean
) {
    val isBest = rankIndex == 0 || offer.isBest
    val borderColor = when {
        isBest -> ConsuOrange.copy(alpha = 0.55f)
        isPremium -> accentColor.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(ConsuRadius.xl),
        border = BorderStroke(if (isBest || isPremium) 1.dp else 0.5.dp, borderColor),
        shadowElevation = if (isBest || isPremium) 3.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(ConsuSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ConsuSpacing.xs)
                ) {
                    Text(
                        text = "CONSUBANCO",
                        color = accentColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = ConsuOrange.copy(alpha = 0.11f),
                        shape = RoundedCornerShape(ConsuRadius.sm)
                    ) {
                        Text(
                            text = "IMSS BIENESTAR",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = ConsuOrangeDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.45.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isBest) {
                    Surface(
                        color = ConsuOrange.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(ConsuRadius.sm),
                        border = BorderStroke(1.dp, ConsuOrange.copy(alpha = 0.28f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = ConsuOrange,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "MEJOR OPCIÓN",
                                color = ConsuOrange,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (offer.amount > offer.mainValue) "REMANENTE" else "MONTO A RECIBIR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )
                CurrencyText(
                    value = offer.mainValue,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        letterSpacing = (-0.7).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm)
            ) {
                BienestarMetric(
                    label = "PAGO QUINCENAL",
                    value = "$${offer.monthlyPayment.toCurrency()}",
                    modifier = Modifier.weight(1f)
                )
                BienestarMetric(
                    label = "PLAZO",
                    value = "${offer.term} meses",
                    supporting = "${offer.periodCount} quincenas",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.sm)
            ) {
                BienestarMetric(
                    label = "CAT",
                    value = offer.cat,
                    modifier = Modifier.weight(1f)
                )
                BienestarMetric(
                    label = "TASA",
                    value = offer.tipoTasa,
                    modifier = Modifier.weight(1f)
                )
            }

            if (onShowAmortization != null) {
                Surface(
                    onClick = onShowAmortization,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ConsuRadius.md),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 11.dp, horizontal = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TableChart,
                            contentDescription = "Ver amortización",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Ver amortización",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BienestarMetric(
    label: String,
    value: String,
    supporting: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = RoundedCornerShape(ConsuRadius.md)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.53f),
                letterSpacing = 0.35.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuicyTermDropdown(
    selectedValue: Int,
    options: List<Int> = listOf(12, 18, 24, 36, 48, 54, 60),
    onSelectionChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            "PLAZO",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = "$selectedValue meses",
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
                    Icon(Icons.Rounded.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                shape = RoundedCornerShape(ConsuRadius.lg),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
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
                                Text("$term meses", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                val widthFraction = term / 60f
                                Box(
                                    modifier = Modifier
                                        .height(4.dp)
                                        .width(40.dp * widthFraction)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(ConsuRadius.hairline))
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
