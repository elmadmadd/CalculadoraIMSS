package com.example.consucalcu.academy

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.consucalcu.JuicyBlue
import com.example.consucalcu.JuicyGreen
import com.example.consucalcu.JuicyRed


data class CreditCase(
    val id: String,
    val title: String,
    val description: String,
    val isViable: Boolean,
    val explanation: String,
    val difficulty: String = "Fácil"
)

// Casos de ejemplo Hardcoded (en un app real vendrían de BD)
val DAILY_CASES = listOf(
    CreditCase(
        "1", "El Novato", 
        "Cliente con 12 meses de antigüedad en su crédito actual. Quiere refinanciar.", 
        true, 
        "¡Correcto! Solo se requieren 12 meses mínimo para refinanciar (si la capacidad lo permite).",
        "Fácil"
    ),
    CreditCase(
        "2", "La Trampa del Plazo", 
        "Cliente tiene crédito a 48 meses. Quiere bajarlo a 36 meses para pagar menos intereses.", 
        false, 
        "¡Error! Por normativa CNCA, el refinanciamiento debe EXTENDER el plazo (a 54+ meses). No se puede reducir.",
        "Medio"
    ),
    CreditCase(
        "3", "Sin Capacidad", 
        "Cliente tiene liquidez negativa (-$200) pero promete cancelar una tarjeta de crédito externa.", 
        false, 
        "¡No viable! La capacidad debe ser positiva al momento del cálculo. Las promesas no cuentan para el sistema.",
        "Difícil"
    )
)

@Composable
fun CreditMatchGame(
    onCaseSolved: (Boolean) -> Unit, // True si acertó
    onClose: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var lastAnswerCorrect by remember { mutableStateOf(false) }
    
    val currentCase = if (!isFinished) DAILY_CASES[currentIndex] else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isFinished) {
            WinnerCard(onClose)
        } else if (currentCase != null) {
            if (showFeedback) {
                FeedbackCard(
                    isCorrect = lastAnswerCorrect,
                    explanation = currentCase.explanation,
                    onNext = {
                        showFeedback = false
                        if (currentIndex < DAILY_CASES.size - 1) {
                            currentIndex++
                        } else {
                            isFinished = true
                        }
                    }
                )
            } else {
                GameCard(
                    case = currentCase,
                    onSwipe = { voteViable ->
                        lastAnswerCorrect = (voteViable == currentCase.isViable)
                        if (lastAnswerCorrect) onCaseSolved(true)
                        showFeedback = true
                    }
                )
            }
        }
    }
}

@Composable
private fun GameCard(case: CreditCase, onSwipe: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(450.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            Modifier.padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = JuicyBlue.copy(alpha=0.1f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        case.difficulty.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = JuicyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                
                Text(
                    case.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    case.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
            
            // Botones Decisión
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Rounded.Close,
                    color = JuicyRed,
                    label = "RECHAZAR",
                    onClick = { onSwipe(false) }
                )
                ActionButton(
                    icon = Icons.Rounded.Check,
                    color = JuicyGreen,
                    label = "APROBAR",
                    onClick = { onSwipe(true) }
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(isCorrect: Boolean, explanation: String, onNext: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) JuicyGreen.copy(alpha=0.1f) else JuicyRed.copy(alpha=0.1f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (isCorrect) Icons.Rounded.Check else Icons.Rounded.Close,
                null,
                tint = if (isCorrect) JuicyGreen else JuicyRed,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                if (isCorrect) "¡EXCELENTE!" else "UPS...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = if (isCorrect) JuicyGreen else JuicyRed
            )
            Spacer(Modifier.height(16.dp))
            Text(
                explanation,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCorrect) JuicyGreen else JuicyBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "SIGUIENTE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun WinnerCard(onClose: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎉", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "¡CASOS COMPLETADOS!",
                fontWeight = FontWeight.Black,
                color = JuicyBlue,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(8.dp))
            Text("Vuelve mañana para más retos.", color = Color.Gray)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JuicyBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "FINALIZAR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            modifier = Modifier.size(72.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
