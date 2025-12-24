package com.example.consucalcu.academy

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Importar colores desde MainActivity
import com.example.consucalcu.JuicyGreen
import com.example.consucalcu.JuicyYellow
import com.example.consucalcu.JuicyRed
import com.example.consucalcu.JuicyBlue
import com.example.consucalcu.JuicyOrange

import kotlinx.coroutines.delay

/**
 * BATTLE SCREEN - Pantalla de batalla RPG
 * Sistema de combate por turnos con preguntas
 */
@Composable
fun BattleScreen(
    lesson: Lesson,
    battleState: BattleState,
    onAnswer: (Boolean) -> Unit,
    onExit: () -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    
    val currentQuestion = if (battleState.currentQuestionIndex < lesson.questions.size) {
        lesson.questions[battleState.currentQuestionIndex]
    } else null
    
    // Animación de shake cuando recibe daño
    val shakeOffset by remember { mutableStateOf(Animatable(0f)) }
    
    LaunchedEffect(battleState.playerHP) {
        if (battleState.playerHP < 100) {
            shakeOffset.animateTo(
                targetValue = 10f,
                animationSpec = tween(50)
            )
            shakeOffset.animateTo(
                targetValue = -10f,
                animationSpec = tween(50)
            )
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(50)
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0E2E),
                        Color(0xFF0A0E27)
                    )
                )
            )
    ) {
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar con botón salir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExit) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Salir",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Pregunta ${battleState.currentQuestionIndex + 1}/${battleState.totalQuestions}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // Área de combate
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Enemigo
                BattleEntity(
                    name = "Jefe ${lesson.title}",
                    hp = battleState.enemyHP,
                    maxHP = 100,
                    isEnemy = true,
                    emoji = "👹"
                )
                
                // VS
                Text(
                    text = "⚔️ VS ⚔️",
                    fontSize = 32.sp,
                    color = JuicyYellow,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                // Jugador
                BattleEntity(
                    name = "Tú",
                    hp = battleState.playerHP,
                    maxHP = 100,
                    isEnemy = false,
                    emoji = "🛡️",
                    shakeOffset = shakeOffset.value
                )
            }
            
            // Área de pregunta
            if (!battleState.isVictory && !battleState.isDefeat && currentQuestion != null) {
                QuestionCard(
                    question = currentQuestion,
                    selectedAnswer = selectedAnswer,
                    showFeedback = showFeedback,
                    isCorrect = isCorrect,
                    onAnswerSelected = { answer ->
                        selectedAnswer = answer
                        isCorrect = answer == currentQuestion.correctAnswer
                        showFeedback = true
                    },
                    onContinue = {
                        onAnswer(isCorrect)
                        selectedAnswer = null
                        showFeedback = false
                    }
                )
            }
            
            // Resultado de la batalla
            if (battleState.isVictory || battleState.isDefeat) {
                BattleResult(
                    isVictory = battleState.isVictory,
                    correctAnswers = battleState.correctAnswers,
                    totalQuestions = battleState.totalQuestions,
                    xpEarned = if (battleState.isVictory) lesson.xpReward else 0,
                    stars = StarRating.calculateStars(battleState.correctAnswers, battleState.totalQuestions),
                    onContinue = onExit
                )
            }
        }
    }
}

/**
 * Entidad de batalla (jugador o enemigo)
 */
@Composable
private fun BattleEntity(
    name: String,
    hp: Int,
    maxHP: Int,
    isEnemy: Boolean,
    emoji: String,
    shakeOffset: Float = 0f
) {
    val scale by animateFloatAsState(
        targetValue = if (hp > 0) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = shakeOffset.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Emoji
        Text(
            text = emoji,
            fontSize = 64.sp,
            modifier = Modifier.scale(scale)
        )
        
        // Nombre
        Text(
            text = name,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Barra de HP
        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "HP",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "$hp / $maxHP",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            LinearProgressIndicator(
                progress = { hp.toFloat() / maxHP.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = when {
                    hp > 60 -> JuicyGreen
                    hp > 30 -> JuicyYellow
                    else -> JuicyRed
                },
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

/**
 * Tarjeta de pregunta en batalla
 */
@Composable
private fun QuestionCard(
    question: Question,
    selectedAnswer: String?,
    showFeedback: Boolean,
    isCorrect: Boolean,
    onAnswerSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pregunta
        Text(
            text = question.question,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Opciones
        question.options.forEach { option ->
            AnswerOption(
                text = option,
                isSelected = selectedAnswer == option,
                isCorrect = showFeedback && option == question.correctAnswer,
                isWrong = showFeedback && selectedAnswer == option && !isCorrect,
                enabled = !showFeedback,
                onClick = { onAnswerSelected(option) }
            )
        }
        
        // Explicación y botón continuar
        if (showFeedback) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (question.explanation.isNotEmpty()) {
                        Text(
                            text = if (isCorrect) "✅ ¡Correcto!" else "❌ Incorrecto",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) JuicyGreen else JuicyRed
                        )
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCorrect) JuicyGreen else JuicyOrange
                        )
                    ) {
                        Text("Continuar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Opción de respuesta
 */
@Composable
private fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect -> JuicyGreen.copy(alpha = 0.2f)
        isWrong -> JuicyRed.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = when {
        isCorrect -> JuicyGreen
        isWrong -> JuicyRed
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isCorrect || isWrong) {
                Icon(
                    imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                    contentDescription = null,
                    tint = if (isCorrect) JuicyGreen else JuicyRed,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * Resultado de la batalla
 */
@Composable
private fun BattleResult(
    isVictory: Boolean,
    correctAnswers: Int,
    totalQuestions: Int,
    xpEarned: Int,
    stars: Int,
    onContinue: () -> Unit
) {
    var showConfetti by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (isVictory) {
            delay(300)
            showConfetti = true
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isVictory) Brush.linearGradient(
                    colors = listOf(JuicyGreen, JuicyBlue)
                ) else Brush.linearGradient(
                    colors = listOf(JuicyRed, JuicyOrange)
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isVictory) "🎉 ¡VICTORIA! 🎉" else "💔 Derrota 💔",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        if (isVictory) {
            // Estrellas
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Icon(
                        imageVector = if (index < stars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                        contentDescription = "Estrella",
                        tint = if (index < stars) JuicyYellow else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Text(
                text = "$correctAnswers / $totalQuestions respuestas correctas",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "+$xpEarned XP",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = JuicyYellow
            )
        } else {
            Text(
                text = "Inténtalo de nuevo",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = if (isVictory) JuicyGreen else JuicyRed
            )
        ) {
            Text(
                text = if (isVictory) "Continuar" else "Volver",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}
