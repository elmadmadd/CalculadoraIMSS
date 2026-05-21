package com.example.consucalcu

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.graphics.graphicsLayer

/**
 * Componentes UI mejorados para ConsuCalcu.
 * Incluye skeletons, badges, chips y otros elementos reutilizables.
 */

// ========== COLORES GLOBALES ==========
val SkeletonBaseLight = Color(0xFFE0E0E0)
val SkeletonHighlightLight = Color(0xFFF5F5F5)
val SkeletonBaseDark = Color(0xFF424242)
val SkeletonHighlightDark = Color(0xFF616161)

// ========== SKELETON LOADING ==========

/**
 * Efecto shimmer animado para skeletons.
 */
@Composable
fun shimmerBrush(
    targetColor: Color = Color(0xFFE2E8F0), // Slate-200
    highlightColor: Color = Color(0xFFF1F5F9), // Slate-100
    accentColor: Color = Color(0xFF93C5FD).copy(alpha = 0.3f), // Blue-300 con alpha
    showShimmer: Boolean = true
): Brush {
    return if (showShimmer) {
        // Colores premium con toque de azul
        val shimmerColors = listOf(
            targetColor,
            highlightColor,
            accentColor, // ✨ NEW: Acento de color para shimmer premium
            highlightColor,
            targetColor
        )
        
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1500f, // Mayor rango para efecto más suave
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing), // Más lento y suave
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )
        
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim.value - 300f, translateAnim.value - 300f),
            end = Offset(translateAnim.value, translateAnim.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(targetColor, targetColor),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

/**
 * Skeleton de una tarjeta de resultado (LoanOffer) con efecto de latido.
 */
@Composable
fun ResultCardSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    
    // ========== HEARTBEAT ANIMATION ==========
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val heartbeatScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeatScale"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = heartbeatScale
                scaleY = heartbeatScale
            },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Monto skeleton (Hero element)
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Pill skeleton
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush)
            )
            
            Spacer(Modifier.height(20.dp))
            
            // Divider skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(brush)
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Footer skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}

/**
 * Lista de skeletons para mostrar mientras cargan las ofertas.
 */
@Composable
fun ResultCardSkeletonList(
    count: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(count) { index ->
            // Animación de entrada escalonada
            val enterAnim = remember {
                Animatable(0f)
            }
            LaunchedEffect(Unit) {
                enterAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 100,
                        easing = FastOutSlowInEasing
                    )
                )
            }
            
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = enterAnim.value
                        translationY = (1f - enterAnim.value) * 50f
                    }
            ) {
                ResultCardSkeleton()
            }
        }
    }
}

// Función auxiliar eliminada para evitar StackOverflow
// Se debe usar el import directo androidx.compose.ui.graphics.graphicsLayer

// ========== BADGE / CHIP MEJORADO ==========

/**
 * Badge para mostrar el CAT de forma prominente.
 */
@Composable
fun CatBadge(
    cat: String,
    isPreferencial: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isPreferencial) {
        Color(0xFF4CAF50).copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isPreferencial) {
        Color(0xFF2E7D32)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CAT",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.7f)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = cat,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
    }
}

/**
 * Chip de plazo estilizado.
 */
@Composable
fun TermChip(
    term: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = "${term}m",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ========== INDICADOR DE AHORRO ==========

/**
 * Mini barra comparativa de tasas.
 */
@Composable
fun SavingsIndicator(
    traditionalRate: Float,
    preferentialRate: Float,
    modifier: Modifier = Modifier
) {
    val savingsPercent = ((traditionalRate - preferentialRate) / traditionalRate * 100).toInt()
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ahorro vs Tradicional",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "-$savingsPercent%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF69F0AE) // Verde éxito
            )
        }
        
        Spacer(Modifier.height(6.dp))
        
        // Barra de comparación
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(preferentialRate / traditionalRate)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF69F0AE),
                                Color(0xFF00E676)
                            )
                        )
                    )
            )
        }
    }
}

// ========== ANIMACIONES DE NÚMEROS ==========

/**
 * Número que crece con animación dramática.
 */
@Composable
fun AnimatedCurrency(
    targetValue: Double,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    fontSize: Int = 42
) {
    var displayValue by remember { mutableStateOf(0.0) }
    
    LaunchedEffect(targetValue) {
        animate(
            initialValue = displayValue.toFloat(),
            targetValue = targetValue.toFloat(),
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        ) { value, _ ->
            displayValue = value.toDouble()
        }
    }
    
    Text(
        text = "$${String.format("%,.2f", displayValue)}",
        modifier = modifier,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Black,
        color = textColor,
        letterSpacing = (-1.5).sp
    )
}

// ========== PULSATING DOT ==========

/**
 * Punto pulsante para indicadores de estado.
 */
@Composable
fun PulsatingDot(
    color: Color = Color(0xFF4CAF50),
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val alpha = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .clip(CircleShape)
            .background(color)
    )
}

// ========== EMPTY STATE ILUSTRADO ==========

/**
 * Estado vacío con ilustración y mensaje amigable.
 */
@Composable
fun EmptyStateIllustration(
    title: String = "Sin resultados",
    message: String = "Ajusta los parámetros para ver ofertas disponibles",
    emoji: String = "🔍",
    modifier: Modifier = Modifier
) {
    // Map emoji hint to Material icon
    val icon = when {
        emoji.contains("🔍") || emoji.contains("search", true) -> Icons.Default.Search
        emoji.contains("👋") || emoji.contains("wave", true) -> Icons.Default.Person
        emoji.contains("🚦") || emoji.contains("light", true) -> Icons.Default.Warning
        else -> Icons.Default.Info
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subtle float animation
        val infiniteTransition = rememberInfiniteTransition(label = "emptyFloat")
        val floatY = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "floatAnim"
        )
        
        // Icon in a circle
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = floatY.value }
                .size(80.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
        
        Spacer(Modifier.height(20.dp))
        
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 0.2.sp
        )
        
        Spacer(Modifier.height(6.dp))
        
        Text(
            text = message,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
