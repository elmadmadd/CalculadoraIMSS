package com.example.consucalcu.academy
import com.example.consucalcu.ui.theme.ConsuRadius
import com.example.consucalcu.ui.theme.ConsuSpacing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    onComplete: (correctAnswers: Int, totalCases: Int) -> Unit,
    onClose: () -> Unit
) {
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var isFinished by rememberSaveable { mutableStateOf(false) }
    var showFeedback by rememberSaveable { mutableStateOf(false) }
    var lastAnswerCorrect by rememberSaveable { mutableStateOf(false) }
    var correctAnswers by rememberSaveable { mutableIntStateOf(0) }

    val currentCase = if (!isFinished) DAILY_CASES[currentIndex] else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ConsuSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Decide el caso",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (isFinished) "Práctica terminada" else "Caso ${currentIndex + 1} de ${DAILY_CASES.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.semantics { contentDescription = "Salir de la práctica" }
            ) {
                Icon(Icons.Rounded.Close, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(ConsuSpacing.sm))

        if (isFinished) {
            WinnerCard(
                correctAnswers = correctAnswers,
                totalCases = DAILY_CASES.size,
                onComplete = { onComplete(correctAnswers, DAILY_CASES.size) }
            )
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
                        if (lastAnswerCorrect) correctAnswers++
                        onCaseSolved(lastAnswerCorrect)
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
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .heightIn(min = 360.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(ConsuRadius.xl)
    ) {
        Column(
            Modifier.padding(ConsuSpacing.xxl),
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
                horizontalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
            ) {
                ActionButton(
                    icon = Icons.Rounded.Close,
                    color = JuicyRed,
                    label = "RECHAZAR",
                    modifier = Modifier.weight(1f),
                    onClick = { onSwipe(false) }
                )
                ActionButton(
                    icon = Icons.Rounded.Check,
                    color = JuicyGreen,
                    label = "APROBAR",
                    modifier = Modifier.weight(1f),
                    onClick = { onSwipe(true) }
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(isCorrect: Boolean, explanation: String, onNext: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) JuicyGreen.copy(alpha=0.1f) else JuicyRed.copy(alpha=0.1f)
        ),
        shape = RoundedCornerShape(ConsuRadius.xl),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.padding(ConsuSpacing.xxl),
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
                shape = RoundedCornerShape(ConsuRadius.md)
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
private fun WinnerCard(
    correctAnswers: Int,
    totalCases: Int,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(ConsuRadius.xl)
    ) {
        Column(
            Modifier.padding(ConsuSpacing.xxxl),
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
            Text(
                "$correctAnswers de $totalCases decisiones correctas",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JuicyBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(ConsuRadius.md)
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
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            modifier = Modifier
                .size(64.dp)
                .semantics { contentDescription = label.lowercase() },
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
