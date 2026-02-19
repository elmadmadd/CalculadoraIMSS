package com.example.consucalcu.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.JuicyBlue
import com.example.consucalcu.JuicyGreen
import com.example.consucalcu.JuicyRed
import java.math.BigDecimal
import java.text.DecimalFormat

// ==========================================
// 1. CHART: ANTES VS DESPUÉS (AHORRO VISUAL)
// ==========================================

@Composable
fun BeforeAfterChart(
    currentPayment: BigDecimal,
    newPayment: BigDecimal,
    period: String = "Mensual"
) {
    val saving = currentPayment.subtract(newPayment).max(BigDecimal.ZERO)
    val maxVal = currentPayment.max(newPayment).toFloat().coerceAtLeast(1f)
    
    // Animación de las barras
    var triggerAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggerAnim = true }
    
    val bar1Fill by animateFloatAsState(
        targetValue = if (triggerAnim) (currentPayment.toFloat() / maxVal) else 0f,
        animationSpec = tween(1000)
    )
    val bar2Fill by animateFloatAsState(
        targetValue = if (triggerAnim) (newPayment.toFloat() / maxVal) else 0f,
        animationSpec = tween(1000, delayMillis = 300)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "IMPACTO EN TU BOLSILLO",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            
            Row(
                Modifier.fillMaxWidth().height(140.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Barra Actual (Antes)
                BarColumn(
                    label = "ANTES",
                    amount = currentPayment,
                    fillHeight = bar1Fill,
                    color = JuicyRed.copy(alpha = 0.8f)
                )
                
                // Barra Nueva (Después)
                BarColumn(
                    label = "AHORA",
                    amount = newPayment,
                    fillHeight = bar2Fill,
                    color = JuicyGreen
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Resumen de Ahorro
            Surface(
                color = JuicyGreen.copy(alpha=0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = JuicyGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Liberas $${saving.toCurrency()} al mes",
                        color = JuicyGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BarColumn(label: String, amount: BigDecimal, fillHeight: Float, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            "$${amount.toCurrency()}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(0.8f) // Espacio reservado para las barras dentro del Row
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(MaterialTheme.colorScheme.surface) // Fondo de la "pista"
                ,
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillHeight) // Altura dinámica animada
                    .background(color)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ==========================================
// 2. SEMÁFORO DE DIAGNÓSTICO (VIABILITY TRAFFIC LIGHT)
// ==========================================

@Composable
fun ViabilityTrafficLight(
    checks: List<Pair<String, Boolean>> // Ejemplo: ("Capacidad" to true, "Plazo" to false)
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "DIAGNÓSTICO DE VIABILIDAD",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            
            checks.forEach { (label, itemsOk) ->
                CheckRow(label, itemsOk)
                if (label != checks.last().first) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                }
            }
        }
    }
}

@Composable
private fun CheckRow(label: String, isOk: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.8f)
        )
        
        Surface(
            color = if (isOk) JuicyGreen.copy(alpha=0.2f) else JuicyRed.copy(alpha=0.2f),
            shape = RoundedCornerShape(50),
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isOk) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                    null,
                    tint = if (isOk) JuicyGreen else JuicyRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isOk) "APROBADO" else "REVISAR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOk) JuicyGreen else JuicyRed
                )
            }
        }
    }
}

// Extension util
private fun BigDecimal.toCurrency(): String {
    val df = DecimalFormat("#,###")
    return df.format(this)
}
